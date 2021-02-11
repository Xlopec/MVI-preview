@file:Suppress("FunctionName")

package com.max.reader.screens.article.list.ui

import androidx.compose.animation.asDisposableClock
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.animation.defaultFlingConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.textButtonColors
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalAnimationClock
import androidx.compose.ui.text.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.max.reader.app.ScreenId
import com.max.reader.app.message.*
import com.max.reader.domain.Article
import com.max.reader.domain.Author
import com.max.reader.domain.Description
import com.max.reader.domain.Title
import com.max.reader.misc.safe
import com.max.reader.screens.article.list.*
import com.max.reader.screens.article.list.ArticlesState.TransientState.*
import com.max.reader.screens.article.list.QueryType.*
import com.max.reader.ui.theme.ThemedPreview
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview as Render

@Composable
fun ArticlesScreen(
    modifier: Modifier,
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val listState = listState(id = state.id)

        when (val transientState = state.transientState) {
            is Exception -> ArticlesExceptionContent(
                id = state.id,
                state = listState,
                query = state.query,
                articles = state.articles,
                cause = transientState.th,
                onMessage = onMessage
            )
            is Loading -> ArticlesLoadingContent(
                state = listState,
                id = state.id,
                query = state.query,
                articles = state.articles,
                onMessage = onMessage
            )
            is Refreshing,
            is Preview,
            -> ArticlesPreviewContent(
                state = listState,
                id = state.id,
                query = state.query,
                articles = state.articles,
                onMessage = onMessage
            )
        }.safe
    }
}

@Composable
private fun ArticlesProgress(
    modifier: Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ArticlesLoadingContent(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    articles: List<Article>,
    onMessage: (Message) -> Unit,
) {
    ArticlesContent(
        state = state,
        id = id,
        query = query,
        onMessage = onMessage
    ) {

        if (articles.isEmpty()) {
            item {
                ArticlesProgress(modifier = Modifier.fillParentMaxSize())
            }
        } else {
            ArticlesContentNonEmptyImpl(
                id = id,
                query = query,
                articles = articles,
                onMessage = onMessage
            )

            item {
                Spacer(modifier = Modifier.preferredHeight(16.dp))
                ArticlesProgress(modifier = Modifier.fillMaxWidth())
            }
        }

    }
}

@Composable
private fun ArticlesExceptionContent(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    articles: List<Article>,
    cause: Throwable,
    onMessage: (Message) -> Unit,
) {
    ArticlesContent(state, id, query, onMessage) {

        if (articles.isEmpty()) {
            item {
                ArticlesError(
                    modifier = Modifier.fillParentMaxSize(),
                    id = id,
                    message = cause.toReadableMessage(),
                    onMessage = onMessage
                )
            }
        } else {
            ArticlesContentNonEmptyImpl(id, query, articles, onMessage) {}

            item {
                ArticlesError(
                    modifier = Modifier.fillMaxWidth(),
                    id = id,
                    message = cause.toReadableMessage(),
                    onMessage = onMessage
                )
            }
        }
    }
}

@Composable
private fun ArticlesPreviewContent(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    articles: List<Article>,
    onMessage: (Message) -> Unit,
) {
    if (articles.isEmpty()) {
        ArticlesContentEmpty(state, id, query, onMessage)
    } else {
        ArticlesContentNonEmpty(state, id, query, articles, onMessage)
    }
}

@Composable
private fun ArticlesContent(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    onMessage: (Message) -> Unit,
    children: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            ArticleSearchHeader(
                id = id,
                query = query,
                onMessage = onMessage
            )

            Spacer(modifier = Modifier.preferredHeight(16.dp))
        }

        children()
    }
}

@Composable
private fun ArticlesContentEmpty(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    onMessage: (Message) -> Unit,
) {
    ArticlesContent(state, id, query, onMessage) {

        item {
            Message(
                modifier = Modifier.fillParentMaxSize(),
                message = "No articles",
                actionText = "Reload",
                onClick = {
                    onMessage(LoadArticlesFromScratch(id))
                }
            )
        }
    }
}

