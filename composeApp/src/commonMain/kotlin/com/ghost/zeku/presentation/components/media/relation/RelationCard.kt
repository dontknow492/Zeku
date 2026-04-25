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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.RelationType
import com.ghost.zeku.domain.model.enum.getPreferred
import com.ghost.zeku.domain.model.media.MediaRelation
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.theme.AppTheme

@Composable
fun MediaRelationCard(
    relation: MediaRelation,
    config: MediaRelationCardConfig = MediaRelationCardConfig(),
    modifier: Modifier = Modifier,
    onClick: (MediaRelation) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val targetScale = when {
        isPressed -> config.scaleOnPress
        isHovered -> config.scaleOnHover
        else -> 1f
    }

    val targetElevation = when {
        isPressed -> config.pressedElevation
        isHovered -> config.hoverElevation
        else -> config.elevation
    }

    val imageScale = if (isHovered) config.imageHoverScale else 1f

    val scale by animateFloatAsState(targetScale, config.animationSpec)
    val elevation by animateDpAsState(targetElevation, config.elevationAnimationSpec)
    val imgScale by animateFloatAsState(imageScale, config.animationSpec)

    val title = relation.title.getPreferred(config.preferredTitleLanguage)

    Card(
        onClick = { if (config.clickable) onClick(relation) },
        interactionSource = interactionSource,
        shape = config.shape,
        colors = CardDefaults.cardColors(containerColor = config.containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = modifier
            .width(config.width)
            .aspectRatio(config.aspectRatio)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box {

            // 🎞️ Cover image
            MediaAsyncImage(
                url = relation.coverImage.orEmpty(),
                contentDescription = title,
                contentScale = config.contentScale,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = imgScale
                        scaleY = imgScale
                    }
            )

            // 🌑 Gradient
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(config.gradient)
            )

            // 🔗 Relation badge (top-left)
            if (config.showRelationBadge) {
                RelationBadge(
                    text = relation.relationType.name,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }

            // 🎬 Format badge (top-right)
            if (config.showFormatBadge) {
                RelationBadge(
                    text = relation.format.name,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            // 📝 Bottom info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(config.contentPadding)
            ) {
                Text(
                    text = title,
                    style = config.titleTextStyle,
                    color = config.titleColor,
                    maxLines = 2
                )

                Text(
                    text = relation.mediaType.name,
                    style = config.subtitleTextStyle,
                    color = config.subtitleColor
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
                )
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
                )
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
                )
            )
        }
    }

}