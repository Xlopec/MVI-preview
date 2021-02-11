package com.max.reader.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val RefreshDistance = 80.dp

@Composable
fun RefreshIndicator(
    isRefreshing: Boolean,
    @FloatRange(from = .0, to = 1.0) progress: Float,
) {
    require(progress in .0..1.0) { "Value was out of range ${.0..1.0}, value: $progress" }
    Surface(elevation = 10.dp, shape = CircleShape) {

        val modifier = Modifier
            .preferredSize(36.dp)
            .padding(8.dp)

        val color = MaterialTheme.colors.onSurface

        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = modifier,
                color = color
            )
        } else {
            CircularProgressIndicator(
                progress = progress,
                modifier = modifier,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToRefreshLayout(
    refreshingState: Boolean,
    onRefresh: () -> Unit,
    enabled: Boolean = true,
    refreshIndicator: @Composable (Boolean, Float) -> Unit = { isRefreshing, progress ->
        RefreshIndicator(isRefreshing, progress)
    },
    content: @Composable () -> Unit,
) {
    val refreshDistance = with(LocalDensity.current) { RefreshDistance.toPx() }
    val state = rememberSwipeableState(refreshingState) { newValue ->
        // compare both copies of the swipe state before calling onRefresh(). This is a workaround.
        if (newValue && !refreshingState) onRefresh()
        true
    }

    Box(
        modifier = Modifier.swipeable(
            enabled = enabled,
            state = state,
            anchors = mapOf(
                -refreshDistance to false,
                refreshDistance to true
            ),
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Vertical
        )
    ) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = state.offset.value
                    .takeUnless(Float::isNaN)
                    ?.let { with(LocalDensity.current) { it.toDp() } } ?: 0.dp)
        ) {
            if (!state.offset.value.isNaN() && state.offset.value != -refreshDistance) {
                refreshIndicator(refreshingState,
                    (state.offset.value / refreshDistance).coerceIn(0f, 1f))
            }
        }

        // TODO (https://issuetracker.google.com/issues/164113834): This state->event trampoline is a
        //  workaround for a bug in the SwipableState API. Currently, state.value is a duplicated
        //  source of truth of refreshingState.
        DisposableEffect(refreshingState) {
            state.animateTo(refreshingState)
            onDispose {}
        }
    }
}
