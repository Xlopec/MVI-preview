@file:Suppress("FunctionName")
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core

import com.oliynick.max.tea.core.component.Resolver
import com.oliynick.max.tea.core.component.Updater
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted

/**
 * Environment is an application component responsible for holding application dependencies
 *
 * @param initializer initializer to be used to provide initial values for application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param io coroutine dispatcher to be used in [resolver]
 * @param computation coroutine dispatcher to be used in [updater]
 * @param M message type
 * @param S state type
 * @param C command type
 */
data class Env<M, S, C>(
    val initializer: Initializer<S, C>,
    val resolver: Resolver<C, M>,
    val updater: Updater<M, S, C>,
    // todo: group to reduce number of arguments
    val scope: CoroutineScope,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val computation: CoroutineDispatcher = Dispatchers.Unconfined,
    val shareOptions: ShareOptions = ShareStateWhileSubscribed,
)

data class ShareOptions(
    val started: SharingStarted,
    val replay: UInt,
)

@UnstableApi
val ShareStateWhileSubscribed = ShareOptions(SharingStarted.WhileSubscribed(), 1U)
