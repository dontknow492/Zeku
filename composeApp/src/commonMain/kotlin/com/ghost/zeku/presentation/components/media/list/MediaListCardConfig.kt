package com.ghost.zeku.presentation.components.media.list

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.presentation.components.media.poster.MediaImageConfig
import com.ghost.zeku.presentation.components.media.poster.NsfwConfig

@Immutable
data class MediaListCardConfig(
    val ui: MediaListUiConfig = MediaListUiConfig(),
    val image: MediaImageConfig = MediaImageConfig(),
    val nsfw: NsfwConfig = NsfwConfig(),
    val content: MediaListContentConfig = MediaListContentConfig(),
    val interaction: MediaListInteractionConfig = MediaListInteractionConfig()
)


data class MediaListInteractionConfig(
    val enableHover: Boolean = true,
    val hoverScale: Float = 1.01f,
    val pressedScale: Float = 0.97f,
    val hoveredElevation: Dp = 8.dp,
    val normalElevation: Dp = 2.dp
)

data class MediaListUiConfig(
    val maxWidth: Dp = Dp.Unspecified, // 👈 desktop fix
    val imageWidth: Dp = 90.dp,
    val spacing: Dp = 16.dp,
    val cornerRadius: Dp = 16.dp,
)

data class MediaListContentConfig(
    val showDescription: Boolean = true,
    val showGenres: Boolean = true,
    val showScore: Boolean = true,
    val showProgress: Boolean = true,
    val descriptionMaxLines: Int = 3
)

enum class MediaListCardVariant {
    COMPACT,   // dense (sidebar / desktop lists)
    COMFORTABLE, // default
    DETAILED   // large (tablet / featured)
}



