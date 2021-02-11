// used by OS
@file:Suppress("unused")

package com.max.reader.app

import android.app.Activity
import android.app.Application
import com.max.reader.app.command.CloseApp
import com.max.reader.app.env.Environment
import com.max.reader.app.message.Message
import com.max.reader.misc.unsafeLazy
import com.max.reader.ui.isDarkModeEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class AndroidApp : Application(), CoroutineScope by AppComponentScope {

    val environment by unsafeLazy {
        Environment(this, this)
    }

    val component by unsafeLazy { environment.AppComponent(AppInitializer(isDarkModeEnabled)) }

    val messages = BroadcastChannel<Message>(1)

}

private object AppComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Job() + Executors.newSingleThreadExecutor { r -> Thread(r, "App Scheduler") }
            .asCoroutineDispatcher()
}

inline val Activity.androidApp: AndroidApp
    get() = application as AndroidApp

inline val Activity.appComponent: (Flow<Message>) -> Flow<AppState>
    get() = androidApp.component

inline val Activity.appMessages: BroadcastChannel<Message>
    get() = androidApp.messages

inline val Activity.environment: Environment
    get() = androidApp.environment

inline val Activity.closeAppCommands: Flow<CloseApp>
    get() = androidApp.environment.closeCommands.asFlow()
