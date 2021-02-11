@file:Suppress("FunctionName")

package com.max.reader.app

import com.max.reader.app.command.Command
import com.max.reader.app.command.LoadArticlesByQuery
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType
import com.oliynick.max.tea.core.Initializer
import java.util.*

fun AppInitializer(
    isDarkModeEnabled: Boolean,
): Initializer<AppState, Command> {

    val initScreen = ArticlesState.newLoading(
        UUID.randomUUID(),
        Query("android", QueryType.Regular),
    )

    return Initializer(
        AppState(initScreen, isDarkModeEnabled),
        LoadArticlesByQuery(
            initScreen.id,
            initScreen.query,
            initScreen.articles.size,
            ArticlesState.ArticlesPerPage
        )
    )
}
