package com.ghost.zeku.presentation.components.character

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MediaCharacterCardConfig(

    // Layout
    val imageWidth: Dp = 180.dp,
    val aspectRatio: Float = 2 / 3f,
    val shape: Shape = RoundedCornerShape(20.dp),
    val contentPadding: PaddingValues = PaddingValues(12.dp),

    // Colors
    val containerColor: Color = Color(0xFF1C1C1E),
    val gradient: Brush = Brush.verticalGradient(
        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
    ),
    val nameColor: Color = Color.White,
    val roleTextColor: Color = Color.White.copy(alpha = 0.8f),

    // Typography
    val nameTextStyle: TextStyle = TextStyle.Default,
    val roleTextStyle: TextStyle = TextStyle.Default,

    // Role badge
    val showRoleBadge: Boolean = true,
    val roleBadgeStyle: RoleBadgeStyle = RoleBadgeStyle(),
    val showRoleText: Boolean = true,

    // Elevation
    val mainElevation: Dp = 8.dp,
    val normalElevation: Dp = 2.dp,
    val hoverElevation: Dp = 10.dp,
    val pressedElevation: Dp = 1.dp,

    // Animation
    val enableScaleAnimation: Boolean = true,
    val scaleOnPress: Float = 0.96f,
    val scaleOnHover: Float = 1.04f,
    val imageHoverScale: Float = 1.08f,
    val animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    val elevationAnimationSpec: AnimationSpec<Dp> = spring(),

    // Image
    val contentScale: ContentScale = ContentScale.Crop,

    // Interaction
    val clickable: Boolean = true
)


@Immutable
data class RoleBadgeStyle(
    val backgroundColor: Color = Color.Black.copy(alpha = 0.6f),
    val contentColor: Color = Color.White,
    val shape: Shape = RoundedCornerShape(50),
    val padding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 4.dp),

    )