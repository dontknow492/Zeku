package com.ghost.zeku.presentation.common.chips

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun GenreChip(
    onClick: () -> Unit,
    genre: String,
    shape: Shape = RoundedCornerShape(50)
) {
    MyChips(
        text = {
            Text(text = genre, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        shape = shape,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = {}
    )
}
