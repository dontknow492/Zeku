package org.ghost.zeku.ui.component.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.R

/**
 * A reusable Material 3 dialog for confirming a destructive action, such as deletion.
 *
 * @param modifier The modifier to be applied to the dialog.
 * @param onDismissRequest Called when the user tries to dismiss the dialog by clicking outside
 * or pressing the back button.
 * @param onConfirm Called when the user clicks the confirm button. The caller is responsible
 * for hiding the dialog and performing the delete action.
 * @param title The title of the dialog, summarizing the action.
 * @param text The main text of the dialog, providing more details about the consequences.
 * @param icon The icon to be displayed at the top of the dialog. Defaults to a warning icon.
 */
@Composable
fun ConfirmDeleteDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String = stringResource(R.string.confirm_deletion),
    text: String = stringResource(R.string.default_delete_message),
    icon: ImageVector = Icons.Outlined.Warning
) {
    AestheticDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirm,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = "Warning Icon",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = title,
        content = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    )
}

// --- Preview ---

@Preview(showBackground = true)
@Composable
private fun ConfirmDeleteDialogPreview() {
    // Wrap in your app's theme or a default MaterialTheme for accurate preview
    MaterialTheme {
        Surface {
            ConfirmDeleteDialog(
                onDismissRequest = { /* Preview action */ },
                onConfirm = { /* Preview action */ },
                title = "Delete Note?",
                text = "The note 'Meeting Highlights' will be permanently removed."
            )
        }
    }
}