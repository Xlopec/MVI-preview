package com.max.reader.screens.article.details.update

import com.max.reader.app.command.ArticleDetailsCommand
import com.max.reader.app.command.DoOpenArticle
import com.max.reader.app.message.ArticleDetailsMessage
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.app.message.OpenInBrowser
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command

object LiveArticleDetailsUpdater : ArticleDetailsUpdater {

    override fun updateArticleDetails(
        message: ArticleDetailsMessage,
        screen: ArticleDetailsState,
    ): UpdateWith<ArticleDetailsState, ArticleDetailsCommand> =
        when(message) {
            is OpenInBrowser -> screen command DoOpenArticle(screen.article)
        }
}
