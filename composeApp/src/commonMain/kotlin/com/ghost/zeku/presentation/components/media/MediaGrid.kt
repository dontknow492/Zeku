package com.ghost.zeku.presentation.components.media

// Assumed imports for your list card (Adjust packages as needed)
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridCells.Adaptive
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference
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
import kotlinx.serialization.Serializable


@Serializable
private enum class GridUiState {
    LOADING, ERROR, EMPTY, CONTENT
}

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
    val state = rememberLazyGridState()
    val refreshLoadState = pagingItems.loadState.refresh
    val isListEmpty = pagingItems.itemCount == 0

    val displayMode = displayPreferences.mode


    // 🌟 Auto-adjust columns based on Display Mode
    // A Grid with Fixed(1) behaves exactly like a LazyColumn!
    val actualColumns = when (displayMode) {
        MediaDisplayMode.PosterGrid -> displayPreferences.gridStyle.toGridCells(
            minSize = displayPreferences.gridMinSize,
            count = displayPreferences.gridCount
        )

        MediaDisplayMode.List -> Fixed(1)
    }

    val actualSpacing = when (displayMode) {
        MediaDisplayMode.PosterGrid -> displayPreferences.gridSpacing
        MediaDisplayMode.List -> displayPreferences.listSpacing
    }

    val verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.spacedBy(actualSpacing, Alignment.Top) else Arrangement.spacedBy(
            actualSpacing,
            Alignment.Bottom
        )
    val horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(actualSpacing, Alignment.Start)


    // 🌟 1. Determine the current UI State
    val currentUiState = when (refreshLoadState) {
        is LoadState.Loading if isListEmpty -> GridUiState.LOADING
        is LoadState.Error if isListEmpty -> GridUiState.ERROR
        is LoadState.NotLoading if isListEmpty -> GridUiState.EMPTY
        else -> GridUiState.CONTENT
    }

    Crossfade(
        targetState = currentUiState,
        label = "MediaGridStateTransition",
        modifier = modifier
    ) { targetState ->

        when (targetState) {
            GridUiState.LOADING -> {
                LazyVerticalGrid(
                    columns = actualColumns,
                    state = state,
                    contentPadding = contentPadding,
                    reverseLayout = reverseLayout,
                    horizontalArrangement = horizontalArrangement,
                    verticalArrangement = verticalArrangement,
                    userScrollEnabled = false,
                ) {
                    items(count = 12) {
                        LoadingItem(
                            displayPreferences.mode,
                            displayPreferences.listConfig,
                            displayPreferences.posterConfig
                        )
                    }
                }
            }

            GridUiState.ERROR -> {
                val error = (refreshLoadState as? LoadState.Error)?.error ?: Exception("Unknown Error")
                FullScreenError(
                    error = error,
                    onRetry = { pagingItems.retry() }
                )
            }

            GridUiState.EMPTY -> {
                EmptyMediaState()
            }

            GridUiState.CONTENT -> {
                LazyVerticalGrid(
                    columns = actualColumns,
                    state = state,
                    contentPadding = contentPadding,
                    reverseLayout = reverseLayout,
                    horizontalArrangement = horizontalArrangement,
                    verticalArrangement = verticalArrangement,
                    flingBehavior = flingBehavior,
                    userScrollEnabled = userScrollEnabled,
                    overscrollEffect = overscrollEffect,
                ) {
                    // Prepend Loading/Error
                    if (pagingItems.loadState.prepend is LoadState.Loading) {
                        item(span = { GridItemSpan(maxLineSpan) }) { PaginationLoader() }
                    }
                    if (pagingItems.loadState.prepend is LoadState.Error) {
                        val error = (pagingItems.loadState.prepend as LoadState.Error).error
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            PaginationError(error = error, onRetry = { pagingItems.retry() })
                        }
                    }

                    // Main Items
                    items(count = pagingItems.itemCount) { index ->
                        val item = pagingItems[index]
                        if (item != null) {
                            when (displayMode) {


                                MediaDisplayMode.PosterGrid -> MediaPosterCard(
                                    data = item.toMediaPosterUiData(),
                                    layout = displayPreferences.posterLayout,
                                    config = displayPreferences.posterConfig,
                                    onAction = onMediaAction,
                                    modifier = Modifier.animateItem().animateContentSize().fillMaxWidth()
                                )

                                MediaDisplayMode.List -> MediaListCard(
                                    data = item.toMediaListUiData(),
                                    layout = displayPreferences.listCardLayout,
                                    config = displayPreferences.listConfig,
                                    onAction = onMediaAction,
                                    modifier = Modifier.animateItem().animateContentSize().fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Append Loading/Error
                    if (pagingItems.loadState.append is LoadState.Loading) {
                        item(span = { GridItemSpan(maxLineSpan) }) { PaginationLoader() }
                    }
                    if (pagingItems.loadState.append is LoadState.Error) {
                        val error = (pagingItems.loadState.append as LoadState.Error).error
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            PaginationError(error = error, onRetry = { pagingItems.retry() })
                        }
                    }
                }

            }
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
private fun PaginationLoader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun PaginationError(
    error: Throwable,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Failed to load more",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = error.localizedMessage ?: "Please check your connection.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}