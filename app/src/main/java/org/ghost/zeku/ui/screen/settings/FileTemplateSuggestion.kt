package org.ghost.zeku.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.zeku.core.utils.FileTemplateUtils


data class Template(
    val labelResId: Int,
    val isSelected: Boolean = false // Default to not selected
)

/**
 * A responsive row of suggestion chips that wraps content to the next line.
 *
 * @param templates A map where the key is the String Resource ID for the label
 * and the value is the placeholder string to be returned on click.
 * @param onTemplateClick A callback that is invoked with the placeholder string
 * (e.g., "%(title)s") when a chip is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun FilenameTemplateSuggestion(
    templates: List<Template>,
    onTemplateClick: (Template) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp) // Use this if you expect multiple rows
    ) {
        // Iterate through the map to create a chip for each template
        templates.forEach { template ->
            TemplateChip(
                template = template,
                onClick = { onTemplateClick(template) }
            )
        }
    }
}


/**
 * A reusable composable for an individual suggestion chip.
 * It changes color based on its selection state.
 */
@Composable
private fun TemplateChip(
    template: Template,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (template.isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        AssistChipDefaults.assistChipColors().containerColor
    }

    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = { Text(
            stringResource(id = template.labelResId),
            style = MaterialTheme.typography.bodySmall
            )
        },
        colors = AssistChipDefaults.assistChipColors(containerColor = containerColor),
        leadingIcon = {
            if (template.isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FilenameTemplateSuggestionPreview() {
    // A dummy map for previewing the component


    FilenameTemplateSuggestion(
        templates = FileTemplateUtils.getAvailableTemplates().map { Template(it.key, !false) },
        onTemplateClick = { placeholder ->
            // In a real app, you would use this placeholder
            // to update your TextField's state.
            println("Chip clicked: $placeholder")
        }
    )
}