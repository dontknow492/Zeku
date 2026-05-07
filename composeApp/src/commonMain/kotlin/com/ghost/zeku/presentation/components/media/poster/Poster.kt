package com.ghost.zeku.presentation.components.media.poster


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
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.common.MediaBadge
import com.ghost.zeku.presentation.common.MediaImage
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.OnMediaAction
import com.ghost.zeku.presentation.theme.AppTheme


// ============================================================================
// MAIN CARD
// ============================================================================

@Composable
fun MediaPosterCard(
    data: MediaPosterUiData,
    config: PosterConfig,
    onAction: OnMediaAction,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> config.interaction.scaleOnPress
            isHovered && config.interaction.enableHover -> config.interaction.scaleOnHover
            else -> 1f
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = modifier
            .width(config.content.width)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onAction(MediaAction.MediaClick(data.id, data.mediaType))
            }
    ) {
        when (config.layout) {
            PosterLayout.Minimal -> MinimalPoster(data, onAction, config)
            PosterLayout.Overlay -> OverlayPoster(data, onAction, config, isHovered)
            PosterLayout.Modern -> ModernPoster(data, onAction, config)
            PosterLayout.Compact -> CompactPoster(data, onAction, config)
        }
    }
}

// ============================================================================
// VARIANTS
// ============================================================================

// ----------------------------------------------------------------------------
// MINIMAL
// ----------------------------------------------------------------------------

@Composable
private fun MinimalPoster(
    data: MediaPosterUiData,
    onAction: OnMediaAction,
    config: PosterConfig
) {
    Column {
        MediaImage(
            imageUrl = data.imageUrl,
            title = data.title,
            isNsfw = data.isNsfw,
            isRevealed = data.isNsfwRevealed,
            onReveal = {
                onAction(MediaAction.RevealNsfw(data.id))
            },
            nsfwConfig = config.nsfw,
            mediaImageConfig = config.image,
            badge = {
                MediaBadge(data.score, data.badgeText, config.badges)
            }
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = data.title,
            maxLines = config.content.titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            style = config.content.titleStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ----------------------------------------------------------------------------
// OVERLAY (cinematic)
// ----------------------------------------------------------------------------

@Composable
private fun OverlayPoster(
    data: MediaPosterUiData,
    onAction: OnMediaAction,
    config: PosterConfig,
    isHovered: Boolean
) {
    val imageScale by animateFloatAsState(
        targetValue = if (isHovered && config.image.enableHoverZoom)
            config.image.hoverZoomScale else 1f
    )

    Box(
        modifier = Modifier
            .aspectRatio(config.image.aspectRatio)
            .clip(RoundedCornerShape(config.image.cornerRadius))
    ) {

        MediaAsyncImage(
            url = data.imageUrl,
            contentDescription = data.title,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = imageScale
                    scaleY = imageScale
                }
        )

        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(config.image.scrim)
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(config.content.padding)
        ) {
            Text(
                text = data.title,
                maxLines = config.content.titleMaxLines,
                overflow = TextOverflow.Ellipsis,
                style = config.content.titleStyle,
                color = config.content.titleColor
            )

            if (config.content.showSubtitle && data.subTitle != null) {
                Text(
                    text = data.subTitle,
                    style = config.content.subtitleStyle,
                    color = config.content.subtitleColor
                )
            }

            MediaProgressIndicator(
                progress = data.progress,
                show = config.badges.showProgress
            )
        }

        MediaImage(
            imageUrl = data.imageUrl,
            title = data.title,
            isNsfw = data.isNsfw,
            isRevealed = data.isNsfwRevealed,
            onReveal = {
                onAction(MediaAction.RevealNsfw(data.id))
            },
            nsfwConfig = config.nsfw,
            mediaImageConfig = config.image,
            badge = {
                MediaBadge(data.score, data.badgeText, config.badges)
            }
        )
    }
}

// ----------------------------------------------------------------------------
// MODERN
// ----------------------------------------------------------------------------

@Composable
private fun ModernPoster(
    data: MediaPosterUiData,
    onAction: OnMediaAction,
    config: PosterConfig
) {
    Card(
        shape = RoundedCornerShape(config.image.cornerRadius)
    ) {
        Column {

            MediaImage(
                imageUrl = data.imageUrl,
                title = data.title,
                isNsfw = data.isNsfw,
                isRevealed = data.isNsfwRevealed,
                onReveal = {
                    onAction(MediaAction.RevealNsfw(data.id))
                },
                nsfwConfig = config.nsfw,
                mediaImageConfig = config.image,
                badge = {
                    MediaBadge(data.score, data.badgeText, config.badges)
                }
            )

            Column(Modifier.padding(10.dp)) {
                Text(
                    text = data.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = config.content.titleStyle
                )

                if (data.subTitle != null) {
                    Text(
                        text = data.subTitle,
                        style = config.content.subtitleStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPACT
// ----------------------------------------------------------------------------

@Composable
private fun CompactPoster(
    data: MediaPosterUiData,
    onAction: OnMediaAction,
    config: PosterConfig
) {
    Box(
        modifier = Modifier
            .aspectRatio(config.image.aspectRatio)
            .clip(RoundedCornerShape(config.image.cornerRadius))
    ) {
        MediaImage(
            imageUrl = data.imageUrl,
            title = data.title,
            isNsfw = data.isNsfw,
            isRevealed = data.isNsfwRevealed,
            onReveal = {
                onAction(MediaAction.RevealNsfw(data.id))
            },
            nsfwConfig = config.nsfw,
            mediaImageConfig = config.image,
            badge = {
                MediaBadge(data.score, data.badgeText, config.badges)
            }
        )
    }
}

// ============================================================================
// INTERNAL COMPONENTS
// ============================================================================


// ----------------------------------------------------------------------------
// BADGES
// ----------------------------------------------------------------------------


// ----------------------------------------------------------------------------
// PROGRESS
// ----------------------------------------------------------------------------

@Composable
private fun MediaProgressIndicator(
    progress: Float?,
    show: Boolean
) {
    AnimatedVisibility(show && progress != null) {
        LinearProgressIndicator(
            progress = { progress ?: 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )
    }
}


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
        progress = 0.7f,
        mediaType = MediaType.ANIME
//        isNsfw = true
    )

    AppTheme(isDarkTheme = !true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MediaPosterCard(mockData, config = PosterConfig(layout = PosterLayout.Minimal), {})
            MediaPosterCard(mockData, config = PosterConfig(layout = PosterLayout.Overlay), {})
            MediaPosterCard(mockData, config = PosterConfig(layout = PosterLayout.Modern), {})
            MediaPosterCard(mockData, config = PosterConfig(layout = PosterLayout.Compact), {})
        }
    }
}