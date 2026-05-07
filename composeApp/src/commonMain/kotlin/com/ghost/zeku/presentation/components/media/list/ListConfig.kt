package com.ghost.zeku.presentation.components.media.list

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.presentation.components.media.poster.MediaImageConfig
import com.ghost.zeku.presentation.components.media.poster.NsfwConfig
import com.ghost.zeku.utils.serializer.DpSerializer
import kotlinx.serialization.Serializable


@Serializable
@Immutable
data class ListConfig(
    val ui: MediaListUiConfig = MediaListUiConfig(),
    val image: MediaImageConfig = MediaImageConfig(),
    val nsfw: NsfwConfig = NsfwConfig(),
    val content: MediaListContentConfig = MediaListContentConfig(),
    val interaction: MediaListInteractionConfig = MediaListInteractionConfig()
)

@Serializable
data class MediaListInteractionConfig(
    val enableHover: Boolean = true,
    val hoverScale: Float = 1.01f,
    val pressedScale: Float = 0.97f,
    @Serializable(with = DpSerializer::class)
    val hoveredElevation: Dp = 8.dp,
    @Serializable(with = DpSerializer::class)
    val normalElevation: Dp = 2.dp
)

@Serializable
data class MediaListUiConfig(
    @Serializable(with = DpSerializer::class)
    val maxWidth: Dp = Dp.Unspecified, // 👈 desktop fix
    @Serializable(with = DpSerializer::class)
    val imageWidth: Dp = 90.dp,
    @Serializable(with = DpSerializer::class)
    val spacing: Dp = 16.dp,
    @Serializable(with = DpSerializer::class)
    val cornerRadius: Dp = 16.dp,
)

@Serializable
data class MediaListContentConfig(
    val showDescription: Boolean = true,
    val showGenres: Boolean = true,
    val showScore: Boolean = true,
    val showProgress: Boolean = true,
    val descriptionMaxLines: Int = 3
)

@Serializable
enum class MediaListCardVariant {
    COMPACT,   // dense (sidebar / desktop lists)
    COMFORTABLE, // default
    DETAILED   // large (tablet / featured)
}



