package com.ghost.zeku.presentation.components.wide


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
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.presentation.common.MediaAsyncImage

// ============================================================================
// UI STATE & MAPPER
// ============================================================================

data class MediaWideUiData(
    val id: Int,
    val title: String,
    val coverImageUrl: String,
    val bannerImageUrl: String? = null,
    val description: String? = null,
    val score: Float? = null,
    val badgeText: String? = null,
    val progress: Float? = null,
    val genres: List<String> = emptyList()
)

fun Media.toWideUiData(): MediaWideUiData {
    val calculatedProgress = if (trackEntry != null && trackEntry?.totalProgress != null) {
        val current = trackEntry?.progress?.toFloat() ?: 0f
        val total = trackEntry?.totalProgress?.toFloat() ?: 1f
        if (total > 0f) (current / total).coerceIn(0f, 1f) else null
    } else null

    return MediaWideUiData(
        id = this.id,
        title = this.title.getPreferred(),
        coverImageUrl = this.coverImage,
        bannerImageUrl = this.bannerImage,
        description = this.description,
        score = this.score,
        badgeText = this.status?.name?.replaceFirstChar { it.uppercase() },
        progress = calculatedProgress,
        genres = this.genres.take(3) // Take max 3 for UI compactness
    )
}

enum class WideCardStyle {
    LIST_ITEM,  // Classic row: small image left, basic text right
    DETAILED,   // Taller row: image left, description & genres right
    BANNER      // 16:9 Image background spanning full width
}

// ============================================================================
// MAIN COMPOSABLE
// ============================================================================

@Composable
fun MediaWideCard(
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
        targetValue = if (isPressed) 0.96f else if (isHovered) 1.01f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "WideCardScaleAnimation"
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

// ============================================================================
// VARIANT: LIST ITEM (Clean, standard row)
// ============================================================================
@Composable
private fun WideListItemCard(data: MediaWideUiData, imageModifier: Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        MediaAsyncImage(
            url = data.coverImageUrl,
            contentDescription = data.title,
            modifier = imageModifier.aspectRatio(3f / 4f).fillMaxHeight()
        )

        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                data.badgeText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (data.badgeText != null && data.score != null) {
                    Text(" • ", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
                data.score?.let {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = ((it * 10.0).toInt() / 10.0).toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============================================================================
// VARIANT: DETAILED (More height, shows synopsis & genres)
// ============================================================================
@Composable
private fun WideDetailedCard(data: MediaWideUiData, imageModifier: Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        MediaAsyncImage(
            url = data.coverImageUrl,
            contentDescription = data.title,
            modifier = imageModifier.aspectRatio(2f / 3f).fillMaxHeight()
        )

        Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.description ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))

            // Genres Row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                data.genres.forEach { genre ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            data.progress?.let { progress ->
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

// ============================================================================
// VARIANT: BANNER (Immersive 16:9 cinematic background)
// ============================================================================
@Composable
private fun WideBannerCard(data: MediaWideUiData, imageModifier: Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        MediaAsyncImage(
            url = data.bannerImageUrl ?: data.coverImageUrl, // Fallback to cover if no banner
            contentDescription = data.title,
            modifier = imageModifier.fillMaxSize()
        )

        // Gradient Scrim to ensure text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.scrim.copy(alpha = 0.95f)),
                        startY = 50f
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
                color = Color.White, // Forced white for scrim contrast
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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