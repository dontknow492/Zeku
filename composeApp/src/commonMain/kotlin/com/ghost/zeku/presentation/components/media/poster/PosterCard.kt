package com.ghost.zeku.presentation.components.media.poster

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.OnMediaAction
import com.ghost.zeku.presentation.components.media.list.ListCardDefaults
import com.ghost.zeku.presentation.components.media.list.ListCardLayout
import com.ghost.zeku.presentation.components.media.list.MediaListCard
import com.ghost.zeku.presentation.theme.AppTheme

@Composable
fun MediaPosterCard(
    modifier: Modifier = Modifier,
    data: MediaPosterUiData,
    layout: PosterLayout = PosterLayout.Modern,
    config: PosterConfig = PosterDefaults.forLayout(layout),
    onAction: OnMediaAction,
) {

    val interactionSource = remember { MutableInteractionSource() }

    val isHovered by interactionSource.collectIsHoveredAsState()

    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {

            isPressed && config.enablePress ->
                config.scaleOnPress

            isHovered && config.enableHover ->
                config.scaleOnHover

            else -> 1f
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessLow
        ),
        label = "poster_scale"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .aspectRatio(config.aspectRatio)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,

                onClick = {
                    onAction(
                        MediaAction.MediaClick(
                            data.id,
                            data.mediaType
                        )
                    )
                },

                onLongClick = {
                    onAction(
                        MediaAction.LongClick(
                            data.id, data.mediaType
                        )
                    )
                }
            ),

        shape = RoundedCornerShape(
            config.cornerRadius
        )
    ) {

        when (layout) {

            PosterLayout.Minimal -> {
                MinimalPosterLayout(
                    data = data,
                    config = config,
                    isHovered = isHovered,
                    onAction = onAction
                )
            }

            PosterLayout.Modern -> {
                ModernPosterLayout(
                    data = data,
                    config = config,
                    isHovered = isHovered,
                    onAction = onAction
                )
            }

            PosterLayout.Overlay -> {
                OverlayPosterLayout(
                    data = data,
                    config = config,
                    isHovered = isHovered,
                    onAction = onAction
                )
            }

            PosterLayout.Compact -> {
                CompactPosterLayout(
                    data = data,
                    config = config,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
private fun ModernPosterLayout(
    data: MediaPosterUiData,
    config: PosterConfig,
    isHovered: Boolean,
    onAction: OnMediaAction
) {

    Box {

        MediaAsyncImage(
            url = data.imageUrl,
            contentDescription = data.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        if (config.showGradientOverlay) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {

            if (config.showTitle) {

                Text(
                    text = data.title,

                    maxLines = config.maxTitleLines,

                    overflow = TextOverflow.Ellipsis,

                    style = MaterialTheme.typography.titleSmall,

                    color = Color.White
                )
            }

            if (
                config.showSubtitle &&
                data.subTitle != null
            ) {

                Spacer(Modifier.height(2.dp))

                Text(
                    text = data.subTitle,

                    maxLines = config.maxSubtitleLines,

                    overflow = TextOverflow.Ellipsis,

                    style = MaterialTheme.typography.bodySmall,

                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        if (
            config.showScore &&
            data.score != null
        ) {

            Surface(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd),

                shape = RoundedCornerShape(999.dp),

                tonalElevation = 4.dp
            ) {

                Text(
                    text = data.score.toString(),

                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),

                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (
            data.isNsfw &&
            !data.isNsfwRevealed &&
            config.enableBlurNsfw
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.55f)
                    )
                    .clickable {
                        onAction(
                            MediaAction.RevealNsfw(data.id)
                        )
                    },

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "NSFW",

                    style = MaterialTheme.typography.titleMedium,

                    color = Color.White
                )
            }
        }
    }
}


@Composable
private fun OverlayPosterLayout(
    data: MediaPosterUiData,
    config: PosterConfig,
    isHovered: Boolean,
    onAction: OnMediaAction
) {

    Box {

        MediaAsyncImage(
            url = data.imageUrl,
            contentDescription = data.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = isHovered || !config.enableHover,

            enter = fadeIn() + scaleIn(),

            exit = fadeOut() + scaleOut()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.65f)
                    )
            )
        }

        if (
            (isHovered || !config.enableHover) &&
            config.showGradientOverlay
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {

                if (config.showTitle) {

                    Text(
                        text = data.title,

                        style = MaterialTheme.typography.titleMedium,

                        color = Color.White,

                        maxLines = config.maxTitleLines,

                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (
                    config.showSubtitle &&
                    data.subTitle != null
                ) {

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = data.subTitle,

                        style = MaterialTheme.typography.bodySmall,

                        color = Color.White.copy(alpha = 0.75f),

                        maxLines = config.maxSubtitleLines,

                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (
                    config.showProgress &&
                    data.progress != null
                ) {

                    Spacer(Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { data.progress },

                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (
            config.showScore &&
            data.score != null
        ) {

            Surface(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopEnd),

                shape = RoundedCornerShape(999.dp),

                color = Color.Black.copy(alpha = 0.55f)
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,

                        tint = Color.White,

                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = data.score.toString(),

                        color = Color.White,

                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        if (
            data.badgeText != null
        ) {

            Surface(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopStart),

                shape = RoundedCornerShape(999.dp),

                color = MaterialTheme.colorScheme.primary
            ) {

                Text(
                    text = data.badgeText,

                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),

                    style = MaterialTheme.typography.labelSmall,

                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        if (
            data.isNsfw &&
            !data.isNsfwRevealed &&
            config.enableBlurNsfw
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.7f)
                    )
                    .clickable {
                        onAction(
                            MediaAction.RevealNsfw(data.id)
                        )
                    },

                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Rounded.VisibilityOff,
                        contentDescription = null,

                        tint = Color.White
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Reveal NSFW",

                        color = Color.White,

                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}


@Composable
private fun MinimalPosterLayout(
    data: MediaPosterUiData,
    config: PosterConfig,
    isHovered: Boolean,
    onAction: OnMediaAction
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            MediaAsyncImage(
                url = data.imageUrl,
                contentDescription = data.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            if (
                config.showScore &&
                data.score != null
            ) {

                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),

                    shape = RoundedCornerShape(999.dp),

                    color = Color.Black.copy(alpha = 0.6f)
                ) {

                    Text(
                        text = data.score.toString(),

                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),

                        style = MaterialTheme.typography.labelSmall,

                        color = Color.White
                    )
                }
            }

            if (
                data.badgeText != null
            ) {

                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),

                    shape = RoundedCornerShape(999.dp),

                    color = MaterialTheme.colorScheme.primary
                ) {

                    Text(
                        text = data.badgeText,

                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),

                        style = MaterialTheme.typography.labelSmall,

                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (
                data.isNsfw &&
                !data.isNsfwRevealed &&
                config.enableBlurNsfw
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.75f)
                        )
                        .clickable {
                            onAction(
                                MediaAction.RevealNsfw(data.id)
                            )
                        },

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "NSFW",

                        style = MaterialTheme.typography.titleMedium,

                        color = Color.White
                    )
                }
            }
        }

        if (config.showTitle) {

            Column(
                modifier = Modifier.padding(
                    horizontal = 6.dp,
                    vertical = 8.dp
                )
            ) {

                Text(
                    text = data.title,

                    maxLines = config.maxTitleLines,

                    overflow = TextOverflow.Ellipsis,

                    style = MaterialTheme.typography.titleSmall
                )

                if (
                    config.showSubtitle &&
                    data.subTitle != null
                ) {

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = data.subTitle,

                        maxLines = config.maxSubtitleLines,

                        overflow = TextOverflow.Ellipsis,

                        style = MaterialTheme.typography.bodySmall,

                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
private fun CompactPosterLayout(
    data: MediaPosterUiData,
    onAction: OnMediaAction,
    config: PosterConfig,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(config.cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable {
                onAction(MediaAction.MediaClick(data.id, data.mediaType))
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        // =========================
        // Thumbnail
        // =========================
        MediaAsyncImage(
            url = data.imageUrl,
            contentDescription = data.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // =========================
        // Info Section
        // =========================
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)
        ) {

            Text(
                text = data.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            data.subTitle?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (data.score != null || data.progress != null) {
                Spacer(Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    data.score?.let {
                        Text(
                            text = "★ ${String.format("%.1f", it)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    data.progress?.let {
                        Text(
                            text = "${(it * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // =========================
        // Quick Action (Optional)
        // =========================
        IconButton(
            onClick = {
                onAction(MediaAction.AddToList(data.id))
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewPosterCard() {
    val samplePosters = listOf(
        MediaPosterUiData(
            id = 101,
            mediaType = MediaType.MANGA,
            title = "Neon Skies",
            imageUrl = "https://example.com/images/neon_skies_poster.jpg",
            bannerImageUrl = "https://example.com/banners/neon_skies_banner.jpg",
            score = 8.3f,
            badgeText = "TV",
            subTitle = "12 Episodes • Sci‑fi",
            extraInfo = "Episode 5",
            progress = 0.45f,
            progressText = "5/12",
            isNsfw = false,
            isAiring = false,
            overlayLabel = "⟳ CONTINUE"
        ),
        MediaPosterUiData(
            id = 102,
            mediaType = MediaType.MANGA,
            title = "Last Light",
            imageUrl = "https://example.com/images/last_light_poster.jpg",
            bannerImageUrl = null,
            score = 7.1f,
            badgeText = "MOVIE",
            subTitle = "2022 • Drama",
            extraInfo = null,
            progress = 1.0f,
            progressText = "Watched",
            isNsfw = false,
            isAiring = false,
            overlayLabel = "✓ COMPLETED"
        ),
        MediaPosterUiData(
            id = 103,
            mediaType = MediaType.MANGA,
            title = "Circuit Breaker",
            imageUrl = "https://example.com/images/circuit_breaker_poster.jpg",
            bannerImageUrl = null,
            score = 7.8f,
            badgeText = null,
            subTitle = "Science • 90 min",
            extraInfo = null,
            progress = 0.75f,
            progressText = "68 min",
            isNsfw = false,
            isAiring = false,
            overlayLabel = "⟳ CONTINUE"
        ),
        MediaPosterUiData(
            id = 104,
            mediaType = MediaType.MANGA,
            title = "Midnight Market",
            imageUrl = "https://example.com/images/midnight_market_poster.jpg",
            bannerImageUrl = "https://example.com/banners/midnight_market_banner.jpg",
            score = 6.6f,
            badgeText = "TV",
            subTitle = "Season 2 • Comedy",
            extraInfo = "Episode 2",
            progress = 0.12f,
            progressText = "2/10",
            isNsfw = false,
            isAiring = true,
            overlayLabel = "▶ PLAY"
        ),
        MediaPosterUiData(
            id = 105,
            mediaType = MediaType.MANGA,
            title = "Garden of Echoes",
            imageUrl = "https://example.com/images/garden_of_echoes_poster.jpg",
            bannerImageUrl = "https://example.com/banners/garden_of_echoes_banner.jpg",
            score = 8.9f,
            badgeText = "MANHWA",
            subTitle = "Limited • Fantasy",
            extraInfo = "Chapter 120",
            progress = 0.0f,
            progressText = null,
            isNsfw = false,
            isAiring = false,
            overlayLabel = null
        )
    )



    AppTheme {
        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 190.dp)) {
            items(samplePosters) { item ->
                val layout = PosterLayout.Overlay
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(text = layout.name, style = TextStyle(fontSize = 20.sp))
                    MediaPosterCard(
                        modifier = Modifier,
                        data = item,
                        layout = layout,
                        config = PosterDefaults.forLayout(layout),
                        onAction = { TODO() },
                    )
                }
            }
        }
    }
}

