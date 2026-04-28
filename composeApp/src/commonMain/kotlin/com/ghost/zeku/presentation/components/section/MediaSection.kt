package com.ghost.zeku.presentation.components.section


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.presentation.components.media.list.MediaListCard
import com.ghost.zeku.presentation.components.media.list.MediaListUiData
import com.ghost.zeku.presentation.components.media.poster.MediaPosterCard
import com.ghost.zeku.presentation.components.media.poster.MediaPosterUiData
import com.ghost.zeku.presentation.components.media.poster.PosterConfig

// ============================================================================
// ENUMS & CONFIGURATION
// ============================================================================

// ============================================================================
// MAIN COMPOSABLE
// ============================================================================

/**
 * A highly reusable and animated container for displaying lists of Media.
 * It is generic (<T>) so you can pass Poster UI Data, List UI Data, or raw Domain models!
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> MediaSection(
    modifier: Modifier = Modifier,
    title: String,
    items: List<T>,
    config: MediaSectionConfig = MediaSectionConfig(),
    key: ((item: T) -> Any)? = null,
    onViewAllClick: (() -> Unit)? = null,
    isLoading: Boolean = false,
    itemContent: @Composable (item: T, modifier: Modifier) -> Unit
) {
    var isVisible by remember { mutableStateOf(!config.enableEntranceAnimation) }

    LaunchedEffect(Unit) {
        if (config.enableEntranceAnimation) isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible, enter = fadeIn(spring(stiffness = Spring.StiffnessVeryLow)) + slideInVertically { 50 }) {
        Column(modifier = modifier.fillMaxWidth()) {

            // Header
            if (config.showHeader) {
                SectionHeader(
                    title = title,
                    onAction = if (config.showViewAll) onViewAllClick else null,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            when {
                isLoading -> {
                    SectionLoadingShimmer(config)
                }

                items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(config.emptyHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No media found", color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    when (val layout = config.layout) {

                        is SectionLayout.HorizontalRow -> {
                            LazyRow(
                                contentPadding = layout.contentPadding,
                                horizontalArrangement = Arrangement.spacedBy(layout.itemSpacing)
                            ) {
                                items(items, key = key) {
                                    itemContent(it, Modifier.animateItem())
                                }
                            }
                        }

                        is SectionLayout.VerticalList -> {
                            Column(
                                modifier = Modifier.padding(layout.contentPadding),
                                verticalArrangement = Arrangement.spacedBy(layout.itemSpacing)
                            ) {
                                items.forEach {
                                    itemContent(it, Modifier)
                                }
                            }
                        }

                        is SectionLayout.Grid -> {
                            val columns = when (layout.columns) {
                                is GridType.Fixed -> GridCells.Fixed(layout.columns.count)

                                is GridType.Adaptive -> GridCells.Adaptive(layout.columns.minSize)
                            }

                            LazyVerticalGrid(
                                columns = columns,
                                contentPadding = layout.contentPadding,
                                horizontalArrangement = Arrangement.spacedBy(layout.horizontalSpacing),
                                verticalArrangement = Arrangement.spacedBy(layout.verticalSpacing)
                            ) {
                                items(items, key = key) {
                                    itemContent(it, Modifier.animateItem())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// INTERNAL COMPONENTS
// ============================================================================


// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun MediaSectionPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // 1. Horizontal Poster Row (Trending)
                val trendingPosters = listOf(
                    MediaPosterUiData(1, MediaType.ANIME, "Solo Leveling", "", 8.5f, "EP 12"),
                    MediaPosterUiData(2, MediaType.ANIME, "Frieren", "", 9.4f, "Finished"),
                    MediaPosterUiData(3, MediaType.ANIME, "Jujutsu Kaisen", "", 8.8f, "Finished")
                )

                MediaSection(
                    title = "Trending Now",
                    items = trendingPosters,
                    onViewAllClick = { /* Navigate to grid */ }) { item, modifier ->
                    MediaPosterCard(
                        data = item, config = PosterConfig(), onAction = {}, modifier = modifier
                    )
                }

                // 2. Vertical List Column (Top Airing)
                val topAiringList = listOf(
                    MediaListUiData(
                        1,
                        MediaType.ANIME,
                        "One Piece",
                        null,
                        "",
                        "TV",
                        listOf("Action"),
                        "Releasing",
                        8.7f,
                        null,
                        null,
                        true
                    ), MediaListUiData(
                        2,
                        MediaType.ANIME,
                        "Ninja Kamui",
                        null,
                        "",
                        "TV",
                        listOf("Action"),
                        "Releasing",
                        8.1f,
                        null,
                        null,
                        true
                    )
                )

                MediaSection(
                    title = "Top Airing Anime",
                    items = topAiringList,
                    config = MediaSectionConfig(layout = SectionLayout.VerticalList()),
                    onViewAllClick = null // No View All button
                ) { item, modifier ->
                    MediaListCard(
                        data = item, onAction = {}, modifier = modifier
                    )
                }

                // 3. Loading State Example
                MediaSection(
                    title = "Recommended for You",
                    config = MediaSectionConfig(layout = SectionLayout.HorizontalRow()),
                    items = emptyList<Any>(),
                    isLoading = true,
                    onViewAllClick = {}) { _, _ -> }
            }
        }
    }
}