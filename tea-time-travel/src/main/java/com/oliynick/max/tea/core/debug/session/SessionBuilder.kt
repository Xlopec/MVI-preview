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

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.session

import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.component.URL
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*

/**
 * Function that for a given server settings creates a new connection
 * to a debug server
 *
 * @param M message type
 * @param S state type
 * @param J json type
 */
typealias SessionBuilder<M, S, J> = suspend (
    settings: ServerSettings<M, S, J>,
    session: suspend DebugSession<M, S, J>.() -> Unit,
) -> Unit

/**
 * Creates a new web socket session using supplied settings
 *
 * @param settings server settings
 * @param block lambda to interact with [session][DebugSession]
 * @param M message type
 * @param S state type
 * @param J json type
 */
suspend inline fun <reified M, reified S, J> WebSocketSession(
    settings: ServerSettings<M, S, J>,
    crossinline block: suspend DebugSession<M, S, J>.() -> Unit,
) = HttpClient.ws(// todo add timeout
    method = HttpMethod.Get,
    host = settings.url.host,
    port = settings.url.port,
    block = {
        DebugWebSocketSession(
            M::class.java,
            S::class.java,
            settings,
            this
        ).apply { block() }
    }
)

@PublishedApi
internal val HttpClient by lazy { HttpClient { install(WebSockets) } }

@PublishedApi
internal val Localhost by lazy(::URL)
