package com.max.reader.screens.article.list.resolve

import com.max.reader.app.command.ArticlesCommand
import com.max.reader.app.message.Message

interface ArticlesResolver<Env> {

    suspend fun Env.resolve(command: ArticlesCommand): Set<Message>

}
