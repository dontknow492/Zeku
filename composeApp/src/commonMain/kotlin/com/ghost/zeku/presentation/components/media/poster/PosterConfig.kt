package com.ghost.zeku.presentation.components.media.poster

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// CONFIG SYSTEM
// ============================================================================

data class PosterConfig(
    val layout: PosterLayout = PosterLayout.Overlay,
    val image: MediaImageConfig = MediaImageConfig(),
    val content: PosterContentConfig = PosterContentConfig(),
    val badges: BadgeConfig = BadgeConfig(),
    val interaction: PosterInteractionConfig = PosterInteractionConfig(),

    val nsfw: NsfwConfig = NsfwConfig() // 👈 new
)

// ----------------------------------------------------------------------------
// Layout
// ----------------------------------------------------------------------------

sealed class PosterLayout {
    object Minimal : PosterLayout()
    object Overlay : PosterLayout()
    object Modern : PosterLayout()
    object Compact : PosterLayout()
}

// ----------------------------------------------------------------------------
// Image Config
// ----------------------------------------------------------------------------

@Immutable
data class MediaImageConfig(
    val aspectRatio: Float = 2f / 3f,
    val cornerRadius: Dp = 12.dp,
    val contentScale: ContentScale = ContentScale.Crop,

    val enableHoverZoom: Boolean = true,
    val hoverZoomScale: Float = 1.08f,

    val scrim: Brush = Brush.verticalGradient(
        listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
    )
)

// ----------------------------------------------------------------------------
// Content Config
// ----------------------------------------------------------------------------

@Immutable
data class PosterContentConfig(
    val width: Dp = 190.dp,
    val padding: PaddingValues = PaddingValues(8.dp),

    val titleMaxLines: Int = 2,
    val showSubtitle: Boolean = true,

    val titleStyle: TextStyle = TextStyle.Default,
    val subtitleStyle: TextStyle = TextStyle.Default,

    val titleColor: Color = Color.White,
    val subtitleColor: Color = Color.White.copy(alpha = 0.75f)
)

// ----------------------------------------------------------------------------
// Badge Config
// ----------------------------------------------------------------------------

enum class BadgePosition {
    TOP_START, TOP_END, BOTTOM_START, BOTTOM_END
}

@Immutable
data class BadgeConfig(
    val showScore: Boolean = true,
    val showBadge: Boolean = true,
    val showProgress: Boolean = true,

    val scorePosition: BadgePosition = BadgePosition.TOP_END,
    val badgePosition: BadgePosition = BadgePosition.TOP_START
)

// ----------------------------------------------------------------------------
// Interaction Config
// ----------------------------------------------------------------------------

@Immutable
data class PosterInteractionConfig(
    val scaleOnHover: Float = 1.03f,
    val scaleOnPress: Float = 0.96f,
    val enableHover: Boolean = true,
    val animationSpec: AnimationSpec<Float> =
        spring(stiffness = Spring.StiffnessLow)
)

@Immutable
data class NsfwConfig(
    val enabled: Boolean = true,          // global toggle (user setting)
    val blurRadius: Float = 20f,          // blur strength
    val dimAlpha: Float = 0.6f,           // dark overlay
    val showLabel: Boolean = true,        // "NSFW" text
    val clickToReveal: Boolean = true     // tap to reveal
)


