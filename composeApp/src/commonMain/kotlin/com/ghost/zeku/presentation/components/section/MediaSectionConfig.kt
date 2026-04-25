package com.ghost.zeku.presentation.components.section

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MediaSectionConfig(

    val layout: SectionLayout = SectionLayout.HorizontalRow(),

    // Header
    val showHeader: Boolean = true,
    val showViewAll: Boolean = true,

    // Animation
    val enableEntranceAnimation: Boolean = true,

    // Loading
    val shimmerItemCount: Int = 4,

    // Empty state
    val emptyHeight: Dp = 120.dp
)