@file:Suppress("FunctionName")

package com.max.reader.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import com.max.reader.R
import com.max.reader.app.*
import com.max.reader.app.message.*
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType
import com.max.reader.app.message.RefreshArticles
import com.max.reader.screens.article.list.ui.ArticlesScreen
import com.max.reader.screens.home.BottomMenuItem.*
import com.max.reader.screens.settings.SettingsScreen
import com.max.reader.ui.InsetAwareTopAppBar
import com.max.reader.ui.SwipeToRefreshLayout
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

enum class BottomMenuItem {
    Feed,
    Favorite,
    Trending,
    Settings
}

@Composable
fun HomeScreen(
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {
    SwipeToRefreshLayout(
        refreshingState = state.isRefreshing,
        onRefresh = { onMessage(RefreshArticles(state.id)) },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomBar(
                    modifier = Modifier.navigationBarsPadding(),
                    item = state.query.toMenuItem(),
                    onMessage = onMessage
                )
            }, bodyContent = { innerPadding ->
                ArticlesScreen(Modifier.padding(innerPadding), state, onMessage)
            })
    }
}

@Composable
fun HomeScreen(
    state: AppState,
    onMessage: (Message) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            InsetAwareTopAppBar(
                backgroundColor = MaterialTheme.colors.surface,
                title = {
                    Text(text = "Settings")
                }
            )
        },
        bottomBar = {
            BottomBar(
                modifier = Modifier.navigationBarsPadding(),
                item = Settings,
                onMessage = onMessage
            )
        }, bodyContent = { innerPadding ->
            SettingsScreen(
                innerPadding = innerPadding,
                state = state,
                onMessage = onMessage
            )
        })
}

@Composable
fun BottomBar(
    modifier: Modifier,
    item: BottomMenuItem,
    onMessage: (Navigation) -> Unit,
) {
    BottomNavigation(modifier = modifier) {

        BottomNavigationItem(icon = {
            Icon(
                imageVector = vectorResource(id = R.drawable.ic_language_white_24dp),
                contentDescription = "Feed"
            )
        },
            selected = item === Feed,
            onClick = { onMessage(NavigateToFeed) }
        )

        BottomNavigationItem(icon = {
            Icon(
                imageVector = vectorResource(id = R.drawable.ic_favorite_border_white_24dp),
                contentDescription = "Favorite"
            )
        },
            selected = item === Favorite,
            onClick = { onMessage(NavigateToFavorite) }
        )

        BottomNavigationItem(icon = {
            Icon(
                imageVector = Icons.Filled.TrendingUp,
                contentDescription = "Trending"
            )
        },
            selected = item === Trending,
            onClick = { onMessage(NavigateToTrending) }
        )

        BottomNavigationItem(icon = {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings"
            )
        },
            selected = item === Settings,
            onClick = { onMessage(NavigateToSettings) }
        )
    }
}

private fun Query.toMenuItem() = when (type) {
    QueryType.Regular -> Feed
    QueryType.Favorite -> Favorite
    QueryType.Trending -> Trending
}
