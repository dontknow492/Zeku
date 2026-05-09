package com.ghost.zeku.presentation.components.media

// Assumed imports for your list card (Adjust packages as needed)
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridCells.Adaptive
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference
import com.ghost.zeku.presentation.common.isDesktop
import com.ghost.zeku.presentation.common.rememberPlatformConfiguration
import com.ghost.zeku.presentation.components.media.list.ListCardConfig
import com.ghost.zeku.presentation.components.media.list.ListCardShimmer
import com.ghost.zeku.presentation.components.media.list.MediaListCard
import com.ghost.zeku.presentation.components.media.list.toMediaListUiData
import com.ghost.zeku.presentation.components.media.poster.MediaPosterCard
import com.ghost.zeku.presentation.components.media.poster.PosterCardShimmer
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.toMediaPosterUiData
import com.ghost.zeku.presentation.components.section.EmptyMediaState
import com.ghost.zeku.presentation.components.section.FullScreenError
import com.ghost.zeku.utils.desktopDragScroll
import kotlinx.serialization.Serializable



@Serializable
enum class GridStyle {
    Adaptive, Fixed
}


fun GridStyle.toGridCells(
    minSize: Dp,
    count: Int
): GridCells {
    return when (this) {
        GridStyle.Adaptive -> Adaptive(minSize)
        GridStyle.Fixed -> Fixed(count)
    }
}

/**
 * Defines how the media collection should be presented.
 * This encapsulates the layout type, variant, and configurations.
 */

@Serializable
enum class MediaDisplayMode {
    PosterGrid,
    List
}


@Composable
fun MediaGrid(
    displayPreferences: MediaDisplayPreference,
    pagingItems: LazyPagingItems<Media>,
    onMediaAction: OnMediaAction,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    overscrollEffect: OverscrollEffect? = rememberOverscrollEffect(),
) {

    val isDesktop = rememberPlatformConfiguration().isDesktop

    val gridState = rememberLazyGridState()

    val displayMode = displayPreferences.mode

    val columns = when (displayMode) {
        MediaDisplayMode.PosterGrid -> {
            displayPreferences.gridStyle.toGridCells(
                minSize = displayPreferences.gridMinSize,
                count = displayPreferences.gridCount
            )
        }

        MediaDisplayMode.List -> Fixed(1)
    }

    val spacing = when (displayMode) {
        MediaDisplayMode.PosterGrid -> displayPreferences.gridSpacing
        MediaDisplayMode.List -> displayPreferences.listSpacing
    }

    val refreshState = pagingItems.loadState.refresh

    val hasItems = pagingItems.itemSnapshotList.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {

        // =========================================================
        // GRID
        // =========================================================

        LazyVerticalGrid(
            columns = columns,

            state = gridState,

            modifier = Modifier.fillMaxSize()
                .then(if (isDesktop) Modifier.desktopDragScroll(state = gridState) else Modifier)
                .pointerHoverIcon(
                    if (isDesktop) PointerIcon.Hand else PointerIcon.Default
                ),

            contentPadding = contentPadding,

            reverseLayout = reverseLayout,

            horizontalArrangement = Arrangement.spacedBy(spacing),

            verticalArrangement = Arrangement.spacedBy(spacing),

            flingBehavior = flingBehavior,

            userScrollEnabled = userScrollEnabled,

            overscrollEffect = overscrollEffect
        ) {

            // =========================================================
            // INITIAL LOADING SHIMMERS
            // =========================================================

            if (refreshState is LoadState.Loading && !hasItems) {

                items(12) {

                    LoadingItem(
                        displayMode = displayMode,
                        listConfig = displayPreferences.listConfig,
                        posterConfig = displayPreferences.posterConfig
                    )
                }
            }

            // =========================================================
            // CONTENT
            // =========================================================

            else {

                items(
                    count = pagingItems.itemCount,
                    key = { index ->
                        pagingItems[index]?.id ?: index
                    }
                ) { index ->

                    val item = pagingItems[index]

                    if (item != null) {

                        when (displayMode) {

                            MediaDisplayMode.PosterGrid -> {

                                MediaPosterCard(
                                    data = item.toMediaPosterUiData(),

                                    layout = displayPreferences.posterLayout,

                                    config = displayPreferences.posterConfig,

                                    onAction = onMediaAction,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                )
                            }

                            MediaDisplayMode.List -> {

                                MediaListCard(
                                    data = item.toMediaListUiData(),

                                    layout = displayPreferences.listCardLayout,

                                    config = displayPreferences.listConfig,

                                    onAction = onMediaAction,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                )
                            }
                        }
                    }
                }

                // =========================================================
                // APPEND LOADING
                // =========================================================

                if (pagingItems.loadState.append is LoadState.Loading) {

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        PaginationLoader()
                    }
                }

                // =========================================================
                // APPEND ERROR
                // =========================================================

                if (pagingItems.loadState.append is LoadState.Error) {

                    val error =
                        (pagingItems.loadState.append as LoadState.Error).error

                    item(span = { GridItemSpan(maxLineSpan) }) {

                        PaginationError(
                            error = error,
                            onRetry = pagingItems::retry
                        )
                    }
                }
            }
        }

        // =========================================================
        // FULLSCREEN ERROR
        // =========================================================

        if (refreshState is LoadState.Error && !hasItems) {

            FullScreenError(
                error = refreshState.error,
                onRetry = pagingItems::retry,
                modifier = Modifier.fillMaxSize()
            )
        }

        // =========================================================
        // EMPTY STATE
        // =========================================================

        if (
            refreshState is LoadState.NotLoading &&
            !hasItems
        ) {

            EmptyMediaState(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
private fun LoadingItem(
    displayMode: MediaDisplayMode,
    listConfig: ListCardConfig,
    posterConfig: PosterConfig,
) {
    when (displayMode) {
        MediaDisplayMode.PosterGrid -> PosterCardShimmer(posterConfig)
        MediaDisplayMode.List -> ListCardShimmer(listConfig)
    }
}


@Composable
private fun PaginationLoader(
    modifier: Modifier = Modifier,
    message: String = "Loading more..."
) {

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),

        shape = RoundedCornerShape(24.dp),

        color = MaterialTheme.colorScheme.surfaceContainerLow,

        tonalElevation = 1.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),

            horizontalArrangement = Arrangement.Center,

            verticalAlignment = Alignment.CenterVertically
        ) {

            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.5.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PaginationError(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.elevatedCardColors(
            containerColor =
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            // =====================================================
            // ERROR ICON
            // =====================================================

            Surface(
                shape = CircleShape,

                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
            ) {

                Icon(
                    imageVector = Icons.Rounded.CloudOff,

                    contentDescription = null,

                    tint = MaterialTheme.colorScheme.error,

                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // =====================================================
            // TEXT
            // =====================================================

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Couldn't load more",

                    style = MaterialTheme.typography.titleSmall,

                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text =
                        error.localizedMessage
                            ?.takeIf { it.isNotBlank() }
                            ?: "Please check your internet connection.",

                    style = MaterialTheme.typography.bodySmall,

                    color = MaterialTheme.colorScheme.onSurfaceVariant,

                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // =====================================================
            // RETRY BUTTON
            // =====================================================

            FilledTonalButton(
                onClick = onRetry,

                shape = RoundedCornerShape(14.dp),

                contentPadding = PaddingValues(
                    horizontal = 14.dp,
                    vertical = 10.dp
                )
            ) {

                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Retry")
            }
        }
    }
}