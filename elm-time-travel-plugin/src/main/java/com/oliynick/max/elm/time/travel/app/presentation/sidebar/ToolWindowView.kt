package com.oliynick.max.elm.time.travel.app.presentation.sidebar

import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.changes
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.presentation.misc.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.Container
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import javax.swing.event.DocumentListener
import java.awt.Component as AwtComponent

class ToolWindowView(private val project: Project,
                     private val scope: CoroutineScope,
                     private val component: Component<PluginMessage, PluginState>,
                     private val uiEvents: Channel<PluginMessage>) : CoroutineScope by scope {

    private lateinit var panel: JPanel
    private lateinit var directoriesList: JList<File>
    private lateinit var removeDirectoryButton: JButton
    private lateinit var addDirectoryButton: JButton
    private lateinit var startButton: JButton
    private lateinit var portTextField: JTextField
    private lateinit var hostTextField: JTextField
    private lateinit var componentsPanel: JPanel

    private val directoriesListModel = DiffingListModel(FileDiffCallback)

    private val portListener = object : DefaultDocumentListener {
        override fun onValueUpdated(value: String) {
            uiEvents.offer(UpdatePort(value.toUIntOrNull() ?: return))
        }
    }

    private val hostListener = object : DefaultDocumentListener {
        override fun onValueUpdated(value: String) {
            uiEvents.offer(UpdateHost(value))
        }
    }

    val root: JPanel get() = panel

    init {

        directoriesList.cellRenderer = FileCellRenderer()
        directoriesList.model = directoriesListModel

        launch { component(uiEvents.consumeAsFlow()).collect { state -> render(state, uiEvents) } }
        launch { component.showProgressOnTransientState(project) }
    }

    private fun render(state: PluginState, messages: Channel<PluginMessage>) {
        directoriesListModel.swap(state.settings.classFiles)

        portTextField.isEnabled = state is Stopped
        hostTextField.isEnabled = portTextField.isEnabled

        when (state) {
            is Stopped -> render(state, messages)
            is Starting -> render(state)
            is Started -> render(state, messages)
            is Stopping -> render(state)
        }.safe
    }

    private fun render(state: Stopped, messages: Channel<PluginMessage>) {
        portTextField.setText(state.settings.serverSettings.port.toString(), portListener)
        hostTextField.setText(state.settings.serverSettings.host, hostListener)

        startButton.text = "Start"

        if (state.canStart) {
            startButton.setOnClickListenerEnabling { messages.offer(StartServer) }
        } else {
            startButton.removeMouseListenersDisabling()
        }

        removeDirectoryButton.setOnClickListenerEnabling {
            messages.offer(RemoveFiles(directoriesList.selectedValuesList))
        }

        addDirectoryButton.setOnClickListenerEnabling {
            project.chooseClassFiles { files -> messages.offer(AddFiles(files)) }
        }

        if (componentsPanel.componentCount == 0) {
            componentsPanel.add(EmptyComponentsView(component, scope.coroutineContext, project).root, "EmptyView")
        }
    }

    private fun render(state: Starting) {
        startButton.text = "Starting"
        startButton.removeMouseListenersDisabling()

        removeDirectoryButton.removeMouseListenersDisabling()
        addDirectoryButton.removeMouseListenersDisabling()
    }

    private fun render(state: Started, messages: Channel<PluginMessage>) {
        startButton.text = "Stop"
        startButton.setOnClickListenerEnabling { messages.offer(StopServer) }

        removeDirectoryButton.removeMouseListenersDisabling()
        addDirectoryButton.removeMouseListenersDisabling()

        if (state.debugState.components.size == 1) {
            componentsPanel.clearCancelling()

            val componentsTabPane = JBTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
                .also { tabPane -> tabPane.model = DefaultSingleSelectionModel() }

            componentsPanel.add(componentsTabPane)
        }

        if (state.debugState.components.isNotEmpty()) {
            val componentsTabPane = componentsPanel.getComponent(0) as JTabbedPane

            state.debugState.components
                .filter { e -> componentsTabPane.indexOfTab(e.key.id) == -1 }
                .forEach { (id, s) -> componentsTabPane.addTab(id.id, ComponentView(scope, component, s)._root) }
        }
    }

    private fun render(state: Stopping) {
        startButton.text = "Stopping"
        startButton.removeMouseListenersDisabling()

        removeDirectoryButton.removeMouseListenersDisabling()
        addDirectoryButton.removeMouseListenersDisabling()
    }

}

private fun JTextField.setText(text: String, listener: DocumentListener) {
    document.removeDocumentListener(listener)
    this.text = text
    document.addDocumentListener(listener)
}

private suspend fun Component<PluginMessage, PluginState>.showProgressOnTransientState(project: Project) {
    changes().filter { state -> state.isTransient }.collect { transientState ->
        runBackgroundableTask(transientState.progressDescription, project, false) {
            runBlocking { changes().first { state -> !state.isTransient } }
        }
    }
}

private val PluginState.isTransient inline get() = this is Starting || this is Stopping

private val ServerSettings.asHumanReadable: String inline get() = "$host:$port"

private val PluginState.progressDescription: String
    inline get() = when (this) {
        is Starting -> "Starting server on ${settings.serverSettings.asHumanReadable}"
        is Stopping -> "Stopping server on ${settings.serverSettings.asHumanReadable}"
        is Stopped, is Started -> throw IllegalStateException("Can't get description for $this")
    }

private fun AwtComponent.setOnClickListenerEnabling(l: (MouseEvent) -> Unit) {
    setOnClickListener(l)
    isEnabled = true
}

private fun AwtComponent.removeMouseListenersDisabling() {
    removeMouseListeners()
    isEnabled = false
}

fun Container.clearCancelling() {
    for (i in 0 until componentCount) {
        val c = getComponent(i)

        if (c is CoroutineScope) {
            // fixme, with CoroutineScope.cancel() it doesn't even compile
            c.coroutineContext[Job.Key]?.cancel()
        }

        remove(c)
    }
}