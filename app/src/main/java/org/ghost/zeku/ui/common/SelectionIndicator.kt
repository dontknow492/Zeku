package org.ghost.zeku.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.zeku.R

// Assuming you have a 'state' object that contains 'selectedIds: Set<Long>'
// Example:
// data class MyScreenState(val selectedIds: Set<Long> = emptySet())
// val state by remember { mutableStateOf(MyScreenState(setOf(1L, 2L, 3L))) }

@Composable
fun SelectedItemsIndicator(
    selectedIds: Set<Long>, // Pass the selectedIds directly
    modifier: Modifier = Modifier
) {
    val count = selectedIds.size

    // Only show the indicator if there are selected items
    if (count > 0) {
        Row(
            modifier = modifier.padding(8.dp), // Overall padding for the row
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The Box will create the circular background for the number
            Box(
                modifier = Modifier
//                    .size(28.dp) // Fixed size for the circular background
                    .background(
                        color = MaterialTheme.colorScheme.primary, // Use your primary color
                        shape = CircleShape // Makes it a circle
                    )
                    .size(28.dp),
                contentAlignment = Alignment.Center // Centers the text inside the Box
            ) {
                Text(
                    text = count.toString(),
                    color = MaterialTheme.colorScheme.onPrimary, // Text color contrasting with primary
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(8.dp)) // Space between "Selected:" and the count circle
            Text(
                text = stringResource(R.string.selected),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )



        }
    } else {
        // Optionally show "Select items" or nothing if no items are selected
        Text(
            text = "Select items",
            modifier = modifier.padding(8.dp),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun SelectedItemsIndicatorPreview() {
    val selectedIds = remember { setOf(1L, 2L, 3L) } // Example set of selected IDs

    SelectedItemsIndicator(selectedIds = selectedIds)

}