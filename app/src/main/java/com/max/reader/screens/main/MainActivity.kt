package com.max.reader.screens.main

import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.max.reader.R
import com.max.reader.app.*
import com.max.reader.app.message.Message
import com.max.reader.app.message.Pop
import com.max.reader.misc.safe
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.screens.article.details.ui.ArticleDetailsScreen
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.home.HomeScreen
import com.max.reader.screens.settings.SettingsState
import com.max.reader.ui.theme.AppTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_NewsReader)
        super.onCreate(savedInstanceState)
        // todo migrate at some point in the future
        @Suppress("DEPRECATION")
        window.setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val stateFlow = remember { appComponent(appMessages.asFlow()) }
            val state = stateFlow.collectAsState(context = Dispatchers.Main, initial = null)

            state.value?.render(appMessages::offer)
        }

        launch {
            closeAppCommands.collect {
                finishAfterTransition()
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        appMessages.offer(Pop)
    }

}

@Composable
private fun AppState.render(
    onMessage: (Message) -> Unit,
) {
    AppTheme(
        isDarkModeEnabled = isDarkModeEnabled
    ) {
        when (val screen = screen) {
            is ArticlesState -> HomeScreen(screen, onMessage)
            is SettingsState -> HomeScreen(this, onMessage)
            is ArticleDetailsState -> ArticleDetailsScreen(screen, onMessage)
            else -> error("unhandled branch $screen")
        }.safe
    }
}