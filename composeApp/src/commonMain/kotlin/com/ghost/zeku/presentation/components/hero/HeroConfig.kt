package com.ghost.zeku.presentation.components.hero

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class HeroConfig(
    val cornerRadius: Dp = 24.dp,
    val aspectRatioDesktop: Float = 21f / 9f,
    val aspectRatioMobile: Float = 4f / 5f,

    val scrimIntensity: Float = 0.9f,

    val contentPaddingDesktop: PaddingValues = PaddingValues(48.dp),
    val contentPaddingMobile: PaddingValues = PaddingValues(16.dp),

    val titleMaxLines: Int = 2,
    val descriptionMaxLines: Int = 3,

    val showDescriptionOnMobile: Boolean = false,
    val showGenres: Boolean = true,
    val showBadge: Boolean = true,

    val hoveredScale: Float = 1.06f,
    val pressedScale: Float = 1.03f,

    val enableHoverZoom: Boolean = true
)


