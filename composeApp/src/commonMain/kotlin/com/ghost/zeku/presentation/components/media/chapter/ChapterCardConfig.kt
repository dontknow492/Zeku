package com.ghost.zeku.presentation.components.media.chapter

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
data class ChapterCardConfig(

    val variant: ChapterCardVariant = ChapterCardVariant.MINIMAL,

    // Layout
    val height: Dp = 72.dp,
    val shape: Shape = RoundedCornerShape(14.dp),
    val padding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),

    // Colors
    val containerColor: Color = Color(0xFF1C1C1E),
    val titleColor: Color = Color.White,
    val subtitleColor: Color = Color.White.copy(alpha = 0.7f),
    val accentColor: Color = Color(0xFF90CAF9),

    // Typography
    val titleStyle: TextStyle = TextStyle.Default,
    val subtitleStyle: TextStyle = TextStyle.Default,

    // Behavior
    val showVolume: Boolean = true,

    // Animation
    val scaleOnHover: Float = 1.015f,
    val scaleOnPress: Float = 0.97f,
    val animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),

    val clickable: Boolean = true
)


fun formatChapterNumber(number: Float): String {
    return if (number % 1f == 0f) {
        number.toInt().toString()
    } else {
        number.toString()
    }
}