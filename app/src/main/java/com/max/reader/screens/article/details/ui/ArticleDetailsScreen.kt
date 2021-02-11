@file:Suppress("FunctionName")

package com.max.reader.screens.article.details.ui

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import com.max.reader.app.message.Message
import com.max.reader.app.message.Pop
import com.max.reader.domain.Article
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.app.message.OpenInBrowser
import com.max.reader.ui.InsetAwareTopAppBar

@Composable
fun ArticleDetailsScreen(
    screen: ArticleDetailsState,
    onMessage: (Message) -> Unit,
) {

    val context = LocalContext.current

    val view = remember {
        AppWebView(context)
    }
    // TODO implement back navigation inside web view
    Scaffold(
        topBar = {

            InsetAwareTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(onClick = { onMessage(Pop) }) {
                        Icon(
                            contentDescription = "Close",
                            imageVector = /*if (view.canGoBack()) Icons.Default.ArrowBack else*/ Icons.Default.Close,
                        )
                    }

                },
                title = {
                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        text = screen.article.title.value
                    )
                },
                actions = {
                    IconButton(onClick = { onMessage(OpenInBrowser(screen.id)) }) {
                        Icon(
                            contentDescription = "Open in Browser",
                            imageVector = Icons.Default.OpenInBrowser
                        )
                    }
                }
            )
        }, bodyContent = { innerPadding ->
            ArticleDetailsContent(Modifier.padding(innerPadding), screen.article, view)
        })
}

@Composable
private fun ArticleDetailsContent(
    modifier: Modifier,
    article: Article,
    view: WebView,
) {
    AndroidView(
        viewBlock = { view },
        modifier = modifier
            .fillMaxSize()
            .onKeyEvent { event ->
                event.key == Key.Back && view
                    .canGoBack()
                    .also { if (it) view.goBack() }
            }
    ) { webView ->
        webView.loadUrl(article.url.toExternalForm())
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun AppWebView(
    context: Context,
) = WebView(context).apply {
    settings.javaScriptEnabled = true
    settings.setSupportZoom(true)
    webChromeClient = WebChromeClient()
    webViewClient = WebViewClient()
}
