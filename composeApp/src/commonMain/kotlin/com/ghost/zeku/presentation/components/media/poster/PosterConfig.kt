package com.ghost.zeku.presentation.components.media.poster

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.utils.serializer.DpSerializer
import kotlinx.serialization.Serializable


@Serializable
@Immutable
data class PosterConfig(
    // shape
    @Serializable(with = DpSerializer::class)
    val cornerRadius: Dp = 18.dp,

    // ratio
    val aspectRatio: Float = 2f / 3f,

    // interactions
    val enableHover: Boolean = true,
    val enablePress: Boolean = true,

    val scaleOnHover: Float = 1.03f,
    val scaleOnPress: Float = 0.97f,

    // effects
    val enableShadow: Boolean = true,
    val enableGlow: Boolean = false,

    val enableBlurNsfw: Boolean = true,

    // metadata visibility
    val showTitle: Boolean = true,
    val showSubtitle: Boolean = true,
    val showScore: Boolean = true,
    val showProgress: Boolean = true,

    // overlay
    val showGradientOverlay: Boolean = true,

    // typography behavior
    val maxTitleLines: Int = 2,
    val maxSubtitleLines: Int = 1
)



