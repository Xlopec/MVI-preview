package com.max.reader.app.resolve

import com.max.reader.app.command.Command
import com.max.reader.app.message.Message

interface AppResolver<Env> {

    suspend fun Env.resolve(command: Command): Set<Message>

}