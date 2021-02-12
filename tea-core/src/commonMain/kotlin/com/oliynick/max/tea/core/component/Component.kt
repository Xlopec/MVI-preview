/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.internal.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*

/**
 * Component is a builder that accepts [flow][Flow] of messages as input and returns
 * [flow][Flow] of [snapshots][Snapshot] as result.
 *
 * Some behavior notes:
 * * the resulting flow of snapshots always provides observer with the latest available snapshot
 * * the whole component is a lazy function which means that incoming messages won't be consumed
 * unless there is an active collector because of the [Flow] semantic
 *
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
typealias Component<M, S, C> = (messages: Flow<M>) -> Flow<Snapshot<M, S, C>>

/**
 * Updater is a **pure** function that accepts message and current state of the application and then returns
 * a new state and possible empty set of commands to be resolved
 *
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
typealias Updater<M, S, C> = (message: M, state: S) -> UpdateWith<S, C>

/**
 * Alias for result of the [updater][Updater] function
 *
 * @param S state of the component
 * @param C commands to be executed
 */
typealias UpdateWith<S, C> = Pair<S, Set<C>>

/**
 * Alias for an **impure** function that resolves commands and returns possibly empty set of messages to feed the
 * [updater][Updater]
 *
 * @param M incoming messages
 * @param C commands to be executed
 */
typealias Resolver<C, M> = suspend (command: C) -> Set<M>

@UnstableApi
typealias Sink<T> = suspend (T) -> Unit

/**
 * Creates new component using supplied values
 *
 * @param initializer initializer to be used to provide initial values for application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
fun <M, C, S> Component(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    // todo: group to reduce number of arguments
    scope: CoroutineScope,
    io: CoroutineDispatcher,
    computation: CoroutineDispatcher = Dispatchers.Unconfined,
    shareOptions: ShareOptions = ShareStateWhileSubscribed,
): Component<M, S, C> =
    Component(Env(initializer, resolver, updater, scope, io, computation, shareOptions))

/**
 * Creates new component using preconfigured environment
 *
 * @param env environment to be used
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
@OptIn(UnstableApi::class)
fun <M, S, C> Component(
    env: Env<M, S, C>,
): Component<M, S, C> {

    val input = Channel<M>(Channel.RENDEZVOUS)

    fun input(
        startFrom: Initial<S, C>,
    ) = env.resolveAsFlow(startFrom.commands)
        .mergeWith(input.receiveAsFlow())

    val upstream = env.upstream(env.init(), input::send, ::input)
        .shareIn(env.scope, env.shareOptions)

    return { messages -> upstream.downstream(messages, input) }
}

@UnstableApi
fun <M, S, C> Env<M, S, C>.upstream(
    snapshots: Flow<Initial<S, C>>,
    sink: Sink<M>,
    input: (Initial<S, C>) -> Flow<M>,
): Flow<Snapshot<M, S, C>> =
    snapshots.flatMapLatest { startFrom -> compute(input(startFrom), startFrom, sink) }

@UnstableApi
fun <M, S, C> Flow<Snapshot<M, S, C>>.downstream(
    input: Flow<M>,
    upstreamInput: SendChannel<M>,
): Flow<Snapshot<M, S, C>> =
    channelFlow {
        @Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
        onStart { launch { input.into(upstreamInput) } }
            .into(channel)
    }

@UnstableApi
fun <S, C> Env<*, S, C>.init(): Flow<Initial<S, C>> =
    channelFlow { withContext(io) { send(initializer()) } }

@UnstableApi
fun <M, S, C> Env<M, S, C>.compute(
    input: Flow<M>,
    startFrom: Initial<S, C>,
    sink: Sink<M>,
): Flow<Snapshot<M, S, C>> {

    suspend fun newState(
        current: Snapshot<M, S, C>,
        message: M,
    ): Regular<M, S, C> {
        val (newState, commands) = update(message, current.currentState)

        return Regular(newState, commands, current.currentState, message)
    }

    return channelFlow {

        var current: Snapshot<M, S, C> = startFrom

        input
            .map { message -> newState(current, message) }
            .onEach { regular -> current = regular }
            .onEach { regular -> resolveAll(this@channelFlow, sink, regular.commands) }
            .collect(::send)

    }.startFrom(startFrom)
}

@UnstableApi
fun <T> Flow<T>.shareIn(
    scope: CoroutineScope,
    shareOptions: ShareOptions,
) = shareIn(scope, shareOptions.started, shareOptions.replay.toInt())

@UnstableApi
fun <M, S, C> Env<M, S, C>.resolveAsFlow(
    commands: Collection<C>,
): Flow<M> =
    flow { emitAll(resolve(commands)) }

private fun <M, S, C> Env<M, S, C>.resolveAll(
    coroutineScope: CoroutineScope,
    sink: Sink<M>,
    commands: Iterable<C>,
) =
// launches each suspending function
// in 'launch and forget' fashion so that
// updater can process a new portion of messages
    commands.forEach { command ->
        coroutineScope.launch(io + CoroutineName("Resolver coroutine: $command")) {
            sink(resolver(command))
        }
    }

private suspend operator fun <E> Sink<E>.invoke(
    elements: Iterable<E>,
) = elements.forEach { e -> invoke(e) }

private suspend fun <M, S, C> Env<M, S, C>.update(
    message: M,
    state: S,
): UpdateWith<S, C> =
    withContext(computation) { updater(message, state) }

private suspend fun <M, C> Env<M, *, C>.resolve(
    commands: Collection<C>,
): Iterable<M> =
    commands
        .parMapTo(io, resolver::invoke)
        .flatten()
