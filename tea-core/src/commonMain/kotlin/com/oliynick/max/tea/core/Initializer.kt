@file:Suppress("FunctionName")

package com.oliynick.max.tea.core

/**
 * Initializer is an **impure** function that returns [initial][Initial] snapshot to be consumed
 * by component
 *
 * @param S initial state of the application
 * @param C initial set of commands to be executed
 */
typealias Initializer<S, C> = suspend () -> Initial<S, C>

/**
 * Constructs initializer using initial [state] and set of [commands]
 *
 * @param state initial state
 * @param commands initial set of commands
 */
fun <S, C> Initializer(
    state: S,
    commands: Set<C> = emptySet()
): Initializer<S, C> = { Initial(state, commands) }

/**
 * Constructs initializer using initial [state] and array of [commands]
 *
 * @param S state
 * @param C command
 * @param state initial state
 * @param commands initial set of commands
 */
fun <S, C> Initializer(
    state: S,
    vararg commands: C
): Initializer<S, C> = Initializer(state, setOf(*commands))
