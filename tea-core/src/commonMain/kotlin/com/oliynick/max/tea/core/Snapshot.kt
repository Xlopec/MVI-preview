package com.oliynick.max.tea.core

/**
 * Snapshot is data structure that describes component's current state
 *
 * @param M message
 * @param S state
 * @param C command
 */
sealed class Snapshot<out M, out S, out C> {
    /**
     * Current state of a component
     */
    abstract val currentState: S

    /**
     * Set of commands to be resolved and executed
     */
    abstract val commands: Set<C>
}

/**
 * [Snapshot] that describes component's initial state
 *
 * @param S state
 * @param C command
 */
data class Initial<out S, out C>(
    override val currentState: S,
    override val commands: Set<C>
) : Snapshot<Nothing, S, C>()

/**
 * [Snapshot] that describes component's state
 *
 * @param M message
 * @param S state
 * @param C command
 */
data class Regular<out M, out S, out C>(
    override val currentState: S,
    override val commands: Set<C>,
    /**
     * Previous state of a component
     */
    val previousState: S,
    /**
     * Message that triggered state update
     */
    val message: M
) : Snapshot<M, S, C>()
