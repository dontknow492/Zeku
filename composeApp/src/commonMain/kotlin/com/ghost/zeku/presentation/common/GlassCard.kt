package com.ghost.zeku.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp


@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        onClick = onClick ?: {}
    ) {
        content()
    }
}

@Composable
fun GlassCardV2(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val baseModifier = modifier
        .clip(shape)

    val glassColor = colors.surface.copy(alpha = 0.35f)

    val borderColor = colors.outlineVariant.copy(alpha = 0.25f)

    val contentModifier = Modifier
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    glassColor.copy(alpha = 0.6f),
                    glassColor
                )
            )
        )
        .border(BorderStroke(1.dp, borderColor), shape)
        .padding(contentPadding)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = baseModifier,
            color = Color.Transparent,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            shape = shape
        ) {
            Column(contentModifier, content = content)
        }
    } else {
        Surface(
            modifier = baseModifier,
            color = Color.Transparent,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            shape = shape
        ) {
            Column(contentModifier, content = content)
        }
    }
}

