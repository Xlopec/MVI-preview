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

package com.oliynick.max.tea.core.debug.misc

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.debug.gson.GsonNotifyServer
import com.oliynick.max.tea.core.debug.session.DebugSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class TestDebugSession<M, S>(
    override val messages: Flow<M> = emptyFlow(),
    override val states: Flow<S> = emptyFlow()
) : DebugSession<M, S, JsonElement> {

    private val _packets = mutableListOf<GsonNotifyServer>()

    val packets: List<GsonNotifyServer> = _packets

    override suspend fun invoke(packet: GsonNotifyServer) {
        _packets += packet
    }

}