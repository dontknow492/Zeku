package org.ghost.zeku.ui.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.core.enum.SettingEnum
import org.ghost.zeku.core.enum.VideoEncoding
import org.ghost.zeku.ui.component.dialog.InputDialog
import org.ghost.zeku.ui.component.dialog.RadioBoxDialog
import org.ghost.zeku.ui.component.dialog.RadioItem
import kotlin.enums.EnumEntries

// This is a helper data class for the UI. It's good practice.

@Composable
fun <T> RadioSettingEnumItem(
    modifier: Modifier = Modifier,
    selectedValue: T, // Renamed 'selected' to be clearer
    items: EnumEntries<T>,
    title: String,
    // The main description for the setting itself, shown when the dialog is closed.
    description: String,
    icon: ImageVector?,
    enabled: Boolean = true,
    onValueChange: (T) -> Unit, // Renamed to align with Compose conventions
) where T : Enum<T>, T : SettingEnum {

    val context = LocalContext.current

    var isDialogVisible by remember { mutableStateOf(false) }

    // This is the main UI the user sees. It's clean and simple.
    // We are assuming you have a generic 'SettingItem' that takes an onClick.
    SettingItem(
        modifier = modifier,
        title = title,
        description = description,
        icon = icon,
        enabled = enabled,
        onClick = { isDialogVisible = true }
    )

    if (isDialogVisible) {
        // --- This is where the magic happens ---

        // 1. A single, temporary state for the dialog session.
        //    It's of type T (the enum), which is our "source of truth".
        var tempSelection by remember { mutableStateOf(selectedValue) }

        // 2. We convert the enums to UI models *inside* the composable scope.
        //    This allows us to correctly use stringResource().
        val radioOptions = remember(items) {
            items.map { enumItem ->
                RadioItem(
                    label = enumItem.label,
                    description = enumItem.descriptionResId?.let { resId ->
                        context.getString(resId)
                    }
                )
            }
        }

        // 3. We find the selected UI item based on our temporary enum state.
        val selectedRadioItem = remember(tempSelection, radioOptions) {
            radioOptions.find { it.label == tempSelection.label }
        }

        RadioBoxDialog(
            title = title,
            selected = selectedRadioItem,
            items = radioOptions,

            onDismissRequest = { isDialogVisible = false }, // Simple and clean

            onConfirmation = {
                // On confirm, we commit the temporary state to the real state.
                onValueChange(tempSelection)
                isDialogVisible = false
            },

            onItemClick = { clickedItem, _ ->
                // When an item is clicked, we find the corresponding enum
                // and update our temporary state.
                val newSelection = items.first { it.label == clickedItem.label }
                tempSelection = newSelection
            }
        )
    }
}

@Composable
fun InputSettingItem(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    label: String,
    placeholder: String,
    description: String,
    isError: Boolean,
    icon: ImageVector?,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    extraContent: @Composable (() -> Unit)? = null
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    // This is the main UI the user sees. It's clean and simple.
    SettingItem(
        modifier = modifier,
        title = title,
        description = description,
        icon = icon,
        enabled = enabled,
        onClick = { isDialogVisible = true }
    )

    if (isDialogVisible) {
        var tempValue by remember { mutableStateOf(value) }
        InputDialog(
            value = tempValue,
            onValueChange = { tempValue = it },
            onDismissRequest = { isDialogVisible = false },
            onConfirmation = { onValueChange(tempValue) },
            title = title,
            label = label,
            isError = isError,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            extraContent = extraContent
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun InputSettingItemPreview() {
    var value by remember { mutableStateOf("Current Value") }
    MaterialTheme {
        Surface {
            InputSettingItem(
                value = value,
                onValueChange = { value = it },
                title = "Input Setting Title",
                label = "Input Label",
                placeholder = "Enter text here...",
                description = "This is the current value: $value",
                isError = false,
                icon = Icons.Default.Settings,
                enabled = true
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 360)
@Composable
fun RadioSettingEnumItemPreview() {
    MaterialTheme {
        // --- State Management for the Preview ---
        // This is what you would do in your actual screen or ViewModel.
        var currentSelection by remember { mutableStateOf(VideoEncoding.H264) }

        Surface {
            // --- Calling the Component ---
            RadioSettingEnumItem(
                selectedValue = currentSelection,
                items = VideoEncoding.entries,
                title = "Video Encoding",
                // The description shows the currently selected value's label.
                description = currentSelection.label,
                icon = Icons.Default.Settings,
                onValueChange = { newSelection ->
                    // In a real app, this would trigger a ViewModel event.
                    // For the preview, we just update the local state.
                    currentSelection = newSelection
                }
            )
        }
    }
}