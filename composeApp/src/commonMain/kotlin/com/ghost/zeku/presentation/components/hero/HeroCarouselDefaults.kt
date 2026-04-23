package com.ghost.zeku.presentation.components.hero

import androidx.compose.ui.unit.dp

object HeroCarouselDefaults {
    fun config(isDesktop: Boolean): HeroCarouselConfig {
        return if (isDesktop) {
            HeroCarouselConfig(
                enableAutoScroll = true,
                autoScrollDuration = 6000,
                peek = 80.dp,      // Show more of next/prev on desktop
                pageSpacing = 24.dp,
                scaleMin = 0.85f,
                alphaMin = 0.7f,
                enableParallax = true,
                parallaxOffset = 80f,
                showEdgeGradients = true,
                showIndicators = true,
                beyondViewportPageCount = 1
            )
        } else {
            HeroCarouselConfig(
                enableAutoScroll = true,
                autoScrollDuration = 5000,
                peek = 24.dp,      // Smaller peek on mobile
                pageSpacing = 12.dp,
                scaleMin = 0.9f,
                alphaMin = 0.8f,
                enableParallax = false, // Disable parallax on mobile for performance
                parallaxOffset = 0f,
                showEdgeGradients = false,
                showIndicators = true,
                beyondViewportPageCount = 1
            )
        }
    }
}