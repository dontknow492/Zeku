package com.ghost.zeku.utils

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import kotlinx.coroutines.launch


@Composable
fun Modifier.desktopDragScroll(state: ScrollableState): Modifier {
    return this.draggable(
        orientation = Orientation.Horizontal,
        state = rememberDraggableState { delta ->
            // dispatchRawDelta applies the pixel movement synchronously
            // directly to the Carousel's scroll state.
            // We use -delta because dragging left (negative delta)
            // means scrolling forward (positive scroll).
            state.dispatchRawDelta(-delta)
        }
    )
}


@Composable
fun Modifier.desktopCarouselBehaviors(
    isDesktop: Boolean,
    pagerState: PagerState
): Modifier = composed {
    if (isDesktop) {
        val scope = rememberCoroutineScope()

        this.draggable(
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { delta ->
                pagerState.dispatchRawDelta(-delta)
            },
            onDragStopped = { velocity ->
                // FIX: When the user lets go of the mouse, force the pager to snap!
                scope.launch {
                    pagerState.animateScrollToPage(
                        page = pagerState.targetPage,
                        // Optional: you can pass initialVelocity = velocity here if you want it to fling
                    )
                }
            }
        ).pointerHoverIcon(PointerIcon.Hand)
    } else {
        this
    }
}