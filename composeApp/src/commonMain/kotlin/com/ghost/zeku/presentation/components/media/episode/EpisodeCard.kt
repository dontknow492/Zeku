package com.ghost.zeku.presentation.components.media.episode

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.media.Episode
import com.ghost.zeku.presentation.common.MediaAsyncImage


@Composable
fun EpisodeCard(
    episode: Episode,
    config: EpisodeCardConfig = EpisodeCardConfig(),
    modifier: Modifier = Modifier,
    onClick: (Episode) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scaleTarget = when {
        isPressed -> config.scaleOnPress
        isHovered -> config.scaleOnHover
        else -> 1f
    }

    val scale by animateFloatAsState(scaleTarget, config.animationSpec)

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        when (config.variant) {
            EpisodeCardVariant.MODERN ->
                ModernEpisodeCard(episode, config, interactionSource, onClick)

            EpisodeCardVariant.MINIMAL ->
                MinimalEpisodeCard(episode, config, interactionSource, onClick)

            EpisodeCardVariant.COMPACT ->
                CompactEpisodeCard(episode, config, interactionSource, onClick)
        }
    }
}


@Composable
private fun ModernEpisodeCard(
    episode: Episode,
    config: EpisodeCardConfig,
    interactionSource: MutableInteractionSource,
    onClick: (Episode) -> Unit,
) {
    Card(
        onClick = { if (config.clickable) onClick(episode) },
        interactionSource = interactionSource,
        shape = config.shape,
        colors = CardDefaults.cardColors(containerColor = config.containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {

            if (config.showThumbnail && episode.thumbnail != null) {
                MediaAsyncImage(
                    url = episode.thumbnail,
                    contentDescription = episode.title ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            // Gradient overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(config.padding)
            ) {
                Text(
                    text = "EP ${episode.number}",
                    color = config.subtitleColor,
                    style = config.subtitleStyle
                )

                Text(
                    text = episode.title ?: "Episode ${episode.number}",
                    color = config.titleColor,
                    style = config.titleStyle,
                    maxLines = 1
                )
            }

            if (episode.isFiller) {
                FillerBadge(Modifier.align(Alignment.TopEnd))
            }
        }
    }
}


@Composable
private fun MinimalEpisodeCard(
    episode: Episode,
    config: EpisodeCardConfig,
    interactionSource: MutableInteractionSource,
    onClick: (Episode) -> Unit,
) {
    Card(
        onClick = { if (config.clickable) onClick(episode) },
        interactionSource = interactionSource,
        shape = config.shape,
        colors = CardDefaults.cardColors(containerColor = config.containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(config.height)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(config.padding)
        ) {

            Text(
                text = episode.number.toString(),
                style = config.titleStyle,
                color = config.titleColor
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = episode.title ?: "Episode ${episode.number}",
                    style = config.titleStyle,
                    color = config.titleColor,
                    maxLines = 1
                )

                if (config.showDescription && episode.description != null) {
                    Text(
                        text = episode.description,
                        style = config.subtitleStyle,
                        color = config.subtitleColor,
                        maxLines = 1
                    )
                }
            }

            if (episode.isFiller) {
                FillerBadge()
            }
        }
    }
}


@Composable
private fun CompactEpisodeCard(
    episode: Episode,
    config: EpisodeCardConfig,
    interactionSource: MutableInteractionSource,
    onClick: (Episode) -> Unit,
) {
    Card(
        onClick = { if (config.clickable) onClick(episode) },
        interactionSource = interactionSource,
        shape = config.shape,
        colors = CardDefaults.cardColors(containerColor = config.containerColor)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (config.showThumbnail && episode.thumbnail != null) {
                MediaAsyncImage(
                    url = episode.thumbnail,
                    contentDescription = "",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "EP ${episode.number}",
                    style = config.subtitleStyle,
                    color = config.subtitleColor
                )

                Text(
                    text = episode.title ?: "Episode ${episode.number}",
                    style = config.titleStyle,
                    color = config.titleColor,
                    maxLines = 1
                )
            }

            if (episode.isFiller) {
                FillerBadge()
            }
        }
    }
}


@Composable
fun FillerBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFFF9800))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "FILLER",
            color = Color.Black,
            style = MaterialTheme.typography.labelSmall
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewEpisodeCardModern() {
    val sampleEpisode = Episode(
        id = "1",
        number = 1,
        title = "The Beginning",
        description = "The journey starts with unexpected events.",
        thumbnail = "https://via.placeholder.com/300x200",
        isFiller = false
    )

    val fillerEpisode = sampleEpisode.copy(
        number = 5,
        title = "Side Story",
        isFiller = true
    )
    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            EpisodeCard(
                episode = sampleEpisode,
                config = EpisodeCardConfig(
                    variant = EpisodeCardVariant.MODERN
                ),
                onClick = {}
            )

            EpisodeCard(
                episode = fillerEpisode,
                config = EpisodeCardConfig(
                    variant = EpisodeCardVariant.MODERN
                ),
                onClick = {}

            )

            EpisodeCard(
                episode = sampleEpisode,
                config = EpisodeCardConfig(
                    variant = EpisodeCardVariant.MINIMAL
                ),
                onClick = {}
            )

            EpisodeCard(
                episode = fillerEpisode,
                config = EpisodeCardConfig(
                    variant = EpisodeCardVariant.MINIMAL
                ),
                onClick = {}
            )

            EpisodeCard(
                episode = sampleEpisode,
                config = EpisodeCardConfig(
                    variant = EpisodeCardVariant.COMPACT
                ),
                onClick = {}
            )

            EpisodeCard(
                episode = fillerEpisode,
                config = EpisodeCardConfig(
                    variant = EpisodeCardVariant.COMPACT
                ),
                onClick = {}
            )
        }
    }
}