private fun LazyListScope.ArticlesContentNonEmptyImpl(
    id: ScreenId,
    query: Query,
    articles: List<Article>,
    onMessage: (Message) -> Unit,
    onLastElement: () -> Unit = { onMessage(LoadNextArticles(id)) },
) {
    require(articles.isNotEmpty())

    item {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            text = query.toScreenTitle(),
            style = typography.subtitle1
        )

        Spacer(modifier = Modifier.preferredHeight(16.dp))
    }

    itemsIndexed(articles) { index, article ->
        Column {
            ArticleItem(
                screenId = id,
                article = article,
                onMessage = onMessage
            )

            if (index != articles.lastIndex) {
                Spacer(modifier = Modifier.preferredHeight(16.dp))
            }

            if (index == articles.lastIndex) {
                DisposableEffect(Unit) {
                    onLastElement()
                    onDispose { }
                }
            }
        }
    }
}

@Composable
private fun ArticlesContentNonEmpty(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    articles: List<Article>,
    onMessage: (Message) -> Unit,
) {
    ArticlesContent(state, id, query, onMessage) {
        ArticlesContentNonEmptyImpl(id, query, articles, onMessage)
    }
}

@Composable
private fun ArticleImage(
    imageUrl: URL?,
) {
    Surface(
        modifier = Modifier
            .preferredHeight(200.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        color = colors.onSurface.copy(alpha = 0.2f)
    ) {

        if (imageUrl != null) {

            CoilImage(
                modifier = Modifier.fillMaxWidth(),
                data = imageUrl.toExternalForm(),
                fadeIn = true,
                contentScale = ContentScale.Crop,
                contentDescription = "Article's Image"
            )
        }
    }
}

@Composable
private fun ArticleItem(
    screenId: ScreenId,
    article: Article,
    onMessage: (Message) -> Unit,
) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = { onMessage(NavigateToArticleDetails(article)) })
        ) {

            ArticleImage(imageUrl = article.urlToImage)

            Spacer(modifier = Modifier.height(8.dp))

            ArticleContents(article = article)

            Spacer(modifier = Modifier.height(4.dp))

            ArticleActions(onMessage, article, screenId)
        }
    }
}

@Composable
private fun ArticleContents(
    article: Article,
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {

        Text(
            text = article.title.value,
            style = typography.h6
        )

        if (article.author != null) {
            Text(
                text = article.author.value,
                style = typography.subtitle2
            )
        }

        Text(
            text = "Published on ${DateFormatter.format(article.published)}",
            style = typography.body2
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = article.description?.value ?: "No description",
            style = typography.body2
        )
    }
}

@Composable
private fun ArticleActions(
    onMessage: (Message) -> Unit,
    article: Article,
    screenId: ScreenId,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {

        IconButton(
            onClick = { onMessage(ShareArticle(article)) }
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share"
            )
        }

        IconButton(
            onClick = { onMessage(ToggleArticleIsFavorite(screenId, article)) }
        ) {
            Icon(
                imageVector = if (article.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (article.isFavorite) "Remove from favorite" else "Add to favorite"
            )
        }
    }
}

@Composable
private fun ArticlesError(
    modifier: Modifier,
    id: ScreenId,
    message: String,
    onMessage: (Message) -> Unit,
) {
    Message(
        modifier,
        "Failed to load articles, message: '${message.decapitalize(Locale.getDefault())}'",
        "Retry"
    ) {
        onMessage(LoadArticlesFromScratch(id))
    }
}

@Composable
private fun Message(
    modifier: Modifier,
    message: String,
    actionText: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center
        )

        TextButton(
            colors = textButtonColors(contentColor = colors.onSurface),
            onClick = onClick
        ) {
            Text(text = actionText)
        }
    }
}

