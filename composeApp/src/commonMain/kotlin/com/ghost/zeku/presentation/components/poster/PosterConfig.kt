package com.ghost.zeku.presentation.components.poster

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PosterConfig(
    val width: Dp = 140.dp,
    val aspectRatio: Float = 2f / 3f,
    val cornerRadius: Dp = 12.dp,

    val titleMaxLines: Int = 2,
    val showScore: Boolean = true,
    val showBadge: Boolean = true,
    val showProgress: Boolean = true,

    val elevation: Dp = 2.dp,
    val hoveredScale: Float = 1.02f,
    val pressedScale: Float = 0.96f,
)

