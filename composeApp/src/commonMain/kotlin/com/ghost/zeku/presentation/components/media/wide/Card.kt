package com.ghost.zeku.presentation.components.media.wide

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.presentation.common.MediaAsyncImage
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.Res
import zeku.composeapp.generated.resources.wide_bullet_separator
import zeku.composeapp.generated.resources.wide_no_description
import zeku.composeapp.generated.resources.wide_star_content_description

// ============================================================================
// PUBLIC CARD WITH ANIMATION & CLICK HANDLING
// ============================================================================
@Composable
fun WideMediaCard(
    data: MediaWideUiData,
    style: WideCardStyle,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.97f
            isHovered -> 1.01f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "WideCardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick(data.id) }
    ) {
        when (style) {
            WideCardStyle.LIST_ITEM -> WideListItemCard(data, imageModifier)
            WideCardStyle.DETAILED -> WideDetailedCard(data, imageModifier)
            WideCardStyle.BANNER -> WideBannerCard(data, imageModifier)
        }
    }
}


// ----------------------------------------------------------------------------
// STYLE: LIST ITEM (Clean row, compact)
// ----------------------------------------------------------------------------
@Composable
private fun WideListItemCard(
    data: MediaWideUiData,
    imageModifier: Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.height(100.dp)
        ) {
            MediaAsyncImage(
                url = data.coverImageUrl,
                contentDescription = data.title,
                modifier = imageModifier
                    .aspectRatio(3f / 4f)
                    .fillMaxHeight()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    data.badgeText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (data.badgeText != null && data.score != null) {
                        Text(
                            text = stringResource(Res.string.wide_bullet_separator),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    data.score?.let { score ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = stringResource(Res.string.wide_star_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "%.1f".format(score),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// STYLE: DETAILED (Shows synopsis & genres)
// ----------------------------------------------------------------------------
@Composable
private fun WideDetailedCard(
    data: MediaWideUiData,
    imageModifier: Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp,
            hoveredElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.height(140.dp)
        ) {
            MediaAsyncImage(
                url = data.coverImageUrl,
                contentDescription = data.title,
                modifier = imageModifier
                    .aspectRatio(2f / 3f)
                    .fillMaxHeight()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.description?.takeIf { it.isNotBlank() }
                        ?: stringResource(Res.string.wide_no_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))

                // Genres chips
                if (data.genres.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        data.genres.take(3).forEach { genre ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                tonalElevation = 0.dp
                            ) {
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                data.progress?.let { progress ->
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// STYLE: BANNER (Cinematic 16:9 with gradient overlay)
// ----------------------------------------------------------------------------
@Composable
private fun WideBannerCard(
    data: MediaWideUiData,
    imageModifier: Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            MediaAsyncImage(
                url = data.bannerImageUrl ?: data.coverImageUrl,
                contentDescription = data.title,
                modifier = imageModifier.fillMaxSize()
            )

            // Gradient scrim for text contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f)
                            ),
                            startY = 120f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                data.badgeText?.let {
                    Text(
                        text = it.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary, // High contrast on scrim
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!data.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun WideCardPreview() {
    val mockData = MediaWideUiData(
        id = 1,
        title = "Frieren: Beyond Journey's End",
        coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx154587-nBy0DmcVNoV9.jpg",
        bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/154587-S99zVv9Y76nu.jpg",
        description = "The adventure is over but life goes on for an elf mage who begins to learn what it means to live. Elf mage Frieren and her courageous fellow adventurers have defeated the Demon King.",
        score = 9.4f,
        badgeText = "Finished",
        progress = 0.85f, // 85% watched
        genres = listOf("Adventure", "Drama", "Fantasy")
    )

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Style: BANNER
                SectionHeader("Cinematic Banner Style")
                MediaWideCard(
                    data = mockData,
                    style = WideCardStyle.BANNER,
                    onClick = {}
                )

                // Style: DETAILED
                SectionHeader("Detailed Info Style")
                MediaWideCard(
                    data = mockData,
                    style = WideCardStyle.DETAILED,
                    onClick = {}
                )

                // Style: LIST_ITEM
                SectionHeader("Compact List Item Style")
                MediaWideCard(
                    data = mockData,
                    style = WideCardStyle.LIST_ITEM,
                    onClick = {}
                )

                // Example with Manga data
                SectionHeader("Manga Example (ListItem)")
                MediaWideCard(
                    data = mockData.copy(
                        title = "Berserk",
                        badgeText = "Releasing",
                        progress = null,
                        genres = listOf("Action", "Dark Fantasy")
                    ),
                    style = WideCardStyle.LIST_ITEM,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}