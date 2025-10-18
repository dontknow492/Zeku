package org.ghost.zeku.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.zeku.R
import org.ghost.zeku.database.models.CookieItem

@Composable
fun CookieListItem(
    modifier: Modifier = Modifier,
    cookie: CookieItem,
    selected: Boolean = false,
    onLongClick: (CookieItem) -> Unit,
    onClick: (CookieItem) -> Unit,
    onEnableChange: (CookieItem, Boolean) -> Unit,
) {
    ListItem(
        modifier = modifier
            .padding(horizontal = 8.dp) // Add horizontal padding for list context
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = { onClick(cookie) },
                onLongClick = { onLongClick(cookie) }
            ),
        headlineContent = {
            Text(
                text = cookie.url,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(
                text = cookie.content,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            // Icon changes based on selection state for a clear visual cue
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.baseline_cookie_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        trailingContent = {
            Box(
                modifier = Modifier.fillMaxHeight(), // Fills the vertical space of the list item
                contentAlignment = Alignment.Center // Centers the Switch within the Box
            ) {
                Switch(
                    checked = cookie.enabled,
                    onCheckedChange = { isChecked -> onEnableChange(cookie, isChecked) }
                )
            }

        },
        // Use custom colors to reflect the selection state
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    )
}

@Preview
@Composable
fun CookieListItemPreview() {
    val cookie = CookieItem(
        id = 1,
        url = "https://example.com",
        content = "session_id=abc123xyz; user_id=42; preference=dark",
        description = "A sample cookie for preview",
        enabled = true
    )
    Scaffold { innerpadding ->
        CookieListItem(
            modifier = Modifier.padding(innerpadding),
            cookie = cookie,
            onClick = {},
            onLongClick = {},
            onEnableChange = { _, _ -> }
        )
    }

}