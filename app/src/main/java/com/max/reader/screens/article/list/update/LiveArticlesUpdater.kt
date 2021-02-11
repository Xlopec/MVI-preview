package com.max.reader.screens.article.list.update

import com.max.reader.app.command.*
import com.max.reader.app.message.*
import com.max.reader.domain.Article
import com.max.reader.domain.toggleFavorite
import com.max.reader.screens.article.list.*
import com.max.reader.screens.article.list.QueryType.*
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

// nothing is private in our world
@Suppress("MemberVisibilityCanBePrivate")
object LiveArticlesUpdater : ArticlesUpdater {

    override fun updateArticles(
        message: ArticlesMessage,
        state: ArticlesState
    ): UpdateWith<ArticlesState, Command> =
        when (message) {
            is ArticlesLoaded -> state.toPreview(message.articles, message.hasMore).noCommand()
            is LoadNextArticles -> loadNextArticles(state)
            is LoadArticlesFromScratch, is RefreshArticles -> state.toRefreshing() command LoadArticlesByQuery(state.id, state.query, state.articles.size, ArticlesState.ArticlesPerPage)
            is ArticlesOperationException -> state.toException(message.cause).noCommand()
            is ToggleArticleIsFavorite -> toggleFavorite(message.article, state)
            is ArticleUpdated -> updateArticle(message.article, state)
            is ShareArticle -> shareArticle(message.article, state)
            // fixme redesign FeedState
            is OnQueryUpdated -> updateQuery(message.query, state)
        }

    fun loadNextArticles(
        state: ArticlesState
    ) =
        if (state.isPreview && state.hasMoreArticles) {
            state.toLoadingNext() command LoadArticlesByQuery(
                state.id,
                state.query,
                state.articles.size,
                ArticlesState.ArticlesPerPage
            )
        } else {
            state.noCommand()
        }

    fun updateArticle(
        article: Article,
        state: ArticlesState
    ): UpdateWith<ArticlesState, Command> {

        val updated = when (state.query.type) {
            Regular, Trending -> state.updateArticle(article)
            Favorite -> if (article.isFavorite) state.prependArticle(article) else state.removeArticle(article)
        }

        return updated.noCommand()
    }

    fun toggleFavorite(
        article: Article,
        state: ArticlesState
    ): UpdateWith<ArticlesState, Command> {

        val toggled = article.toggleFavorite()

        return state.updateArticle(toggled) command toggled.storeCommand()
    }

    fun shareArticle(
        article: Article,
        state: ArticlesState
    ): UpdateWith<ArticlesState, DoShareArticle> = state command DoShareArticle(article)

    fun updateQuery(
        input: String,
        state: ArticlesState
    ): UpdateWith<ArticlesState, ArticlesCommand> = state.updateQuery(input).noCommand()

    fun Article.storeCommand() = if (isFavorite) SaveArticle(this) else RemoveArticle(this)

    // fixme inline
    fun ArticlesState.updateQuery(
        input: String
    ) = copy(query = query.copy(input = input))

}
