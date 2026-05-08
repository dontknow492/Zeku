package com.ghost.zeku.presentation.components.media.list

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.utils.serializer.DpSerializer
import kotlinx.serialization.Serializable


@Serializable
@Immutable
data class ListCardConfig(

    // shape
    @Serializable(with = DpSerializer::class)
    val cornerRadius: Dp = 14.dp,

    // sizing
    @Serializable(with = DpSerializer::class)
    val imageWidth: Dp = 72.dp,
    val aspectRatio: Float = 2f / 3f,

    // interactions
    val enableHover: Boolean = true,
    val enablePress: Boolean = true,

    val scaleOnHover: Float = 1.01f,
    val scaleOnPress: Float = 0.98f,

    @Serializable(with = DpSerializer::class)
    val elevation: Dp = 1.dp,

    // visuals
    val enableShadow: Boolean = false,
    val enableDivider: Boolean = false,

    // content visibility
    val showSubtitle: Boolean = true,
    val showDescription: Boolean = false,
    val showProgress: Boolean = true,
    val showScore: Boolean = true,
    val showGenres: Boolean = false,

    // layout behavior
    val maxTitleLines: Int = 1,
    val maxSubtitleLines: Int = 1,
    val maxDescriptionLines: Int = 2,

    // extras
    val showActions: Boolean = true,
    val showTrailingIcon: Boolean = true
)




