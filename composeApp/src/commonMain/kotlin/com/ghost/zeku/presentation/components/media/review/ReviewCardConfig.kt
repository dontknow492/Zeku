package com.ghost.zeku.presentation.components.media.review

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class ReviewCardConfig(

    val showAvatar: Boolean = true,
    val showScore: Boolean = true,
    val showSummary: Boolean = true,
    val showUpvotes: Boolean = true,
    val showDate: Boolean = true,

    val maxCollapsedLines: Int = 3,
    val enableExpand: Boolean = true,


    val shape: Shape = RoundedCornerShape(20.dp),

    val padding: PaddingValues = PaddingValues(16.dp),

    val animationSpec: FiniteAnimationSpec<Float> = tween(200)
)
