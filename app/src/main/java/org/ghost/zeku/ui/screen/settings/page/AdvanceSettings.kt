package org.ghost.zeku.ui.screen.settings.page

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.R
import org.ghost.zeku.ui.common.SettingScaffold
import org.ghost.zeku.ui.component.InputSettingItem
import org.ghost.zeku.ui.component.SettingItem
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.AdvancedSettingsState

sealed interface AdvancedSettingsEvent {
    data class OnSponsorBlockChange(val sponsorBlock: Boolean) : AdvancedSettingsEvent
    data class OnSponsorBlockCategoriesChange(val sponsorBlockCategories: Set<String>) :
        AdvancedSettingsEvent

    data class OnTemplateIdChange(val templateId: Int) : AdvancedSettingsEvent
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvanceSettings(
    modifier: Modifier = Modifier,
    state: AdvancedSettingsState,
    event: (AdvancedSettingsEvent) -> Unit,
    onBackClick: () -> Unit,
    onCustomCommandClick: () -> Unit,
) {
    SettingScaffold(
        modifier = modifier,
        title = stringResource(R.string.settings_advanced_title),
        error = state.error,
        onBackClick = onBackClick
    ) {
        // ### Custom Command ###
        SettingItem(
            title = stringResource(R.string.settings_advanced_custom_command_title),
            description = "${stringResource(R.string.settings_advanced_custom_command_description)}",
            icon = ImageVector.vectorResource(R.drawable.rounded_terminal_24),
            onClick = onCustomCommandClick
        )

        // ### Skip Video Segments (Sponsor Block) ###
        SwitchSettingItem(
            title = stringResource(R.string.settings_advanced_sponsor_block_title),
            description = stringResource(R.string.settings_advanced_sponsor_block_description),
            icon = ImageVector.vectorResource(R.drawable.rounded_tag_24),
            checked = state.sponsorBlock,
            onSelectionChange = { skip -> event(AdvancedSettingsEvent.OnSponsorBlockChange(skip)) }
        )

        // ### Skippable Categories ###
        val categoriesValue = if (state.sponsorBlockCategories.isNotEmpty()) {
            state.sponsorBlockCategories.joinToString(", ")
        } else {
            stringResource(R.string.settings_advanced_sponsor_block_categories_none)
        }
        SettingItem(
            title = stringResource(R.string.settings_advanced_sponsor_block_categories_title),
            description = "${stringResource(R.string.settings_advanced_sponsor_block_categories_description)}\nCategories: [$categoriesValue]",
            icon = ImageVector.vectorResource(R.drawable.baseline_category_24),
            enabled = state.sponsorBlock,
            onClick = { /* TODO: Handle Sponsor Block Categories change */ }
        )

        // ### Command Template ###
        InputSettingItem(
            value = state.templateId.toString(),
            onValueChange = { id ->
                event(
                    AdvancedSettingsEvent.OnTemplateIdChange(
                        id.toIntOrNull() ?: 0
                    )
                )
            },
            title = stringResource(R.string.settings_advanced_template_id_title),
            description = "${stringResource(R.string.settings_advanced_template_id_description)}\nCurrent Id: [${state.templateId}]",
            icon = ImageVector.vectorResource(R.drawable.rounded_123_24),
            label = stringResource(R.string.template_id),
            placeholder = stringResource(R.string.template_placeholder),
            isError = !state.isValidTemplateId,
        )
    }
}

@Preview
@Composable
fun AdvanceSettingsPreview() {
    AdvanceSettings(
        state = AdvancedSettingsState(
            customCommand = "",
            sponsorBlock = true,
            sponsorBlockCategories = setOf("sponsor", "intro"),
            templateId = 0
        ), event = { }, onBackClick = {}, onCustomCommandClick = {}
    )
}