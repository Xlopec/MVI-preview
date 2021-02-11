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

import com.max.reader.domain.Article

sealed interface Navigation : Message

object NavigateToFeed : Navigation

object NavigateToFavorite : Navigation

object NavigateToTrending : Navigation

object NavigateToSettings : Navigation

object Pop : Navigation

data class NavigateToArticleDetails(
    val article: Article
) : Navigation
