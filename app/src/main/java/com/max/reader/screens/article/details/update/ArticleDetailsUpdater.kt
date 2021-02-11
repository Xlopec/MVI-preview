package com.max.reader.screens.article.details.update

import com.max.reader.app.command.ArticleDetailsCommand
import com.max.reader.app.message.ArticleDetailsMessage
import com.max.reader.screens.article.details.ArticleDetailsState
import com.oliynick.max.tea.core.component.UpdateWith

interface ArticleDetailsUpdater {

    fun updateArticleDetails(
        message: ArticleDetailsMessage,
        screen: ArticleDetailsState
    ): UpdateWith<ArticleDetailsState, ArticleDetailsCommand>

}
