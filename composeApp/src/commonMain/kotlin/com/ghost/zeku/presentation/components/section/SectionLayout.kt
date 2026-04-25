package com.ghost.zeku.presentation.components.section

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class SectionLayout {

    data class HorizontalRow(
        val itemSpacing: Dp = 16.dp,
        val contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
    ) : SectionLayout()

    data class VerticalList(
        val itemSpacing: Dp = 8.dp,
        val contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
    ) : SectionLayout()

    data class Grid(
        val columns: GridType = GridType.Adaptive(140.dp),
        val verticalSpacing: Dp = 12.dp,
        val horizontalSpacing: Dp = 12.dp,
        val contentPadding: PaddingValues = PaddingValues(16.dp)
    ) : SectionLayout()
}

sealed class GridType {
    data class Fixed(val count: Int) : GridType()
    data class Adaptive(val minSize: Dp) : GridType()
}

