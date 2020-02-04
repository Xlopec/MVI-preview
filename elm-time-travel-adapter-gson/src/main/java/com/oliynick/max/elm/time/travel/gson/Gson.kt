@file:Suppress("FunctionName")

package com.oliynick.max.elm.time.travel.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import protocol.ComponentId
import protocol.ServerMessage
import java.util.*

private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

fun Gson(
    config: GsonBuilder.() -> Unit = {}
): Gson =
    GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .setDateFormat(DATE_FORMAT)
        .registerTypeHierarchyAdapter(ServerMessage::class.java, ServerMessageAdapter)
        .registerTypeAdapter(UUID::class.java, UUIDAdapter)
        .registerTypeAdapter(ComponentId::class.java, ComponentIdAdapter)
        .apply(config)
        .create()
