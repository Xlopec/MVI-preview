@file:Suppress("FunctionName")

package com.max.reader.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import com.max.reader.R
import com.max.reader.app.*
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.article.list.LoadCriteria
import com.max.reader.screens.article.list.ui.ArticlesScreen
import com.max.reader.screens.home.BottomMenuItem.*
import com.max.reader.ui.InsetAwareTopAppBar
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

enum class BottomMenuItem {
    FEED,
    FAVORITE,
    TRENDING
}

@Composable
fun HomeScreen(
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            InsetAwareTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(text = state.toScreenTitle())
                },
            )
        }, bottomBar = {
            BottomBar(
                modifier = Modifier.navigationBarsPadding(),
                item = state.criteria.toMenuItem(),
                onMessage = onMessage
            )
        }, bodyContent = { innerPadding ->
            ArticlesScreen(Modifier.padding(innerPadding), state, onMessage)
        })
}

private fun LoadCriteria.toMenuItem() = when (this) {
    is LoadCriteria.Query -> FEED
    LoadCriteria.Favorite -> FAVORITE
    LoadCriteria.Trending -> TRENDING
}

@Composable
fun BottomBar(
    modifier: Modifier,
    item: BottomMenuItem,
    onMessage: (Navigation) -> Unit,
) {
    BottomNavigation(modifier = modifier) {

        BottomNavigationItem(icon = {
            Icon(asset = vectorResource(id = R.drawable.ic_language_white_24dp))
        },
            selected = item === FEED,
            onClick = { onMessage(NavigateToFeed) }
        )

        BottomNavigationItem(icon = {
            Icon(asset = vectorResource(id = R.drawable.ic_favorite_border_white_24dp))
        },
            selected = item === FAVORITE,
            onClick = { onMessage(NavigateToFavorite) }
        )

        BottomNavigationItem(icon = {
            Icon(asset = vectorResource(id = R.drawable.ic_trending_up_white_24dp))
        },
            selected = item === TRENDING,
            onClick = { onMessage(NavigateToTrending) }
        )
    }
}

private fun ArticlesState.toScreenTitle(): String =
    when (criteria) {
        is LoadCriteria.Query -> "Feed"
        is LoadCriteria.Favorite -> "Favorite"
        is LoadCriteria.Trending -> "Trending"
    }
