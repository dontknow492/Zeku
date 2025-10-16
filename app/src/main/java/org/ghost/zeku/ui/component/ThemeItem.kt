package org.ghost.zeku.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.zeku.ui.theme.AppTheme
import org.ghost.zeku.ui.theme.ZekuTheme

/**
 * A composable that displays a theme preview and its name.
 * It handles selection state and user interaction.
 *
 * @param theme The app theme to be displayed.
 * @param selected Whether this theme is currently selected.
 * @param onSelectionChange Callback invoked when this item is clicked.
 * @param modifier The modifier to be applied to the Column.
 */
@Composable
fun ThemeItem(
    modifier: Modifier = Modifier,
    theme: AppTheme,
    selected: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onSelectionChange: (AppTheme) -> Unit
) {
    // ZekuTheme applies the specific theme to the preview composables inside it.
    ZekuTheme(
        appTheme = theme,
        dynamicColor = false,
        darkTheme = isDarkTheme
    ) {
        Column(
            modifier = modifier
                .padding(8.dp)
                .clickable(
                    onClick = { onSelectionChange(theme) },
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The card's border color changes based on the selection state.
            OutlinedCard(
                border = BorderStroke(
                    width = 2.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                // The actual visual preview of the theme's colors.
                ThemePreviewItem()
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = theme.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeItemPreview() {
    LazyVerticalGrid(
        GridCells.Fixed(2),
    ) {
        items(items = AppTheme.themes, key = { it.name }) { theme ->
            ThemeItem(theme = theme, selected = true, onSelectionChange = {})
        }
    }
}

/**
 * A stateless composable that creates an abstract, aesthetic preview of the current theme's colors.
 * It arranges colored circles to represent the primary, secondary, tertiary, and surface variant colors.
 *
 * @param modifier The modifier to be applied to the preview Box.
 */
@Composable
fun ThemePreviewItem(modifier: Modifier = Modifier, selected: Boolean = false) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(width = 100.dp, height = 160.dp)
            .background(colorScheme.surface)
            .clip(CardDefaults.outlinedShape) // Ensure content respects card's rounded corners
    ) {
        // A Box for layering the color swatches
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Swatch for Surface Variant color
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceVariant)
            )

            // Swatch for Tertiary color
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 48.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorScheme.tertiary)
            )

            // Swatch for Secondary color
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 24.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colorScheme.secondary)
            )

            // Swatch for Primary color
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary)
            )
        }
    }
}