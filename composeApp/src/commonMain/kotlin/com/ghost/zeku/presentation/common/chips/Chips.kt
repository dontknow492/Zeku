package com.ghost.zeku.presentation.common.chips

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

@Composable
fun MyChips(
    text: @Composable () -> Unit,
    colors: ChipColors = AssistChipDefaults.assistChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(50),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {

    AssistChip(
        onClick = onClick,
        label = text,
        shape = shape,
        colors = colors,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled
    )
}


@Composable
fun MyChips(
    text: String,
    colors: ChipColors = AssistChipDefaults.assistChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(50),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    MyChips(
        text = { Text(text) },
        colors = colors,
        onClick = onClick,
        shape = shape,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled
    )
}