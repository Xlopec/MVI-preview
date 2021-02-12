@file:Suppress("FunctionName")

package com.max.reader.app

import android.os.Build
import com.max.reader.app.command.Command
import com.max.reader.app.env.Environment
import com.max.reader.app.message.Message
import com.max.reader.app.serialization.PersistentListSerializer
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.component.states
import com.oliynick.max.tea.core.debug.component.Component
import com.oliynick.max.tea.core.debug.component.URL
import com.oliynick.max.tea.core.debug.gson.GsonSerializer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow

fun Environment.AppComponent(
    initializer: Initializer<AppState, Command>,
): (Flow<Message>) -> Flow<AppState> {

    suspend fun resolve(command: Command) = this.resolve(command)

    fun update(
        message: Message,
        state: AppState,
    ) = this.update(message, state)

    // todo state persistence

    return Component(
        ComponentId("News Reader App: ${Build.MANUFACTURER} ${Build.MODEL}"),
        initializer,
        ::resolve,
        ::update,
        AppGsonSerializer(),
        scope = this,
        URL(host = "10.0.2.2")
    ).states()
}

private fun AppGsonSerializer() = GsonSerializer {
    registerTypeHierarchyAdapter(PersistentList::class.java, PersistentListSerializer)
}
