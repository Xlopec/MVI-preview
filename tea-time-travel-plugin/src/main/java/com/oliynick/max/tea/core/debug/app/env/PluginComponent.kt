@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.env

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.trace
import com.oliynick.max.tea.core.Initial
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.Regular
import com.oliynick.max.tea.core.Snapshot
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.Interceptor
import com.oliynick.max.tea.core.component.with
import com.oliynick.max.tea.core.debug.app.component.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.component.cms.PluginState
import com.oliynick.max.tea.core.debug.app.component.cms.Stopped
import com.oliynick.max.tea.core.debug.app.misc.PluginId
import com.oliynick.max.tea.core.debug.app.misc.settings
import kotlinx.coroutines.Dispatchers

fun PluginComponent(
    environment: Environment
): Component<PluginMessage, PluginState, PluginCommand> {

    suspend fun doResolve(
        c: PluginCommand
    ): Set<PluginMessage> = with(environment) { resolve(c) }

    fun doUpdate(
        message: PluginMessage,
        state: PluginState
    ) = with(environment) { update(message, state) }

    return Component(AppInitializer(environment), ::doResolve, ::doUpdate, environment, Dispatchers.IO)
        .with(com.oliynick.max.tea.core.debug.app.env.Logger())
}

private fun AppInitializer(
    environment: Environment
) : Initializer<Stopped, Nothing> =
    { Initial(Stopped(environment.properties.settings), emptySet()) }

private fun Logger(
    logger: Logger = Logger.getInstance(PluginId)
): Interceptor<PluginMessage, PluginState, PluginCommand> =
    { snapshot ->
        logger.info(snapshot.infoMessage())
        logger.debug { snapshot.debugMessage() }
        logger.trace { snapshot.traceMessage() }
    }

private fun Snapshot<*, *, *>.infoMessage(): String =
    when (this) {
        is Initial -> "Init class=${currentState?.javaClass}, commands=${commands.size}"
        is Regular -> """
        Regular with new state=${currentState?.javaClass},
        prev state=${previousState?.javaClass},
        caused by message=${message?.javaClass},
        commands=${commands.size}"}
    """.trimIndent()
    }

private fun Snapshot<*, *, *>.debugMessage(): String =
    when (this) {
        is Initial -> "Init class=${currentState?.javaClass}" +
                if (commands.isEmpty()) "" else ", commands=${commands.joinToString { it?.javaClass.toString() }}"
        is Regular -> """
        Regular with new state=${currentState?.javaClass},
        prev state=${previousState?.javaClass},
        caused by message=${message?.javaClass}
        ${if (commands.isEmpty()) "" else "\ncommands=${commands.joinToString { it?.javaClass.toString() }}"}
    """.trimIndent()
    }

private fun Snapshot<*, *, *>.traceMessage(): String =
    when (this) {
        is Initial -> "Init with state=$currentState" +
                if (commands.isEmpty()) "" else ", commands=$commands"
        is Regular -> """
        Regular with new state=$currentState,
        prev state=$previousState,
        caused by message=$message
        ${if (commands.isEmpty()) "" else "\ncommands=$commands"}
    """.trimIndent()
    }
