package com.ghost.zeku.presentation.components.media.poster

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.utils.serializer.*
import kotlinx.serialization.Serializable

// ============================================================================
// CONFIG SYSTEM
// ============================================================================
@Serializable
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
@Serializable
sealed class PosterLayout {
    @Serializable
    object Minimal : PosterLayout()

    @Serializable
    object Overlay : PosterLayout()

    @Serializable
    object Modern : PosterLayout()

    @Serializable
    object Compact : PosterLayout()
}

// ----------------------------------------------------------------------------
// Image Config
// ----------------------------------------------------------------------------

@Serializable
enum class ImageContentScale {
    CROP,
    FIT,
    FILL_WIDTH,
    FILL_HEIGHT,
    INSIDE
}

@Serializable
@Immutable
data class MediaImageConfig(
    val aspectRatio: Float = 2f / 3f,
    @Serializable(with = DpSerializer::class)
    val cornerRadius: Dp = 12.dp,

    @Serializable(with = ContentScaleSerializer::class)
    val contentScale: ContentScale = ContentScale.Crop,

    val enableHoverZoom: Boolean = true,
    val hoverZoomScale: Float = 1.08f,

    val scrim: List<@Serializable(with = ColorSerializer::class) Color> = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.7f)
    )
)

// ----------------------------------------------------------------------------
// Content Config
// ----------------------------------------------------------------------------
@Serializable
@Immutable
data class PosterContentConfig(
    @Serializable(with = DpSerializer::class)
    val width: Dp = 190.dp,
    @Serializable(with = PaddingValuesSerializer::class)
    val padding: PaddingValues = PaddingValues(8.dp),

    val titleMaxLines: Int = 2,
    val showSubtitle: Boolean = true,

    @Serializable(with = TextStyleSerializer::class)
    val titleStyle: TextStyle = TextStyle.Default,
    @Serializable(with = TextStyleSerializer::class)
    val subtitleStyle: TextStyle = TextStyle.Default,


    @Serializable(with = ColorSerializer::class)
    val titleColor: Color = Color.White,
    @Serializable(with = ColorSerializer::class)
    val subtitleColor: Color = Color.White.copy(alpha = 0.75f)
)

// ----------------------------------------------------------------------------
// Badge Config
// ----------------------------------------------------------------------------
@Serializable
enum class BadgePosition {
    TOP_START, TOP_END, BOTTOM_START, BOTTOM_END
}

@Serializable
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
@Serializable
@Immutable
data class PosterInteractionConfig(
    val scaleOnHover: Float = 1.03f,
    val scaleOnPress: Float = 0.96f,
    val enableHover: Boolean = true,
)

@Serializable
@Immutable
data class NsfwConfig(
    val enabled: Boolean = true,          // global toggle (user setting)
    val blurRadius: Float = 20f,          // blur strength
    val dimAlpha: Float = 0.6f,           // dark overlay
    val showLabel: Boolean = true,        // "NSFW" text
    val clickToReveal: Boolean = true     // tap to reveal
)


