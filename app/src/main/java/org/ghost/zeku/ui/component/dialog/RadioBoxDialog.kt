package org.ghost.zeku.ui.component.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.zeku.R


/**
 * A data class to represent an item in the multi-select list.
 *
 * @param label The text to display for the item.
 * @param isSelected The current selection state of the item.
 */
data class RadioItem(
    val label: String,
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
fun RadioBoxDialog(
    title: String,
    selected: RadioItem?,
    items: List<RadioItem>,
    onItemClick: (item: RadioItem, index: Int) -> Unit,
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
                        isSelected = item == selected,
                        description = item.description,
                        isRadio = true,
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

@Preview
@Composable
fun RadioBoxDialogPreview() {
    var selectedItem by remember { mutableStateOf<RadioItem?>(null) }
    val items = remember {
        listOf(
            RadioItem("Option 1", "Description for option 1."),
            RadioItem("Option 2", "Description for option 2."),
            RadioItem("Option 3", "This option has a slightly longer description to test wrapping.")
        )
    }

    // Initialize selection with the first item for preview purposes
    if (selectedItem == null) {
        selectedItem = items.first()
    }

    RadioBoxDialog(
        title = "Select an Option",
        selected = selectedItem,
        items = items,
        onItemClick = { item, _ -> selectedItem = item },
        onDismissRequest = { /* Handle dismiss */ },
        onConfirmation = { /* Handle confirmation */ },
        confirmButtonText = "OK",
        dismissButtonText = "Dismiss"
    )
}

