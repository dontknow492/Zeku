package com.ghost.zeku.presentation.components.media.list

import androidx.compose.ui.unit.dp

object ListCardDefaults {

    fun forLayout(layout: ListCardLayout): ListCardConfig {
        return when (layout) {

            ListCardLayout.Minimal -> ListCardConfig(
                cornerRadius = 10.dp,
                imageWidth = 64.dp,
                aspectRatio = 2f / 3f,

                enableHover = false,
                enablePress = true,

                scaleOnHover = 1f,
                scaleOnPress = 0.98f,

                enableShadow = false,
                enableDivider = false,

                showSubtitle = false,
                showDescription = false,
                showProgress = false,
                showScore = false,
                showGenres = false,

                maxTitleLines = 1,
                maxSubtitleLines = 1,
                maxDescriptionLines = 2,

                showActions = false,
                showTrailingIcon = false
            )

            ListCardLayout.Compact -> ListCardConfig(
                cornerRadius = 12.dp,
                imageWidth = 68.dp,
                aspectRatio = 2f / 3f,

                enableHover = true,
                enablePress = true,

                scaleOnHover = 1.01f,
                scaleOnPress = 0.97f,

                enableShadow = false,
                enableDivider = false,

                showSubtitle = true,
                showDescription = false,
                showProgress = true,
                showScore = true,
                showGenres = false,

                maxTitleLines = 1,
                maxSubtitleLines = 1,
                maxDescriptionLines = 2,

                showActions = true,
                showTrailingIcon = true
            )

            ListCardLayout.Modern -> ListCardConfig(
                cornerRadius = 14.dp,
                imageWidth = 72.dp,
                aspectRatio = 2f / 3f,

                enableHover = true,
                enablePress = true,

                scaleOnHover = 1.02f,
                scaleOnPress = 0.97f,

                enableShadow = true,
                enableDivider = false,

                showSubtitle = true,
                showDescription = false,
                showProgress = true,
                showScore = true,
                showGenres = false,

                maxTitleLines = 1,
                maxSubtitleLines = 1,
                maxDescriptionLines = 2,

                showActions = true,
                showTrailingIcon = true
            )

            ListCardLayout.Detailed -> ListCardConfig(
                cornerRadius = 16.dp,
                imageWidth = 80.dp,
                aspectRatio = 2f / 3f,

                enableHover = true,
                enablePress = true,

                scaleOnHover = 1.03f,
                scaleOnPress = 0.96f,

                enableShadow = true,
                enableDivider = true,

                showSubtitle = true,
                showDescription = true,
                showProgress = true,
                showScore = true,
                showGenres = true,

                maxTitleLines = 2,
                maxSubtitleLines = 2,
                maxDescriptionLines = 3,

                showActions = true,
                showTrailingIcon = true
            )
        }
    }
}