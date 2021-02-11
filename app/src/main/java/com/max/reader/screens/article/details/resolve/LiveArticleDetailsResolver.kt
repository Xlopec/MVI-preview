@file:Suppress("FunctionName")

package com.max.reader.screens.article.details.resolve

import android.content.Intent
import android.net.Uri
import com.max.reader.app.command.ArticleDetailsCommand
import com.max.reader.app.command.DoOpenArticle
import com.max.reader.app.message.Message
import com.max.reader.app.message.ScreenMessage
import com.max.reader.app.env.HasAppContext
import com.oliynick.max.tea.core.component.sideEffect

fun <Env> LiveArticleDetailsResolver(): ArticleDetailsResolver<Env> where Env : HasAppContext =
    object : ArticleDetailsResolver<Env> {

        override suspend fun Env.resolve(command: ArticleDetailsCommand): Set<Message> =
            when(command) {
                is DoOpenArticle -> openArticle(command)
            }

        suspend fun Env.openArticle(
            command: DoOpenArticle
        ): Set<ScreenMessage> = command.sideEffect {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url.toString()))
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

            if (intent.resolveActivity(application.packageManager) != null) {
                application.startActivity(intent)
            }
        }

    }
