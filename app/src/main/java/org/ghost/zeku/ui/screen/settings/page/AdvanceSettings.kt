package org.ghost.zeku.ui.screen.settings.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.R
import org.ghost.zeku.ui.component.SettingItem
import org.ghost.zeku.ui.component.SettingTitle
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.AdvancedSettingsState
import org.ghost.zeku.ui.screen.settings.BackButton

sealed interface AdvancedSettingsEvent {
    data class OnCustomCommandChange(val customCommand: String) : AdvancedSettingsEvent
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
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onBackClick = onBackClick) }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            SettingTitle(stringResource(R.string.settings_advanced_title))
            // ### Custom Command ###
            val customCommandValue = state.customCommand.takeIf { it.isNotBlank() }
                ?: stringResource(R.string.settings_advanced_custom_command_not_set)
            SettingItem(
                title = stringResource(R.string.settings_advanced_custom_command_title),
                description = "${stringResource(R.string.settings_advanced_custom_command_description)}\n$customCommandValue",
                icon = ImageVector.vectorResource(R.drawable.rounded_terminal_24),
                onClick = { /* TODO: Handle Custom Command change */ }
            )

            // ### Skip Video Segments (Sponsor Block) ###
            SwitchSettingItem(
                title = stringResource(R.string.settings_advanced_sponsor_block_title),
                description = stringResource(R.string.settings_advanced_sponsor_block_description),
                icon = ImageVector.vectorResource(R.drawable.rounded_tag_24),
                checked = state.sponsorBlock,
                onSelectionChange = { /* TODO: event(AdvancedSettingsEvent.SetSponsorBlock(it)) */ }
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
            SettingItem(
                title = stringResource(R.string.settings_advanced_template_id_title),
                description = "${stringResource(R.string.settings_advanced_template_id_description)}\nCurrent Id: [${state.templateId}]",
                icon = ImageVector.vectorResource(R.drawable.rounded_123_24),
                onClick = { /* TODO: Handle Template ID change */ }
            )
        }
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
        ), event = { }, onBackClick = {}
    )
}