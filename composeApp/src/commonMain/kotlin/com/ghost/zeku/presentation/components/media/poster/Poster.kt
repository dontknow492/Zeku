package com.ghost.zeku.presentation.components.media.poster


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.theme.AppTheme


// ============================================================================
// MAIN CARD
// ============================================================================

@Composable
fun MediaPosterCard(
    data: MediaPosterUiData,
    config: PosterConfig,
    onClick: (Int) -> Unit,
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
        animationSpec = config.interaction.animationSpec
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
            ) { onClick(data.id) }
    ) {
        when (config.layout) {
            PosterLayout.Minimal -> MinimalPoster(data, config)
            PosterLayout.Overlay -> OverlayPoster(data, config, isHovered)
            PosterLayout.Modern -> ModernPoster(data, config)
            PosterLayout.Compact -> CompactPoster(data, config)
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
    config: PosterConfig
) {
    Column {
        PosterImage(data, config)

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
                .background(config.image.scrim)
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

        PosterBadges(data, config)
    }
}

// ----------------------------------------------------------------------------
// MODERN
// ----------------------------------------------------------------------------

@Composable
private fun ModernPoster(
    data: MediaPosterUiData,
    config: PosterConfig
) {
    Card(
        shape = RoundedCornerShape(config.image.cornerRadius)
    ) {
        Column {

            PosterImage(data, config)

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
    config: PosterConfig
) {
    Box(
        modifier = Modifier
            .aspectRatio(config.image.aspectRatio)
            .clip(RoundedCornerShape(config.image.cornerRadius))
    ) {
        PosterImage(data, config)
    }
}

// ============================================================================
// INTERNAL COMPONENTS
// ============================================================================

@Composable
private fun PosterImage(
    data: MediaPosterUiData,
    config: PosterConfig
) {
    var revealed by remember { mutableStateOf(false) }


    val shouldBlur = data.isNsfw &&
            config.nsfw.enabled &&
            !revealed

    val blurRadius by animateFloatAsState(
        targetValue = if (shouldBlur) config.nsfw.blurRadius else 0f
    )

    Box(
        modifier = Modifier
            .aspectRatio(config.image.aspectRatio)
    ) {

        // 🎞️ Image
        MediaAsyncImage(
            url = data.imageUrl,
            contentDescription = data.title,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (shouldBlur)
                        Modifier.blur(blurRadius.dp)
                    else Modifier
                )
        )

        // 🌑 Dark overlay
        if (shouldBlur) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = config.nsfw.dimAlpha))
            )

            // 🔞 Label + CTA
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (config.nsfw.showLabel) {
                    Text(
                        text = "NSFW",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (config.nsfw.clickToReveal) {
                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Tap to reveal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // 👇 Click to reveal (only when blurred)
        if (shouldBlur && config.nsfw.clickToReveal) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        revealed = true
                    }
            )
        }

        // Normal badges (only when visible OR always if you want)
        if (!shouldBlur) {
            PosterBadges(data, config)
        }
    }
}

// ----------------------------------------------------------------------------
// BADGES
// ----------------------------------------------------------------------------

@Composable
private fun BoxScope.PosterBadges(
    data: MediaPosterUiData,
    config: PosterConfig
) {


    fun Modifier.alignBadge(position: BadgePosition) = when (position) {
        BadgePosition.TOP_START -> align(Alignment.TopStart)
        BadgePosition.TOP_END -> align(Alignment.TopEnd)
        BadgePosition.BOTTOM_START -> align(Alignment.BottomStart)
        BadgePosition.BOTTOM_END -> align(Alignment.BottomEnd)
    }

    if (config.badges.showScore && data.score != null) {
        Surface(
            modifier = Modifier
                .alignBadge(config.badges.scorePosition)
                .padding(6.dp),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, null, Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text(
                    "%.1f".format(data.score),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                )
            }
        }
    }

    if (config.badges.showBadge && data.badgeText != null) {
        Surface(
            modifier = Modifier
                .alignBadge(config.badges.badgePosition),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = data.badgeText.uppercase(),
                fontSize = 10.sp,
                modifier = Modifier.padding(4.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

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