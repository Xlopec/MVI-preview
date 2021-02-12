@file:Suppress("FunctionName")

package com.max.reader.app

import com.max.reader.app.env.Environment
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.states
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

fun Environment.AppComponent(
    initializer: Initializer<AppState, Command>
): (Flow<Message>) -> Flow<AppState> {

    suspend fun resolve(command: Command) = this.resolve(command)

    fun update(
        message: Message,
        state: AppState
    ) = this.update(message, state)

    // todo state persistence

    return Component(
        initializer,
        ::resolve,
        ::update,
        this,
        Dispatchers.IO
    ).states()

}
