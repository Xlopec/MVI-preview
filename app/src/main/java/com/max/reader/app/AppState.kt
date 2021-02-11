package com.max.reader.app

import com.max.reader.app.command.Command
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import java.util.*
import kotlin.collections.ArrayList

typealias ScreenId = UUID

abstract class ScreenState {
    abstract val id: ScreenId?
}

data class AppState(
    val isDarkModeEnabled: Boolean,
    val screens: PersistentList<ScreenState>,
) {

    constructor(screen: ScreenState, isNightModeEnabled: Boolean) :
            this(isNightModeEnabled, persistentListOf(screen))

    init {
        require(screens.isNotEmpty())
    }
}

inline val AppState.screen: ScreenState
    get() = screens.last()

inline fun <reified T : ScreenState> AppState.updateScreen(
    id: ScreenId?,
    how: (T) -> UpdateWith<T, Command>,
): UpdateWith<AppState, Command> {

    val index by lazy { screens.indexOfFirst { screen -> screen.id == id && screen is T } }

    return when {
        id == null -> updateScreen(how)
        index < 0 -> noCommand()
        else -> {
            val (screen, commands) = how(screens[index] as T)

            copy(screens = screens.set(index, screen)) command commands
        }
    }
}

inline fun <reified T : ScreenState> AppState.updateScreen(
    how: (T) -> UpdateWith<T, Command>,
): UpdateWith<AppState, Command> {

    val cmds = mutableSetOf<Command>()
    val scrs = screens.fold(ArrayList<ScreenState>(screens.size)) { acc, screen ->

        if (screen is T) {
            val (updatedScreen, commands) = how(screen)

            cmds += commands
            acc += updatedScreen
        } else {
            acc += screen
        }

        acc
    }.toPersistentList()

    return copy(screens = scrs) command cmds
}

fun AppState.swapWithLast(
    i: Int,
) = swapScreens(i, screens.lastIndex)

fun AppState.swapScreens(
    i: Int,
    j: Int,
): AppState {

    if (i == j) return this

    val tmp = screens[j]

    return copy(screens = screens.set(j, screens[i]).set(i, tmp))
}

fun AppState.pushScreen(
    screen: ScreenState,
): AppState = copy(screens = screens.add(screen))

fun AppState.popScreen(): AppState = copy(screens = screens.pop())

private fun <T> PersistentList<T>.pop() = if (lastIndex >= 0) removeAt(lastIndex) else this
