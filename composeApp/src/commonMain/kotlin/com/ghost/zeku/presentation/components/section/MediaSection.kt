package com.ghost.zeku.presentation.components.section


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.presentation.components.list.MediaListCard
import com.ghost.zeku.presentation.components.list.MediaListUiData
import com.ghost.zeku.presentation.components.poster.MediaPosterCard
import com.ghost.zeku.presentation.components.poster.MediaPosterUiData
import com.ghost.zeku.presentation.components.poster.PosterStyle.OVERLAY
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.Res
import zeku.composeapp.generated.resources.no_media_found

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
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    layout: SectionLayout = SectionLayout.HORIZONTAL_ROW,
    key: ((item: T) -> Any)? = null,
    onViewAllClick: (() -> Unit)? = null,
    isLoading: Boolean = false,
    itemContent: @Composable (item: T, modifier: Modifier) -> Unit
) {
    // Subtle entrance animation for the whole section
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessVeryLow)) +
                slideInVertically(initialOffsetY = { 50 })
    ) {
        Column(modifier = modifier.fillMaxWidth()) {

            // 1. Header (Title + Optional "View All" Button)
            SectionHeader(
                title = title,
                onViewAllClick = onViewAllClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Content Area
            if (isLoading) {
                // Show Skeleton Loaders based on layout
                SectionLoadingShimmer(layout = layout)
            } else if (items.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.no_media_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Render the Actual Content
                when (layout) {
                    SectionLayout.HORIZONTAL_ROW -> {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = items,
                                key = key
                                // Optional: If your items have IDs, use key = { it.id } for better performance
                            ) { item ->
                                // The animateItemModifier allows smooth reordering if the list changes!
                                itemContent(item, Modifier.animateItem())
                            }
                        }
                    }

                    SectionLayout.VERTICAL_LIST -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items.forEach { item ->
                                itemContent(item, Modifier)
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

@Composable
private fun SectionHeader(
    title: String,
    onViewAllClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom // Aligns the button nicely with the baseline of the title
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (onViewAllClick != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onViewAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View All $title",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * A beautiful, generic loading shimmer that adapts to the section layout.
 */
@Composable
private fun SectionLoadingShimmer(layout: SectionLayout) {
    when (layout) {
        SectionLayout.HORIZONTAL_ROW -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(140.dp) // Standard poster width
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                }
            }
        }

        SectionLayout.VERTICAL_LIST -> {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .height(110.dp)
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f).padding(top = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth(0.5f).height(12.dp).clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }
    }
}

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
                    MediaPosterUiData(1, "Solo Leveling", "", 8.5f, "EP 12"),
                    MediaPosterUiData(2, "Frieren", "", 9.4f, "Finished"),
                    MediaPosterUiData(3, "Jujutsu Kaisen", "", 8.8f, "Finished")
                )

                MediaSection(
                    title = "Trending Now",
                    items = trendingPosters,
                    layout = SectionLayout.HORIZONTAL_ROW,
                    onViewAllClick = { /* Navigate to grid */ }
                ) { item, modifier ->
                    MediaPosterCard(
                        data = item,
                        style = OVERLAY,
                        onClick = {},
                        modifier = modifier
                    )
                }

                // 2. Vertical List Column (Top Airing)
                val topAiringList = listOf(
                    MediaListUiData(1, "One Piece", "", "TV", listOf("Action"), "Releasing", 8.7f, null, null, true),
                    MediaListUiData(2, "Ninja Kamui", "", "TV", listOf("Action"), "Releasing", 8.1f, null, null, true)
                )

                MediaSection(
                    title = "Top Airing Anime",
                    items = topAiringList,
                    layout = SectionLayout.VERTICAL_LIST,
                    onViewAllClick = null // No View All button
                ) { item, modifier ->
                    MediaListCard(
                        data = item,
                        onClick = {},
                        modifier = modifier
                    )
                }

                // 3. Loading State Example
                MediaSection(
                    title = "Recommended for You",
                    items = emptyList<Any>(),
                    layout = SectionLayout.HORIZONTAL_ROW,
                    isLoading = true,
                    onViewAllClick = {}
                ) { _, _ -> }
            }
        }
    }
}