package com.ghost.zeku.presentation.common.chips

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

@Composable
fun GenreChip(
    genre: String,
    shape: Shape = RoundedCornerShape(50)
) {
    AssistChip(
        onClick = {},
        label = { Text(genre) },
        shape = shape,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
