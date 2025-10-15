package org.ghost.zeku.ui.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.ghost.zeku.R

/**
 * A beautifully designed, reusable dialog composable based on Material Design 3.
 *
 * This dialog provides slots for an icon, title, and custom content, along with
 * customizable confirmation and dismissal buttons.
 *
 * @param onDismissRequest Called when the user requests to dismiss the dialog (e.g., by tapping outside or pressing the back button).
 * @param onConfirmation Called when the user clicks the confirmation button.
 * @param modifier The modifier to be applied to the dialog's card.
 * @param icon An optional composable to be displayed at the top of the dialog.
 * @param title The text to be displayed as the title of the dialog.
 * @param content The main content of the dialog.
 * @param confirmButtonText The text for the confirmation button. Defaults to "Confirm".
 * @param dismissButtonText The text for the dismiss button. Defaults to "Dismiss".
 * @param showDismissButton Whether to show the dismiss button. Defaults to true.
 */
@Composable
fun AestheticDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: String,
    content: @Composable () -> Unit,
    confirmButtonText: String = stringResource(R.string.confirm),
    dismissButtonText: String = stringResource(R.string.dismiss),
    showDismissButton: Boolean = true,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Optional Icon
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(bottom = 8.dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        icon()
                    }
                }

                // 2. Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                ProvideTextStyle(
                    value = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    content()
                }

                // 4. Buttons

                DialogButton(
                    modifier = Modifier.fillMaxWidth(),
                    onDismissRequest = onDismissRequest,
                    onConfirmation = onConfirmation,
                    showDismissButton = showDismissButton,
                    confirmButtonText = confirmButtonText,
                    dismissButtonText = dismissButtonText
                )
            }
        }
    }
}


@Composable
fun SimpleDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit,
    confirmButtonText: String = stringResource(R.string.confirm),
    dismissButtonText: String = stringResource(R.string.dismiss),
    showDismissButton: Boolean = true,
){
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // 1. Optional Icon
                // 2. Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                content()

                // 4. Buttons

                DialogButton(
                    modifier = Modifier.fillMaxWidth(),
                    onDismissRequest = onDismissRequest,
                    onConfirmation = onConfirmation,
                    showDismissButton = showDismissButton,
                    confirmButtonText = confirmButtonText,
                    dismissButtonText = dismissButtonText
                )
            }
        }
    }
}


@Composable
fun DialogButton(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    showDismissButton: Boolean = true,
    confirmButtonText: String = stringResource(R.string.confirm),
    dismissButtonText: String = stringResource(R.string.dismiss),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDismissButton) {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(dismissButtonText)
            }
        }
        TextButton(
            onClick = onConfirmation,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(confirmButtonText)
        }
    }
}

// --- Example Usage ---

@Composable
@Preview
fun DialogPreview() {
    // State to control dialog visibility
    var showDialog by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            Button(onClick = { showDialog = true }) {
                Text("Show Dialog")
            }
        }

        if (!showDialog) {
            SimpleDialog(
                onDismissRequest = { showDialog = false },
                onConfirmation = {
                    showDialog = false
                    // Handle confirmation action (e.g., save data)
                },
                title = "Action Successful",
//                icon = {
//                    Icon(
//                        imageVector = Icons.Outlined.CheckCircle,
//                        contentDescription = "Success Icon",
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(48.dp)
//                    )
//                },
                content = {
                    TextField(value = "asdf", onValueChange = {})
//                    Text("Your changes have been saved successfully. You can continue using the app.")
                },
                confirmButtonText = "Got it!",
                showDismissButton = true
            )
        }
    }
}
