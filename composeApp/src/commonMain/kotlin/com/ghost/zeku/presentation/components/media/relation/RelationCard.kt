package com.ghost.zeku.presentation.components.media.relation


import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.media.MediaTitle
import com.ghost.zeku.domain.model.media.MediaFormat
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.media.RelationType
import com.ghost.zeku.domain.model.media.getPreferred
import com.ghost.zeku.domain.model.media.MediaRelation
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.theme.AppTheme

@Composable
fun MediaRelationCard(
    relation: MediaRelation,
    config: MediaRelationCardConfig = MediaRelationCardConfig(),
    modifier: Modifier = Modifier,
    onClick: (MediaRelation) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scaleTarget = when {
        isPressed -> config.scaleOnPress
        isHovered -> config.scaleOnHover
        else -> 1f
    }

    val elevationTarget = when {
        isPressed -> config.pressedElevation
        isHovered -> config.hoverElevation
        else -> config.elevation
    }

    val scale by animateFloatAsState(scaleTarget, config.animationSpec)
    val elevation by animateDpAsState(elevationTarget, config.elevationAnimationSpec)

    val title = relation.title.getPreferred(config.preferredTitleLanguage)

    val baseModifier = modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }

    when (config.layout) {
        RelationCardLayout.POSTER -> PosterRelationCard(
            relation, title, config, interactionSource, elevation, baseModifier, onClick
        )

        RelationCardLayout.WIDE -> WideRelationCard(
            relation, title, config, interactionSource, elevation, baseModifier, onClick
        )
    }
}

@Composable
private fun PosterRelationCard(
    relation: MediaRelation,
    title: String,
    config: MediaRelationCardConfig,
    interactionSource: MutableInteractionSource,
    elevation: Dp,
    modifier: Modifier,
    onClick: (MediaRelation) -> Unit
) {
    Card(
        onClick = { if (config.clickable) onClick(relation) },
        interactionSource = interactionSource,
        shape = config.shape,
        elevation = CardDefaults.cardElevation(elevation),
        modifier = modifier
            .width(config.width)
            .aspectRatio(config.aspectRatio)
    ) {
        Box {
            MediaAsyncImage(
                url = relation.coverImage.orEmpty(),
                contentDescription = title,
                contentScale = config.contentScale,
                modifier = Modifier
            )

            Box(
                Modifier
                    .matchParentSize()
                    .background(config.gradient)
            )

            if (config.showRelationBadge) {
                RelationBadge(
                    text = relation.relationType.name,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                )
            }

            if (config.showFormatBadge) {
                RelationBadge(
                    text = relation.format.name,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(config.contentPadding)
            ) {
                Text(title, style = config.titleTextStyle, color = config.titleColor, maxLines = 2)
                Text(relation.mediaType.name, style = config.subtitleTextStyle, color = config.subtitleColor)
            }
        }
    }
}


@Composable
private fun WideRelationCard(
    relation: MediaRelation,
    title: String,
    config: MediaRelationCardConfig,
    interactionSource: MutableInteractionSource,
    elevation: Dp,
    modifier: Modifier,
    onClick: (MediaRelation) -> Unit
) {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val imgScale by animateFloatAsState(
        if (isHovered) config.imageHoverScale else 1f,
        config.animationSpec
    )

    Card(
        onClick = { if (config.clickable) onClick(relation) },
        interactionSource = interactionSource,
        shape = config.shape,
        elevation = CardDefaults.cardElevation(elevation),
        modifier = modifier
            .fillMaxWidth()
            .height(config.height)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            // 🎞️ Left image
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
            ) {
                MediaAsyncImage(
                    url = relation.coverImage.orEmpty(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = imgScale
                            scaleY = imgScale
                        }
                )
            }

            // 📄 Right content
            Column(
                modifier = Modifier
                    .fillMaxHeight()

                    .padding(config.contentPadding),
                verticalArrangement = Arrangement.Center
            ) {

                // badges row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (config.showRelationBadge) {
                        RelationBadge(relation.relationType.name)
                    }
                    if (config.showFormatBadge) {
                        RelationBadge(relation.format.name)
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = title,
                    style = config.titleTextStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = relation.mediaType.name,
                    style = config.subtitleTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun RelationBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}


@Composable
@Preview
private fun MediaRelationCardPreview() {

    val url =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDdONirMFJN0clayQEa2Vx_ru6VZ5odnqWaDJuj5zlC5jwXZEqT65XD1JLXW9emHPwdZi3i30ptF8lEeO8hS5UVdB3JiomtyeL1FufZQR31T-P8GEAh3UpJ8kj8Pa1fQxlKZy9WFYOR9rmRSwb8VCUhFZzN70P6x5-6elg3TKhHMY2VY8aKEEg1B5FfHmmwicvXfItsQV9JHuL8j7QjxrPhKxOeFU5FmYE8shj2s6XPrV5_0FSpWoFqM23eOM-RQzi1K9-rJfwYqMQ"
    AppTheme {
        Column {
            MediaRelationCard(
                relation = MediaRelation(
                    id = 123,
                    relationType = RelationType.CHARACTER,
                    title = MediaTitle(
                        romaji = "Jujutsu Kaisen",
                        english = "JUJUTSU KAISEN",
                        native = "呪術廻戦"
                    ),
                    coverImage = url,
                    mediaType = MediaType.ANIME,
                    format = MediaFormat.TV
                ),
                config = MediaRelationCardConfig(layout = RelationCardLayout.WIDE),
                onClick = {}
            )
            Spacer(modifier = Modifier.size(8.dp))
            MediaRelationCard(
                relation = MediaRelation(
                    id = 123,
                    relationType = RelationType.CHARACTER,
                    title = MediaTitle(
                        romaji = "Jujutsu Kaisen",
                        english = "JUJUTSU KAISEN",
                        native = "呪術廻戦"
                    ),
                    coverImage = url,
                    mediaType = MediaType.ANIME,
                    format = MediaFormat.TV
                ),
                onClick = {}
            )
            Spacer(modifier = Modifier.size(8.dp))
            MediaRelationCard(
                relation = MediaRelation(
                    id = 123,
                    relationType = RelationType.CHARACTER,
                    title = MediaTitle(
                        romaji = "Jujutsu Kaisen",
                        english = "JUJUTSU KAISEN",
                        native = "呪術廻戦"
                    ),
                    coverImage = url,
                    mediaType = MediaType.MANGA,
                    format = MediaFormat.TV
                ),
                onClick = {}
            )
            Spacer(modifier = Modifier.size(8.dp))
            MediaRelationCard(
                relation = MediaRelation(
                    id = 123,
                    relationType = RelationType.CHARACTER,
                    title = MediaTitle(
                        romaji = "Jujutsu Kaisen",
                        english = "JUJUTSU KAISEN",
                        native = "呪術廻戦"
                    ),
                    coverImage = url,
                    mediaType = MediaType.UNKNOWN,
                    format = MediaFormat.TV
                ),
                onClick = {}
            )
        }
    }

}