@Composable
private fun ArticleSearchHeader(
    modifier: Modifier = Modifier,
    id: ScreenId,
    query: Query,
    onMessage: (Message) -> Unit,
) {
    Card(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {

        val controllerRef = remember<Ref<SoftwareKeyboardController>> { Ref() }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = query.type.toSearchHint(), style = typography.subtitle2) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            value = query.input,
            maxLines = 1,
            onTextInputStarted = { controller ->
                controllerRef.value = controller
            },
            keyboardActions = KeyboardActions(
                onSearch = {
                    onMessage(LoadArticlesFromScratch(id))
                    controllerRef.value?.hideSoftwareKeyboard()
                }
            ),
            backgroundColor = colors.surface,
            textStyle = typography.subtitle2,
            trailingIcon = {
                IconButton(
                    onClick = { onMessage(LoadArticlesFromScratch(id)) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            },
            onValueChange = { query -> onMessage(OnQueryUpdated(id, query)) }
        )
    }
}

@Composable
private fun listState(
    id: ScreenId,
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0,
    interactionState: InteractionState? = null,
): LazyListState {
    val clock = LocalAnimationClock.current.asDisposableClock()
    val config = defaultFlingConfig()
    val idToListState = remember { mutableMapOf<ScreenId, LazyListState>() }

    return remember(id) {
        idToListState.getOrPut(id) {
            LazyListState(
                initialFirstVisibleItemIndex,
                initialFirstVisibleItemScrollOffset,
                interactionState,
                config,
                clock
            )
        }
    }
}

@Composable
@Render("Articles search input field")
private fun ArticleSearchHeaderPreview() {
    ThemedPreview {
        ArticleSearchHeader(
            id = UUID.randomUUID(),
            query = Query("some input text", Trending),
            onMessage = {}
        )
    }
}

@Composable
@Render("Article item")
private fun ArticleItemPreview() {
    ThemedPreview {
        ArticleItem(
            screenId = UUID.randomUUID(),
            article = ArticleSamplePreview,
            onMessage = {}
        )
    }
}

@Composable
@Render("Articles bottom action menu")
private fun ArticleActionsPreview() {
    ThemedPreview {
        ArticleActions(
            onMessage = {},
            article = ArticleSamplePreview,
            screenId = UUID.randomUUID()
        )
    }
}

@Composable
@Render("Messages preview")
private fun MessagePreview() {
    ThemedPreview {
        Message(
            modifier = Modifier,
            message = "Oops, something went wrong",
            actionText = "Retry",
            onClick = {}
        )
    }
}

@Composable
@Render("Articles exception non empty list preview")
private fun ArticlesExceptionContentPreview() {
    ThemedPreview {
        ArticlesExceptionContent(
            state = rememberLazyListState(),
            id = UUID.randomUUID(),
            query = Query("Android", Regular),
            articles = listOf(ArticleSamplePreview),
            cause = RuntimeException("Some exception"),
            onMessage = { }
        )
    }
}

@Composable
@Render("Articles loading list preview")
private fun ArticlesLoadingContentPreview() {
    ThemedPreview {
        ArticlesLoadingContent(
            state = rememberLazyListState(),
            id = UUID.randomUUID(),
            query = Query("Android", Regular),
            articles = listOf(ArticleSamplePreview),
            onMessage = { }
        )
    }
}

private val DateFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat("dd MMM' at 'hh:mm", Locale.getDefault())
}

private fun Throwable.toReadableMessage() =
    message?.decapitalize(Locale.getDefault()) ?: "unknown exception"

private fun Query.toScreenTitle(): String =
    when (type) {
        Regular -> "Feed"
        Favorite -> "Favorite"
        Trending -> "Trending"
    }

private fun QueryType.toSearchHint(): String =
    when (this) {
        Regular -> "Search in articles"
        Favorite -> "Search in favorite"
        Trending -> "Search in trending"
    }

private val ArticleSamplePreview = Article(
    url = URL("https://www.google.com"),
    title = Title("Jetpack Compose app"),
    author = Author("Max Oliinyk"),
    description = Description("Let your imagination fly! Modifiers let you modify your composable " +
            "in a very flexible way. For example, if you wanted to add some outer spacing, change " +
            "the background color of the composable, and round the corners of the Row, you could " +
            "use the following code"),
    published = Date(),
    isFavorite = true,
    urlToImage = URL("https://miro.medium.com/max/4000/1*Ir8CdY5D5Do5R_22Vo3uew.png")
)
