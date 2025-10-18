package org.ghost.zeku.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import org.ghost.zeku.R

@Composable
fun SelectionBottomNavigation(
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
    onInvertSelectionClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onClearSelectionClick: () -> Unit
) {
    NavigationBar(
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onSelectAllClick,
            label = { Text(text = stringResource(R.string.select_all)) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_select_all_24),
                    contentDescription = stringResource(R.string.select_all)
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = onInvertSelectionClick,
            label = { Text(text = stringResource(R.string.invert)) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_select_window_24),
                    contentDescription = stringResource(R.string.invert)
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = onClearSelectionClick,
            label = { Text(text = stringResource(R.string.clear)) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_deselect_24),
                    contentDescription = stringResource(R.string.clear)
                )
            }
        )
        NavigationBarItem(
            selected = false,
            label = { Text(text = stringResource(R.string.delete)) },
            onClick = onDeleteClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete)
                )
            }
        )

    }
}