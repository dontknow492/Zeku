package org.ghost.zeku.ui.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.zeku.R
import org.ghost.zeku.database.models.CommandTemplate

@Composable
fun CommandTemplateItem(
    modifier: Modifier = Modifier,
    commandTemplate: CommandTemplate,
    selected: Boolean = false,
    onLongClick: (CommandTemplate) -> Unit,
    onClick: (CommandTemplate) -> Unit
) {
    ListItem(
        modifier = modifier
            .padding(horizontal = 8.dp) // Add horizontal padding for list context
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = { onClick(commandTemplate) },
                onLongClick = { onLongClick(commandTemplate) }
            ),
        headlineContent = {
            Text(
                text = commandTemplate.title,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(
                text = commandTemplate.content,
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
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_code_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
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
private fun CommandTemplatePreview() {
    val commandTemplate = CommandTemplate(
        id = 1,
        title = "Sample Title",
        content = "This is some sample content for the command template. It can be quite long to test the text overflow.",
        useAsExtraCommand = false,
        useAsExtraCommandAudio = true,
        useAsExtraCommandVideo = true,
        useAsExtraCommandDataFetching = false
    )
    CommandTemplateItem(
        commandTemplate = commandTemplate,
        onLongClick = {},
        onClick = {}
    )
}

@Preview
@Composable
private fun CommandTemplateSelectedPreview() {


    val commandTemplate = CommandTemplate(
        id = 1,
        title = "Selected Command",
        content = "This is a selected command template to show the visual difference in the UI.",
        useAsExtraCommand = false,
        useAsExtraCommandAudio = true,
        useAsExtraCommandVideo = true,
        useAsExtraCommandDataFetching = false
    )
    CommandTemplateItem(
        commandTemplate = commandTemplate,
        selected = true,
        onLongClick = {},
        onClick = {}
    )
}