package com.oliynick.max.tea.core.debug.app.env

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.domain.resolver.*
import com.oliynick.max.tea.core.debug.app.domain.updater.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface Environment :
    Updater<Environment>,
    NotificationUpdater,
    UiUpdater,
    AppResolver<Environment>,
    HasMessageChannel,
    HasSystemProperties,
    HasProject,
    CoroutineScope

@Suppress("FunctionName")
fun Environment(
    properties: PropertiesComponent,
    project: Project
): Environment =
    object : Environment,
        Updater<Environment> by LiveUpdater(),
        NotificationUpdater by LiveNotificationUpdater,
        UiUpdater by LiveUiUpdater,
        AppResolver<Environment> by LiveAppResolver(),
        HasMessageChannel by HasMessagesChannel(),
        HasSystemProperties by HasSystemProperties(properties),
        HasProject by HasProject(project),
        CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Main) {}