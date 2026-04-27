package com.ghost.zeku.presentation.components.media.episode

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Immutable
data class EpisodeCardConfig(

    val variant: EpisodeCardVariant = EpisodeCardVariant.MODERN,

    // Layout
    val height: Dp = 100.dp,
    val shape: Shape = RoundedCornerShape(16.dp),
    val padding: PaddingValues = PaddingValues(12.dp),

    // Colors
    val containerColor: Color = Color(0xFF1C1C1E),
    val titleColor: Color = Color.White,
    val subtitleColor: Color = Color.White.copy(alpha = 0.75f),
    val fillerColor: Color = Color(0xFFFFB74D),

    // Typography
    val titleStyle: TextStyle = TextStyle.Default,
    val subtitleStyle: TextStyle = TextStyle.Default,

    // Behavior
    val showDescription: Boolean = true,
    val showThumbnail: Boolean = true,

    // Animation
    val scaleOnHover: Float = 1.02f,
    val scaleOnPress: Float = 0.97f,
    val animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),

    val clickable: Boolean = true
)