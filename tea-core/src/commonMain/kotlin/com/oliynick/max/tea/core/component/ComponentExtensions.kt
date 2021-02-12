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

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * **Impure** function that performs some actions on snapshots
 * @param M message
 * @param S state
 * @param C command
 */
typealias Interceptor<M, S, C> = suspend (snapshot: Snapshot<M, S, C>) -> Unit

/**
 * Extension to combine state with command
 *
 * @receiver state to combine with command
 * @param S state to combine with command
 * @param C command
 * @param command command to combine with state
 * @return [UpdateWith] instance with given state and set that consists from a single command
 */
infix fun <S, C> S.command(
    command: C,
): UpdateWith<S, C> = this to setOf(command)

/**
 * Extension to combine state with a single command provider
 *
 * @receiver state to combine with command
 * @param S state to combine with command
 * @param C command
 * @param command command to combine with state
 * @return [UpdateWith] instance with given state and set that consists from a single command
 */
inline infix fun <S, C> S.command(
    command: S.() -> C,
): UpdateWith<S, C> = this to setOf(run(command))

/**
 * Extension to combine state with two commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param first the first command to combine with state
 * @param second the second command to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
fun <S, C> S.command(
    first: C,
    second: C,
): UpdateWith<S, C> = this to setOf(first, second)

/**
 * Extension to combine state with three commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param first the first command to combine with state
 * @param second the second command to combine with state
 * @param third the third command to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
fun <S, C> S.command(
    first: C,
    second: C,
    third: C,
): UpdateWith<S, C> =
    this to setOf(first, second, third)

/**
 * Extension to combine state with multiple commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param commands commands to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
fun <S, C> S.command(
    vararg commands: C,
): UpdateWith<S, C> = this command setOf(*commands)

/**
 * Extension to combine state with set of commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param commands commands to combine with state
 * @return [UpdateWith] instance with given state and set of commands
 */
infix fun <S, C> S.command(
    commands: Set<C>,
): UpdateWith<S, C> = this to commands

/**
 * Extension to combine state with empty set of commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @return [UpdateWith] instance with given state and empty set of commands
 */
fun <S> S.noCommand(): UpdateWith<S, Nothing> = this to emptySet()

/**
 * Wrapper to perform **only** side effect using command as receiver. This function always returns
 * empty set of messages
 * @param action action to perform that produces no messages that can be consumed by a component
 * @param C command
 * @param M message
 * @return set of messages to be consumed by a component, always empty
 */
suspend inline infix fun <C, M> C.sideEffect(
    crossinline action: suspend C.() -> Unit,
): Set<M> {
    action()
    return emptySet()
}

/**
 * Wrapper to perform side effect computations and possibly return a new message to be consumed by [Updater]
 *
 * @receiver command to be used to execute effect
 * @param C command
 * @param M message
 * @param action action to perform that might produce message to be consumed by a component
 * @return set of messages to be consumed a component
 */
suspend inline infix fun <C, M> C.effect(
    crossinline action: suspend C.() -> M?,
): Set<M> = action(this@effect)?.let(::setOf) ?: emptySet()

/**
 * Transforms component into flow of snapshots
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
fun <M, S, C> Component<M, S, C>.observeSnapshots(): Flow<Snapshot<M, S, C>> =
    this(emptyFlow())

/**
 * Transforms component into flow of states
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
fun <M, S, C> Component<M, S, C>.observeStates(): Flow<S> =
    observeSnapshots().map { snapshot -> snapshot.currentState }

/**
 * Transforms component into function that accepts messages and returns flow that
 * emits states only
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
fun <M, S, C> Component<M, S, C>.states(): ((Flow<M>) -> Flow<S>) =
    { input -> this(input).map { snapshot -> snapshot.currentState } }

/**
 * Supplies [messages] to the component. Note that messages won't be consumed
 * until terminal operator is called on the resulting flow
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
operator fun <M, S, C> Component<M, S, C>.invoke(
    vararg messages: M,
): Flow<Snapshot<M, S, C>> = this(flowOf(*messages))

/**
 * Supplies [messages] to the component. Note that messages won't be consumed
 * until terminal operator is called on the resulting flow
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
operator fun <M, S, C> Component<M, S, C>.invoke(
    messages: Iterable<M>,
): Flow<Snapshot<M, S, C>> = this(messages.asFlow())

/**
 * Supplies [message] to the component. Note that messages won't be consumed
 * until terminal operator is called on the resulting flow
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
operator fun <M, S, C> Component<M, S, C>.invoke(
    message: M,
): Flow<Snapshot<M, S, C>> = this(flowOf(message))

/**
 * Attaches [interceptor] to the component
 *
 * @receiver component to transform
 * @param C command
 * @param M message
 * @param S state
 */
infix fun <M, S, C> Component<M, S, C>.with(
    interceptor: Interceptor<M, S, C>,
): Component<M, S, C> =
    { input -> this(input).onEach(interceptor) }

/**
 * Launches sharing coroutine in a given scope effectively
 * making component hot
 *
 * @receiver component to transform
 * @param scope scope in which sharing coroutine will be started
 * @param C command
 * @param M message
 * @param S state
 */
@Deprecated(message = "will be removed", level = DeprecationLevel.ERROR)
fun <M, S, C> Component<M, S, C>.shareIn(
    scope: CoroutineScope,
): Component<M, S, C> {
    scope.launch { this@shareIn(emptyFlow()).collect() }
    return this
}
