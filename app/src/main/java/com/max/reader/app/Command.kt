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

package com.max.reader.app

import com.max.reader.domain.Article
import com.max.reader.screens.article.list.Query

sealed class Command

// App wide commands

object CloseApp : Command()

// Article details commands

sealed class ArticleDetailsCommand : Command()

data class DoOpenArticle(
    val article: Article,
) : ArticleDetailsCommand()

// Feed screen commands

sealed class ArticlesCommand : Command()

data class LoadArticlesByQuery(
    val id: ScreenId,
    val query: Query,
    val currentSize: Int,
    val resultsPerPage: Int,
) : ArticlesCommand()

data class SaveArticle(
    val article: Article,
) : ArticlesCommand()

data class RemoveArticle(
    val article: Article,
) : ArticlesCommand()

data class DoShareArticle(
    val article: Article,
) : ArticlesCommand()