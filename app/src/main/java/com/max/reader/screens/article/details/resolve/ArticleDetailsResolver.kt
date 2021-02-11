package com.max.reader.screens.article.details.resolve

import com.max.reader.app.command.ArticleDetailsCommand
import com.max.reader.app.message.Message

interface ArticleDetailsResolver<Env> {

    suspend fun Env.resolve(command: ArticleDetailsCommand): Set<Message>

}
