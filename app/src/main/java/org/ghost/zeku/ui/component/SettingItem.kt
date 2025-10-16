package org.ghost.zeku.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingTitle(text: String) {
    Text(
        modifier = Modifier
//            .padding(top = 32.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        text = text,
        style = MaterialTheme.typography.displaySmall,
    )
}

@Composable
private fun SettingColumn(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector?,
    enabled: Boolean = true,
) {
    // Define the standard alpha for disabled content
    val disabledAlpha = 0.48f

    // Set the title color based on the enabled state
    val titleColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = disabledAlpha)
    }

    // Set the description color based on the enabled state
    val descriptionColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = disabledAlpha)
    }

    Column(
        modifier = modifier
            .padding(start = if (icon == null) 12.dp else 0.dp)
    ) {
        Text(
            text = title,
            maxLines = 1,
            style = MaterialTheme.typography.titleLarge,
            color = titleColor, // Use the dynamic color
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            maxLines = 3,
            style = MaterialTheme.typography.bodyMedium,
            color = descriptionColor, // Use the dynamic color
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector?,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.surface
    } else {
        // A subtle, theme-aware color for the disabled state
        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }
    Surface(
        modifier = modifier.toggleable(
            value = enabled, // The current state (Boolean)
            onValueChange = { onClick() }, // Lambda that receives the new state,
            enabled = enabled,
            role = Role.Button
        ),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.48f
                    ),
                )
            }
            SettingColumn(
                modifier = Modifier.weight(1f),
                title = title,
                description = description,
                icon = icon,
                enabled = enabled,
            )
        }
    }
}

@Composable
fun SwitchSettingItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector?,
    checked: Boolean,
    enabled: Boolean = true,
    onSelectionChange: (Boolean) -> Unit
) {
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.surface
    } else {
        // A subtle, theme-aware color for the disabled state
        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }
    Surface(
//        modifier = modifier.clickable { onSelectionChange(!checked) },
        modifier = modifier.toggleable(
            value = checked, // The current state (Boolean)
            onValueChange = onSelectionChange, // Lambda that receives the new state
            enabled = enabled,
            role = Role.Switch // Or Role.Switch, for accessibility services
        ),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.48f
                    ),
                )
            }
            SettingColumn(
                modifier = Modifier.weight(1f),
                title = title,
                description = description,
                icon = icon,
                enabled = enabled,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = onSelectionChange,
                enabled = enabled,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
fun GroupSettingItem(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
//            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Preview
@Composable
fun SwitchSettingItemPreview() {
    Column {
        SwitchSettingItem(
            title = "Title",
            description = "Description",
            icon = null,
            checked = !false,
            onSelectionChange = {}
        )
        SwitchSettingItem(
            title = "Title",
            description = "Description",
            icon = null,
            checked = true,
            enabled = false,
            onSelectionChange = {}
        )
    }
}


@Preview
@Composable
private fun SettingItemPreview() {
    Column {
        SettingItem(
            title = "Title",
            description = "Description",
            icon = null,
            onClick = {}
        )
        SettingItem(
            title = "Title",
            description = "Description",
            icon = null,
            onClick = {},
            enabled = false
        )
    }
}

@Preview
@Composable
private fun SettingTitlePreview() {
    SettingTitle("Title")
}