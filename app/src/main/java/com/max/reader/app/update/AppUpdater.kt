package com.max.reader.app.update

import com.max.reader.app.*
import com.max.reader.app.command.Command
import com.max.reader.app.message.Message
import com.oliynick.max.tea.core.component.UpdateWith

interface AppUpdater<Env> {

    fun Env.update(
        message: Message,
        state: AppState
    ): UpdateWith<AppState, Command>

}