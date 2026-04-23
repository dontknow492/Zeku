package com.ghost.zeku.presentation.components.poster

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.Res
import zeku.composeapp.generated.resources.score


@Composable
fun MediaPosterCard(
    data: MediaPosterUiData,
    style: PosterStyle,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    config: PosterConfig? = null // 👈 new
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val resolvedConfig = remember(config) {
        config ?: PosterDefaults.config(style)
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> resolvedConfig.pressedScale
            isHovered -> resolvedConfig.hoveredScale
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PosterScale"
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
            PosterStyle.MINIMAL -> MinimalPoster(data, resolvedConfig, imageModifier)
            PosterStyle.OVERLAY -> OverlayPoster(data, resolvedConfig, imageModifier)
            PosterStyle.MODERN -> ModernPoster(data, resolvedConfig, imageModifier)
            PosterStyle.COMPACT -> CompactPoster(data, resolvedConfig, imageModifier)
        }
    }
}

// ----------------------------------------------------------------------------
// VARIANT: MINIMAL (Image with title below)
// ----------------------------------------------------------------------------
@Composable
private fun MinimalPoster(
    data: MediaPosterUiData,
    config: PosterConfig,
    imageModifier: Modifier
) {
    Column(
        modifier = Modifier.width(config.width)
    ) {
        PosterImage(
            data = data,
            config = config,
            modifier = Modifier.fillMaxWidth(),
            imageModifier = imageModifier
                .aspectRatio(config.aspectRatio)
                .clip(RoundedCornerShape(config.cornerRadius))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = data.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ----------------------------------------------------------------------------
// VARIANT: OVERLAY (Image with gradient + text overlay)
// ----------------------------------------------------------------------------
@Composable
private fun OverlayPoster(
    data: MediaPosterUiData,
    config: PosterConfig,
    imageModifier: Modifier
) {
    Box(
        modifier = Modifier
            .width(config.width)
            .clip(RoundedCornerShape(config.cornerRadius))
            .aspectRatio(config.aspectRatio)
    ) {
        PosterImage(
            data = data,
            config = config,
            modifier = Modifier.fillMaxWidth(),
            imageModifier = imageModifier.fillMaxSize()
        )

        // Modern gradient overlay (using scrim as dark base)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f)
                        ),
                        startY = 100f
                    )
                )
        )

        // Content at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            data.subTitle?.let {
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = data.title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            MediaProgressIndicator(
                progress = data.progress,
                showProgress = config.showProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
            )
        }
    }
}

// ----------------------------------------------------------------------------
// VARIANT: MODERN (Card with image top, info bottom, floating score)
// ----------------------------------------------------------------------------
@Composable
private fun ModernPoster(
    data: MediaPosterUiData,
    config: PosterConfig,
    imageModifier: Modifier
) {
    Card(
        modifier = Modifier.width(config.width),
        shape = RoundedCornerShape(config.cornerRadius),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp,
            hoveredElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            PosterImage(
                data = data,
                config = config,
                modifier = Modifier.fillMaxWidth(),
                imageModifier = imageModifier.aspectRatio(config.aspectRatio)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                data.subTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// VARIANT: COMPACT (Dense, stylized badge)
// ----------------------------------------------------------------------------
@Composable
private fun CompactPoster(
    data: MediaPosterUiData,
    config: PosterConfig,
    imageModifier: Modifier
) {
    Box(
        modifier = Modifier
            .width(config.width)
            .clip(RoundedCornerShape(config.cornerRadius))
            .aspectRatio(config.aspectRatio)
    ) {

        PosterImage(
            data = data,
            config = config,
            modifier = Modifier.fillMaxWidth(),
            imageModifier = imageModifier.fillMaxSize()
        )
    }
}


@Composable
private fun MediaProgressIndicator(
    progress: Float?,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(showProgress && progress != null, modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress ?: 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}


@Composable
private fun PosterImage(
    data: MediaPosterUiData,
    config: PosterConfig,
    imageModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        MediaAsyncImage(
            url = data.imageUrl,
            contentDescription = data.title,
            modifier = imageModifier
        )
        MediaRatingIndicator(
            score = data.score,
            showScore = config.showScore,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        )
        MediaBadgeIndicator(
            text = data.badgeText,
            showBadge = config.showBadge,
            modifier = Modifier.align(Alignment.TopStart).padding(0.dp)
        )
    }
}

@Composable
private fun MediaRatingIndicator(
    modifier: Modifier = Modifier,
    score: Float?,
    showScore: Boolean,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    AnimatedVisibility(showScore && score != null, modifier = modifier) {
        Surface(
            modifier = Modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = stringResource(Res.string.score),
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "%.1f".format(score),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


@Composable
private fun MediaBadgeIndicator(
    modifier: Modifier = Modifier,
    text: String?,
    showBadge: Boolean,
    shape: Shape = RoundedCornerShape(bottomEnd = 8.dp),
) {
    AnimatedVisibility(showBadge && text != null, modifier = modifier) {
        Surface(
            modifier = Modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            shadowElevation = 2.dp
        ) {
//            Text(
//                    text = badge.uppercase(),
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    fontSize = 9.sp,
//                    fontWeight = FontWeight.Black,
//                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
//                )
            Text(
                text = text!!.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}


// ----------------------------------------------------------------------------
// REUSABLE IMAGE COMPONENT (Coil 3 with loading/error states)
// ----------------------------------------------------------------------------


// ----------------------------------------------------------------------------
// PREVIEWS
// ----------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
private fun MediaPosterPreviews() {
    val mockData = MediaPosterUiData(
        id = 1,
        title = "Jujutsu Kaisen Season 2",
        imageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx147105-rwOX8qyUy8gV.jpg",
        score = 8.8f,
        badgeText = "Ch 12",
        subTitle = "Unknown",
        progress = 0.7f
    )

    AppTheme(isDarkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MediaPosterCard(mockData, PosterStyle.MINIMAL, {})
            MediaPosterCard(mockData, PosterStyle.OVERLAY, {})
            MediaPosterCard(mockData, PosterStyle.MODERN, {})
            MediaPosterCard(mockData, PosterStyle.COMPACT, {})
        }
    }
}