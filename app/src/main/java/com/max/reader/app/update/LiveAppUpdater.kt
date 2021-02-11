@file:Suppress("FunctionName")

package com.max.reader.app.update

import com.max.reader.BuildConfig
import com.max.reader.app.*
import com.max.reader.app.command.CloseApp
import com.max.reader.app.command.Command
import com.max.reader.app.command.LoadArticlesByQuery
import com.max.reader.app.message.*
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.screens.article.details.update.ArticleDetailsUpdater
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType
import com.max.reader.screens.article.list.QueryType.*
import com.max.reader.screens.article.list.update.ArticlesUpdater
import com.max.reader.screens.settings.SettingsState
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import java.util.*

fun <Env> AppUpdater(): AppUpdater<Env> where Env : ArticlesUpdater, Env : ArticleDetailsUpdater =
    object : LiveAppUpdater<Env> {}

// todo remove uuid generation from here
interface LiveAppUpdater<Env> : AppUpdater<Env> where Env : ArticlesUpdater,
                                                      Env : ArticleDetailsUpdater {

    override fun Env.update(
        message: Message,
        state: AppState,
    ): UpdateWith<AppState, Command> =
        when (message) {
            is Navigation -> navigate(message, state)
            is ScreenMessage -> updateScreen(message, state)
        }

    fun Env.updateScreen(
        message: ScreenMessage,
        state: AppState,
    ): UpdateWith<AppState, Command> =
        when (message) {
            is ArticlesMessage -> state.updateScreen<ArticlesState>(message.id) { screen ->
                updateArticles(message, screen)
            }
            is ArticleDetailsMessage -> state.updateScreen<ArticleDetailsState>(message.id) { screen ->
                updateArticleDetails(message, screen)
            }
            is SettingsMessage -> updateSettings(message, state)
        }

    fun updateSettings(
        message: SettingsMessage,
        state: AppState
    ): UpdateWith<AppState, Command> =
        when(message) {
            is ToggleDarkMode -> state.copy(isDarkModeEnabled = !state.isDarkModeEnabled).noCommand()
        }

    fun navigate(
        nav: Navigation,
        state: AppState,
    ): UpdateWith<AppState, Command> =
        when (nav) {
            is NavigateToFeed -> state.pushBottomNavigationScreen(nav, Query("android", Regular))
            is NavigateToFavorite -> state.pushBottomNavigationScreen(nav, Query("", Favorite))
            is NavigateToTrending -> state.pushBottomNavigationScreen(nav, Query("", Trending))
            is NavigateToArticleDetails -> state.pushArticleDetailsScreen(nav)
            is NavigateToSettings -> state.pushBottomNavigationScreenForSettings()
            is Pop -> state.pop()
        }

    fun AppState.pop() =
        // if we encounter any screen out of bottom bar screens, we just close the app;
        // we pop the last screen in another case
        if (screens.last().isBottomBarScreen) this command CloseApp
        else popScreen().noCommand()

    fun AppState.pushArticleDetailsScreen(
        nav: NavigateToArticleDetails,
    ) = pushScreen(ArticleDetailsState(/*fixme: move UUID generation out of here*/UUID.randomUUID(), nav.article)).noCommand()

    fun AppState.pushBottomNavigationScreenForSettings(): UpdateWith<AppState, Command> {
        val i = screens.indexOfFirst { s -> s is SettingsState }
        val newState = if (i >= 0) {
            // move current screen to the end of screens stack,
            // so that it'll be popped out first
            swapWithLast(i)
        } else {
            pushScreen(SettingsState)
        }

        if (BuildConfig.DEBUG) {
            checkSettingsScreensNumber(newState, this)
        }

        return newState.noCommand()
    }

    fun AppState.pushBottomNavigationScreen(
        nav: Navigation,
        query: Query,
    ): UpdateWith<AppState, Command> {

        val i = findExistingArticlesScreenForNavigation(nav)
        //fixme: move UUID generation out of here
        val id by lazy { UUID.randomUUID() }
        val swap = i >= 0
        val newState = if (swap) {
            // move current screen to the end of screens stack,
            // so that it'll be popped out first
            swapWithLast(i)
        } else {
            pushScreen(ArticlesState.newLoading(id, query))
        }

        if (BuildConfig.DEBUG) {
            checkArticlesScreensNumber(nav, query, newState, this)
        }

        return if (swap) newState.noCommand()
        else newState command LoadArticlesByQuery(id, query, 0, ArticlesState.ArticlesPerPage)
    }

    fun AppState.findExistingArticlesScreenForNavigation(
        nav: Navigation,
    ): Int = screens.indexOfFirst { s ->
        s is ArticlesState && isCriteriaMatches(s.query.type, nav)
    }

    fun isCriteriaMatches(
        type: QueryType,
        nav: Navigation,
    ): Boolean =
        when (type) {
            Regular -> nav === NavigateToFeed
            Favorite -> nav === NavigateToFavorite
            Trending -> nav === NavigateToTrending
        }

    val ArticlesMessage.id: ScreenId?
        get() = when (this) {
            // todo extract interface
            is LoadNextArticles -> id
            is ToggleArticleIsFavorite -> id
            is ArticlesLoaded -> id
            is ArticlesOperationException -> id
            is OnQueryUpdated -> id
            is RefreshArticles -> id
            is LoadArticlesFromScratch -> id
            is ArticleUpdated, is ShareArticle -> null
        }

    val ArticleDetailsMessage.id: ScreenId
        get() = when (this) {
            is OpenInBrowser -> id
        }

    private inline val ScreenState.isBottomBarScreen: Boolean
        get() = this is ArticlesState || this is SettingsState

    private fun checkArticlesScreensNumber(
        nav: Navigation,
        query: Query,
        newState: AppState,
        oldState: AppState
    ) {
        val grouped = newState.screens.groupBy { screen -> (screen as? ArticlesState)?.query?.type }

        check(QueryType.values().map { type -> grouped[type]?.size ?: 0 }.all { size -> size <= 1 }) {
            WrongScreensNumberErrorMessage(nav, query, newState, oldState)
        }
    }

    private fun checkSettingsScreensNumber(
        newState: AppState,
        oldState: AppState
    ) =
        check(newState.screens.count { screen -> screen is SettingsState } == 1) {
            WrongSettingsScreensNumberErrorMessage(newState, oldState)
        }

    private fun WrongSettingsScreensNumberErrorMessage(
        newState: AppState,
        oldState: AppState
    ) = "Wrong number of settings screens after update," +
            "new state: $newState,\nold state: $oldState"

    private fun WrongScreensNumberErrorMessage(
        nav: Navigation,
        query: Query,
        newState: AppState,
        oldState: AppState
    ) = "Wrong number of bottom navigation screens after navigation update: " +
            "$nav,\nquery: $query,\nnew state: $newState,\nold state: $oldState"
}
