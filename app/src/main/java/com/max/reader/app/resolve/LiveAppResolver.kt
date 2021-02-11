@file:Suppress("FunctionName")

package com.max.reader.app.resolve

import com.max.reader.app.command.ArticleDetailsCommand
import com.max.reader.app.command.ArticlesCommand
import com.max.reader.app.command.CloseApp
import com.max.reader.app.command.Command
import com.max.reader.app.message.Message
import com.max.reader.screens.article.details.resolve.ArticleDetailsResolver
import com.max.reader.screens.article.list.resolve.ArticlesResolver
import com.oliynick.max.tea.core.component.sideEffect
import kotlinx.coroutines.channels.BroadcastChannel

@Deprecated("wait until it'll be fixed")
fun <Env> AppResolver(): AppResolver<Env> where Env : HasCommandTransport,
                                                Env : ArticlesResolver<Env>,
                                                Env : ArticleDetailsResolver<Env> = object : AppResolver<Env> {
    override suspend fun Env.resolve(command: Command): Set<Message> =
        when (command) {
            is CloseApp -> close(command)
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
        }

    suspend fun Env.close(
        command: CloseApp
    ): Set<Message> = command.sideEffect { closeCommands.send(command) }
}

interface HasCommandTransport {
    val closeCommands: BroadcastChannel<CloseApp>
}

fun CommandTransport() = object : HasCommandTransport {
    override val closeCommands: BroadcastChannel<CloseApp> = BroadcastChannel(1)
}
