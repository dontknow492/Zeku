package com.ghost.zeku.presentation.components.hero

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class HeroCarouselConfig(
    val enableAutoScroll: Boolean = true,
    val autoScrollDuration: Long = 5000, // milliseconds
    val peek: Dp = 40.dp, // How much of next/prev card is visible
    val pageSpacing: Dp = 16.dp,
    val scaleMin: Float = 0.85f,
    val alphaMin: Float = 0.6f,
    val enableParallax: Boolean = true,
    val parallaxOffset: Float = 60f,
    val showEdgeGradients: Boolean = true,
    val showIndicators: Boolean = true,
    val beyondViewportPageCount: Int = 1
)