package com.ghost.zeku.presentation.components.media.character

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.media.CharacterRole
import com.ghost.zeku.domain.model.media.MediaCharacter
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.theme.AppTheme

@Composable
fun MediaCharacterCard(
    modifier: Modifier = Modifier,
    character: MediaCharacter,
    config: MediaCharacterCardConfig = MediaCharacterCardConfig(),
    onClick: (MediaCharacter) -> Unit,
) {
    val isMain = character.role == CharacterRole.MAIN

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    // 🎯 Target states
    val targetScale = when {
        isPressed -> config.scaleOnPress
        isHovered -> config.scaleOnHover
        else -> 1f
    }

    val targetElevation = when {
        isPressed -> config.pressedElevation
        isHovered -> config.hoverElevation
        else -> if (isMain) config.mainElevation else config.normalElevation
    }

    val imageScale = when {
        isHovered -> config.imageHoverScale
        else -> 1f
    }

    // 🎞️ Animations
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = config.animationSpec
    )

    val animatedElevation by animateDpAsState(
        targetValue = targetElevation,
        animationSpec = config.elevationAnimationSpec
    )

    val animatedImageScale by animateFloatAsState(
        targetValue = imageScale,
        animationSpec = config.animationSpec
    )

    Card(
        onClick = { if (config.clickable) onClick(character) },
        interactionSource = interactionSource,
        shape = config.shape,
        colors = CardDefaults.cardColors(
            containerColor = config.containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box {

            // 🎬 Image with zoom effect
            MediaAsyncImage(
                url = character.imageUrl.orEmpty(),
                contentDescription = character.name,
                contentScale = config.contentScale,
                modifier = Modifier
                    .width(config.imageWidth)
                    .aspectRatio(config.aspectRatio)
                    .graphicsLayer {
                        scaleX = animatedImageScale
                        scaleY = animatedImageScale
                    }
            )

            // 🌑 Gradient overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(config.gradient)
            )

            // 🏷️ Role badge
            if (config.showRoleBadge) {
                RoleBadge(
                    role = character.role,
                    style = config.roleBadgeStyle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            // 📝 Bottom content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(config.contentPadding)
            ) {
                Text(
                    text = character.name,
                    style = config.nameTextStyle,
                    color = config.nameColor,
                    maxLines = 1
                )

                if (config.showRoleText) {
                    Text(
                        text = character.role.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = config.roleTextStyle,
                        color = config.roleTextColor
                    )
                }
            }
        }
    }
}


@Composable
fun RoleBadge(
    role: CharacterRole,
    style: RoleBadgeStyle,
    modifier: Modifier = Modifier
) {
    val color = when (role) {
        CharacterRole.MAIN -> MaterialTheme.colorScheme.primary
        CharacterRole.SUPPORTING -> MaterialTheme.colorScheme.secondary
        else -> style.backgroundColor
    }

    Box(
        modifier = modifier
            .clip(style.shape)
            .background(color.copy(alpha = 0.85f))
            .padding(style.padding)
    ) {
        Text(
            text = role.name,
            style = MaterialTheme.typography.labelSmall,
            color = style.contentColor
        )
    }
}


@Composable
@Preview
private fun MediaCharacterCardPreview() {
    val url =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDdONirMFJN0clayQEa2Vx_ru6VZ5odnqWaDJuj5zlC5jwXZEqT65XD1JLXW9emHPwdZi3i30ptF8lEeO8hS5UVdB3JiomtyeL1FufZQR31T-P8GEAh3UpJ8kj8Pa1fQxlKZy9WFYOR9rmRSwb8VCUhFZzN70P6x5-6elg3TKhHMY2VY8aKEEg1B5FfHmmwicvXfItsQV9JHuL8j7QjxrPhKxOeFU5FmYE8shj2s6XPrV5_0FSpWoFqM23eOM-RQzi1K9-rJfwYqMQ"
    AppTheme {
        Column {
            MediaCharacterCard(
                character = MediaCharacter(
                    name = "Ren Arisawa",
                    role = CharacterRole.MAIN,
                    imageUrl = url,
                    id = 123,
                ),
                onClick = {}
            )
            MediaCharacterCard(
                character = MediaCharacter(
                    name = "Ren Arisawa",
                    role = CharacterRole.SUPPORTING,
                    imageUrl = url,
                    id = 123
                ),
                onClick = {}
            )
        }
    }

}