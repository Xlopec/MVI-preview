package com.oliynick.max.tea.core.component.internal

import com.oliynick.max.tea.core.UnstableApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@UnstableApi
suspend fun <T> Flow<T>.into(
    sendChannel: SendChannel<T>
) = collect(sendChannel::send)

internal fun <T> Flow<T>.finishWith(
    flow: Flow<T>
) = onCompletion { th -> if (th != null) throw th else emitAll(flow) }

fun <T> Flow<T>.mergeWith(other: Flow<T>): Flow<T> =
    channelFlow {

        launch {
            other.collect {
                send(it)
            }
        }

        launch {
            collect {
                send(it)
            }
        }
    }

internal inline fun <T, R> Flow<T>.foldFlatten(
    acc: R,
    crossinline transform: suspend (R, T) -> Flow<R>
): Flow<R> {

    var current = acc

    return flatMapConcat { next ->
        transform(current, next)
            .onEach { new -> current = new }
    }
}

internal fun <T> Flow<T>.startFrom(
    t: T
) = onStart { emit(t) }

internal suspend fun <T> FlowCollector<T>.emitAll(
    elements: Iterable<T>
) = elements.forEach { element -> emit(element) }
