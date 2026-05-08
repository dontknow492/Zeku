package com.ghost.zeku.presentation.components.media.poster

import androidx.compose.ui.unit.dp


object PosterDefaults {

    fun forLayout(layout: PosterLayout): PosterConfig {
        return when (layout) {

            PosterLayout.Minimal -> PosterConfig(
                cornerRadius = 12.dp,
                aspectRatio = 2f / 3f,

                enableHover = false,
                enablePress = true,

                scaleOnHover = 1f,
                scaleOnPress = 0.98f,

                enableShadow = false,
                enableGlow = false,

                enableBlurNsfw = true,

                showTitle = false,
                showSubtitle = false,
                showScore = false,
                showProgress = false,

                showGradientOverlay = false,

                maxTitleLines = 2,
                maxSubtitleLines = 1
            )

            PosterLayout.Compact -> PosterConfig(
                cornerRadius = 14.dp,
                aspectRatio = 1f,

                enableHover = true,
                enablePress = true,

                scaleOnHover = 1.02f,
                scaleOnPress = 0.97f,

                enableShadow = true,
                enableGlow = false,

                enableBlurNsfw = true,

                showTitle = true,
                showSubtitle = false,
                showScore = true,
                showProgress = false,

                showGradientOverlay = false,

                maxTitleLines = 1,
                maxSubtitleLines = 1
            )

            PosterLayout.Overlay -> PosterConfig(
                cornerRadius = 18.dp,
                aspectRatio = 2f / 3f,

                enableHover = true,
                enablePress = true,

                scaleOnHover = 1.05f,
                scaleOnPress = 0.96f,

                enableShadow = true,
                enableGlow = false,

                enableBlurNsfw = true,

                showTitle = true,
                showSubtitle = true,
                showScore = true,
                showProgress = true,

                showGradientOverlay = true,

                maxTitleLines = 2,
                maxSubtitleLines = 1
            )

            PosterLayout.Modern -> PosterConfig(
                cornerRadius = 18.dp,
                aspectRatio = 2f / 3f,

                enableHover = true,
                enablePress = true,

                scaleOnHover = 1.03f,
                scaleOnPress = 0.97f,

                enableShadow = true,
                enableGlow = false,

                enableBlurNsfw = true,

                showTitle = true,
                showSubtitle = true,
                showScore = true,
                showProgress = true,

                showGradientOverlay = true,

                maxTitleLines = 2,
                maxSubtitleLines = 1
            )
        }
    }
}