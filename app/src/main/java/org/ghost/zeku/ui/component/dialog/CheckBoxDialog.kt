package org.ghost.zeku.ui.component.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.ghost.zeku.R

/**
 * A data class to represent an item in the multi-select list.
 *
 * @param label The text to display for the item.
 * @param isSelected The current selection state of the item.
 */
data class SelectableItem(
    val label: String,
    val isSelected: Boolean,
    val description: String? = null
)

/**
 * A reusable, stateless dialog for showing a list of items with checkboxes for multi-selection.
 *
 * This dialog follows Compose best practices by hoisting its state. The caller is responsible
 * for providing the list of `items` and handling the `onItemClick` callback to manage state.
 *
 * @param title The text to be displayed as the title of the dialog.
 * @param items The list of `SelectableItem`s to display.
 * @param onItemClick The callback that is triggered when an item's checkbox is toggled. It provides the index and the new checked state.
 * @param onDismissRequest Called when the user wants to dismiss the dialog.
 * @param onConfirmation Called when the user clicks the confirmation button.
 * @param modifier The modifier to be applied to the dialog.
 * @param confirmButtonText The text for the confirmation button.
 * @param dismissButtonText The text for the dismiss button.
 */
@Composable
fun MultiSelectCheckboxDialog(
    title: String,
    items: List<SelectableItem>,
    onItemClick: (item: SelectableItem, index: Int) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonText: String = stringResource(R.string.apply),
    dismissButtonText: String = stringResource(R.string.cancel)
) {
    // Using the same base AestheticDialog for a consistent look and feel
    AestheticDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        title = title,
        content = {
            // LazyColumn is used for performance, especially with long lists.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp) // Add some space between title and list
            ) {
                itemsIndexed(items) { index, item ->
                    SelectableRow(
                        label = item.label,
                        isSelected = item.isSelected,
                        description = item.description,
                        onClick = { onItemClick(item, index) }
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )
                    }
                }
            }
        },
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        showDismissButton = true
    )
}

/**
 * A single row in the dialog, containing a checkbox and a label.
 * The entire row is clickable to toggle the selection.
 */
@Composable
fun SelectableRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRadio: Boolean = false,
    description: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isRadio) {
            RadioButton(
                selected = isSelected,
                onClick = null // The Row's click handler does the work
            )
        }
        else{
            Checkbox(
                checked = isSelected,
                onCheckedChange = null // The Row's click handler does the work
            )
        }

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6
                )
            }
        }
    }
}


// --- Example Usage ---

@Preview
@Composable
fun MultiSelectCheckboxDialogPreview() {
    var items by remember {
        mutableStateOf(
            listOf(
                SelectableItem("Option 1", true, "Description for option 1"),
                SelectableItem("Option 2", false, "Description for option 2"),
                SelectableItem("Option 3", true)
            )
        )
    }
    MultiSelectCheckboxDialog(
        title = "Select Options",
        items = items,
        onItemClick = { clickedItem, _ ->
            items = items.map { if (it.label == clickedItem.label) it.copy(isSelected = !it.isSelected) else it }
        },
        onDismissRequest = {},
        onConfirmation = {}
    )
}
