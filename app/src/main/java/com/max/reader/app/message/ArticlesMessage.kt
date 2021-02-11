/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.max.reader.app.message

import com.max.reader.app.ScreenId
import com.max.reader.app.exception.AppException
import com.max.reader.domain.Article

sealed interface ArticlesMessage : ScreenMessage

data class LoadNextArticles(
    val id: ScreenId,
) : ArticlesMessage

data class LoadArticlesFromScratch(
    val id: ScreenId,
) : ArticlesMessage

data class RefreshArticles(
    val id: ScreenId,
) : ArticlesMessage

data class ToggleArticleIsFavorite(
    val id: ScreenId,
    val article: Article,
) : ArticlesMessage

data class ArticlesLoaded(
    val id: ScreenId,
    val articles: List<Article>,
    val hasMore: Boolean,
) : ArticlesMessage

data class ArticlesOperationException(
    val id: ScreenId?,
    val cause: AppException,
) : ArticlesMessage

data class ArticleUpdated(
    val article: Article,
) : ArticlesMessage

data class ShareArticle(
    val article: Article,
) : ArticlesMessage

data class OnQueryUpdated(
    val id: ScreenId,
    val query: String,
) : ArticlesMessage
