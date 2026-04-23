package com.ghost.zeku.presentation.components.poster

import androidx.compose.ui.unit.dp

object PosterDefaults {

    fun config(style: PosterStyle): PosterConfig {
        return when (style) {

            PosterStyle.MINIMAL -> PosterConfig(
                width = 120.dp,
                aspectRatio = 2f / 3f,
                cornerRadius = 8.dp,

                titleMaxLines = 2,
                showScore = false,
                showBadge = false,
                showProgress = false,

                elevation = 0.dp,
                hoveredScale = 1.02f,
                pressedScale = 0.96f
            )

            PosterStyle.COMPACT -> PosterConfig(
                width = 100.dp,
                aspectRatio = 3f / 4f,
                cornerRadius = 8.dp,

                titleMaxLines = 1,
                showScore = false,
                showBadge = true,
                showProgress = false,

                elevation = 0.dp,
                hoveredScale = 1.01f,
                pressedScale = 0.95f
            )

            PosterStyle.OVERLAY -> PosterConfig(
                width = 180.dp,
                aspectRatio = 2f / 3f,
                cornerRadius = 12.dp,

                titleMaxLines = 2,
                showScore = false,
                showBadge = false,
                showProgress = true,

                elevation = 0.dp,
                hoveredScale = 1.03f,
                pressedScale = 0.96f
            )

            PosterStyle.MODERN -> PosterConfig(
                width = 150.dp,
                aspectRatio = 2f / 3f,
                cornerRadius = 16.dp,

                titleMaxLines = 1,
                showScore = true,
                showBadge = false,
                showProgress = false,

                elevation = 2.dp,
                hoveredScale = 1.02f,
                pressedScale = 0.97f
            )
        }
    }
}