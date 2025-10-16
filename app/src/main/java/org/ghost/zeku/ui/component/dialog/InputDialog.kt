package org.ghost.zeku.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


/**
 * A reusable, stateless dialog for capturing a single text input from the user.
 *
 * This dialog follows Compose best practices by hoisting its state. The caller is responsible
 * for providing the `type` and handling the `onValueChange` callback.
 *
 * @param value The current text to display in the text field.
 * @param onValueChange The callback that is triggered when the user enters text.
 * @param onDismissRequest Called when the user wants to dismiss the dialog.
 * @param onConfirmation Called when the user clicks the confirmation button.
 * @param modifier The modifier to be applied to the dialog.
 * @param title The text to be displayed as the title of the dialog.
 * @param label The text to be displayed as the label for the text field.
 * @param placeholder The text to be displayed when the text field is empty.
 * @param isError Indicates if the current input is in an error state.
 * @param leadingIcon An optional icon to display at the beginning of the text field.
 * @param trailingIcon An optional icon to display at the end of the text field.
 * @param keyboardOptions Software keyboard options.
 * @param keyboardActions Actions to be triggered by the software keyboard.
 */
@Composable
fun InputDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    label: String,
    placeholder: String,
    isError: Boolean,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    extraContent: @Composable (() -> Unit)? = null
) {
    // This part remains the same, great UX!
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // Adding a small delay can sometimes help ensure the dialog is fully
        // composed before requesting focus.
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    // Using your base dialog (renamed for consistency)
    AestheticDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation, // The caller now decides what to do on confirm
        title = title,
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    // Switched to OutlinedTextField for a more modern look
                    value = value, // Use the hoisted state
                    onValueChange = onValueChange, // Notify the caller of changes
                    label = { Text(label) },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    isError = isError,
                    singleLine = false,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start),
                )
                if (extraContent != null) {
                    HorizontalDivider()
                    extraContent()
                }
            }
        },
        confirmButtonText = "Ok",
        dismissButtonText = "Cancel",
        showDismissButton = true
    )
}


// --- Example Usage ---

@Composable
@Preview

fun InputDialogPreview() {
    var showDialog by remember { mutableStateOf(false) }
    // 1. The state is now owned by the parent (the "source of truth").
    var name by remember { mutableStateOf("") }
    var hasError = remember { derivedStateOf { name.isBlank() } }

    if (!showDialog) {
        InputDialog(
            title = "Enter Your Name",
            label = "Name",
            placeholder = "e.g., Jane Doe",
            value = name, // 2. Pass the state down.
            onValueChange = { name = it }, // 3. Listen for changes and update the state.
            onConfirmation = {
                if (!hasError.value) {
                    showDialog = false
                    // Now you can use the validated 'name'
                    println("User entered: $name")
                }
            },
            onDismissRequest = { showDialog = false },
            isError = hasError.value // You can now do real-time validation
        )
    }
}

