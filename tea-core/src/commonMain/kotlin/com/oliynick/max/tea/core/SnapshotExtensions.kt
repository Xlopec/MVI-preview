package com.oliynick.max.tea.core

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
operator fun <S> Snapshot<*, S, *>.component1(): S = when (this) {
    is Initial -> currentState
    is Regular -> currentState
}

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
operator fun <C> Snapshot<*, *, C>.component2(): Set<C> = when (this) {
    is Initial -> commands
    is Regular -> commands
}

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
operator fun <S> Snapshot<*, S, *>.component3(): S? = when (this) {
    is Initial -> null
    is Regular -> previousState
}

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
operator fun <M> Snapshot<M, *, *>.component4(): M? = when (this) {
    is Initial -> null
    is Regular -> message
}
