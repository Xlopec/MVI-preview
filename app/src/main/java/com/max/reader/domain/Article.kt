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

package com.max.reader.domain

import java.net.URL
import java.util.*
import kotlin.contracts.contract

data class Article(
    val url: URL,
    val title: Title,
    val author: Author?,
    val description: Description?,
    val urlToImage: URL?,
    val published: Date,
    val isFavorite: Boolean
)

data class Title(val value: String) {

    companion object;

    init {
        require(isValid(value))
    }
}

data class Author(val value: String) {

    companion object;

    init {
        require(isValid(value))
    }
}

data class Description(val value: String) {

    companion object;

    init {
        require(isValid(value))
    }
}

fun Title.Companion.isValid(
    s: String?
): Boolean {
    contract {
        returns(true) implies (s is String)
    }

    return s.isNonEmpty()
}

fun Title.Companion.tryCreate(
    s: String?
) = if (isValid(s)) Title(s) else null

fun Author.Companion.isValid(
    s: String?
): Boolean {
    contract {
        returns(true) implies (s is String)
    }

    return s.isNonEmpty()
}

fun Author.Companion.tryCreate(
    s: String?
) = if (isValid(s)) Author(s) else null

fun Description.Companion.isValid(
    s: String?
): Boolean {
    contract {
        returns(true) implies (s is String)
    }

    return s.isNonEmpty()
}

fun Description.Companion.tryCreate(
    s: String?
) = if (isValid(s)) Description(s) else null

fun Article.toggleFavorite(): Article = copy(isFavorite = !isFavorite)

private fun String?.isNonEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNonEmpty is String)
    }

    return !isNullOrEmpty() && !isNullOrBlank()
}
