package com.oliynick.max.tea.core.debug.app.component.cms

import com.oliynick.max.tea.core.debug.app.domain.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.ComponentMapping
import com.oliynick.max.tea.core.debug.app.domain.DebugState
import com.oliynick.max.tea.core.debug.app.domain.Filter
import com.oliynick.max.tea.core.debug.app.domain.FilterOption
import com.oliynick.max.tea.core.debug.app.domain.FilteredSnapshot
import com.oliynick.max.tea.core.debug.app.domain.Invalid
import com.oliynick.max.tea.core.debug.app.domain.OriginalSnapshot
import com.oliynick.max.tea.core.debug.app.domain.Predicate
import com.oliynick.max.tea.core.debug.app.domain.ServerSettings
import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.domain.SnapshotId
import com.oliynick.max.tea.core.debug.app.domain.Valid
import com.oliynick.max.tea.core.debug.app.domain.Value
import com.oliynick.max.tea.core.debug.app.domain.applyTo
import com.oliynick.max.tea.core.debug.app.misc.map
import com.oliynick.max.tea.core.debug.app.misc.mapNotNull
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import protocol.ComponentId

fun Started.update(
    debugState: DebugState
) = copy(debugState = debugState)

fun Started.removeSnapshots(
    id: ComponentId,
    snapshots: Set<SnapshotId>
) = updateComponents { mapping -> mapping.put(id, debugState.component(id).removeSnapshots(snapshots)) }

fun Started.removeSnapshots(
    id: ComponentId
) = updateComponents { mapping -> mapping.put(id, debugState.component(id).removeSnapshots()) }

fun PluginState.updateSettings(
    how: Settings.() -> Settings
) = when (this) {
    is Stopped -> copy(settings = settings.run(how))
    is Starting -> copy(settings = settings.run(how))
    is Started -> copy(settings = settings.run(how))
    is Stopping -> copy(settings = settings.run(how))
}

fun Stopped.update(
    serverSettings: ServerSettings
) = copy(settings = settings.copy(serverSettings = serverSettings))

inline fun Stopped.updateServerSettings(
    how: ServerSettings.() -> ServerSettings
): ServerSettings =
    settings.serverSettings.run(how)

inline fun Started.updateComponents(
    how: (mapping: ComponentMapping) -> ComponentMapping
) =
    update(debugState.copy(components = how(debugState.components)))

fun Started.updateComponent(
    id: ComponentId,
    how: (mapping: ComponentDebugState) -> ComponentDebugState?
) =
    update(debugState.updateComponent(id, how))

inline fun DebugState.updateComponent(
    id: ComponentId,
    crossinline how: (mapping: ComponentDebugState) -> ComponentDebugState?
) =
    copy(components = components.builder().also { m -> m.computeIfPresent(id) { _, s -> how(s) } }.build())

fun Started.snapshot(
    componentId: ComponentId,
    snapshotId: SnapshotId
): OriginalSnapshot = debugState.components[componentId]?.snapshots?.first { s -> s.meta.id == snapshotId }
    ?: error("Couldn't find a snapshot $snapshotId for component $componentId, available components ${debugState.components}")

fun ComponentDebugState.appendSnapshot(
    snapshot: OriginalSnapshot,
    state: Value
): ComponentDebugState {

    val filtered = when (val validatedPredicate = filter.predicate) {
        is Valid -> snapshot.filteredBy(validatedPredicate.t)
        is Invalid, null -> snapshot.toFiltered()
    }

    return copy(
        snapshots = snapshots.add(snapshot),
        filteredSnapshots = filtered?.let(filteredSnapshots::add) ?: filteredSnapshots,
        state = state
    )
}

fun ComponentDebugState.removeSnapshots(
    ids: Set<SnapshotId>
): ComponentDebugState =
    copy(
        snapshots = snapshots.removeAll { s -> s.meta.id in ids },
        filteredSnapshots = filteredSnapshots.removeAll { s -> s.meta.id in ids }
    )

fun ComponentDebugState.removeSnapshots(): ComponentDebugState =
    copy(
        snapshots = persistentListOf(),
        filteredSnapshots = persistentListOf()
    )

fun DebugState.component(
    id: ComponentId
) = components[id] ?: notifyUnknownComponent(id)

fun Started.updateFilter(
    id: ComponentId,
    filterInput: String,
    ignoreCase: Boolean,
    option: FilterOption
) = updateComponent(id) { s ->

    val filter = Filter.new(filterInput, option, ignoreCase)
    val filtered = when (val validatedPredicate = filter.predicate) {
        is Valid -> s.snapshots.filteredBy(validatedPredicate.t)
        is Invalid -> s.filteredSnapshots
        null -> s.snapshots.toFiltered()
    }

    s.copy(filter = filter, filteredSnapshots = filtered)
}

fun PersistentList<OriginalSnapshot>.filteredBy(
    predicate: Predicate
): PersistentList<FilteredSnapshot> =
    mapNotNull { o -> o.filteredBy(predicate) }

private fun PersistentList<OriginalSnapshot>.toFiltered(): PersistentList<FilteredSnapshot> =
    map { o -> o.toFiltered() }

private fun OriginalSnapshot.toFiltered() =
    FilteredSnapshot.ofBoth(
        meta,
        message,
        state
    )

private fun OriginalSnapshot.filteredBy(
    predicate: Predicate
): FilteredSnapshot? {

    val m = applyTo(message, predicate)
    val s = applyTo(state, predicate)

    return when {
        m != null && s != null -> FilteredSnapshot.ofBoth(meta, m, s)
        m != null -> FilteredSnapshot.ofMessage(meta, m)
        s != null -> FilteredSnapshot.ofState(meta, s)
        else -> null
    }
}

private fun notifyUnknownComponent(
    id: ComponentId
): Nothing =
    throw IllegalArgumentException("Unknown component $id")
