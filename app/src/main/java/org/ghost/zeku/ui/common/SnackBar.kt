package org.ghost.zeku.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ghost.zeku.R

@Composable
fun ErrorSnackBar(modifier: Modifier = Modifier, snackbarData: SnackbarData) {
    Snackbar(
        modifier = Modifier.padding(12.dp),
        // The action is the "close" button
        action = {
            TextButton(
                onClick = { snackbarData.dismiss() },
            ) {
                Text(
                    text = snackbarData.visuals.actionLabel ?: stringResource(R.string.dismiss),
                    color = MaterialTheme.colorScheme.onErrorContainer // Use a color that contrasts with the background
                )
            }
        },
        // Use Material 3 error colors for a consistent theme
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        // This is the main message text
        Text(snackbarData.visuals.message)
    }
}