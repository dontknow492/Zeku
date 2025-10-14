package org.ghost.zeku.ui.screen.settings.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.R
import org.ghost.zeku.core.DownloadType
import org.ghost.zeku.core.enum.PreventDuplicateDownload
import org.ghost.zeku.ui.component.GroupSettingItem
import org.ghost.zeku.ui.component.SettingItem
import org.ghost.zeku.ui.component.SettingTitle
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.BackButton
import org.ghost.zeku.ui.screen.settings.GeneralSettingsState
import org.ghost.zeku.ui.theme.ZekuTheme

sealed interface GeneralSettingsEvent {
    // Event for a boolean switch changing
    data class UpdateConfigure(val isEnabled: Boolean) : GeneralSettingsEvent
    data class UpdateDebug(val isEnabled: Boolean) : GeneralSettingsEvent
    data class UpdateNotification(val isEnabled: Boolean) : GeneralSettingsEvent
    data class UpdateAutoUpdate(val isEnabled: Boolean) : GeneralSettingsEvent
    data class UpdatePrivateMode(val isEnabled: Boolean) : GeneralSettingsEvent
    data class UpdatePreventDuplicates(val value: PreventDuplicateDownload) : GeneralSettingsEvent

    // Event for a text or selection-based setting changing
    data class UpdateChannel(val channel: String) : GeneralSettingsEvent
    data class UpdatePreferredDownloadType(val type: DownloadType) : GeneralSettingsEvent

    // Event for a simple button click with no parameters
    object ResetToDefaults : GeneralSettingsEvent
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettings(
    modifier: Modifier = Modifier,
    state: GeneralSettingsState,
    eventHandler: (GeneralSettingsEvent) -> Unit,
    onBackClick: () -> Unit,
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
            SettingTitle(stringResource(R.string.settings_general_title))
            SwitchSettingItem(
                title = stringResource(R.string.title_always_show_config),
                description = stringResource(R.string.desc_always_show_config),
                checked = state.configure,
                icon = Icons.Filled.Edit,
                onSelectionChange = { checked ->
                    eventHandler(GeneralSettingsEvent.UpdateConfigure(checked))
                }
            )

            SwitchSettingItem(
                title = stringResource(R.string.title_download_status_notifications),
                description = stringResource(R.string.desc_download_status_notifications),
                checked = state.notification,
                icon = Icons.Filled.Notifications,
                onSelectionChange = { checked ->
                    eventHandler(GeneralSettingsEvent.UpdateNotification(checked))
                }
            )

            GroupSettingItem(
                title = stringResource(R.string.group_title_download_management)
            ) {
                SettingItem(
                    title = stringResource(R.string.title_prevent_duplicate_downloads),
                    // Value-based description with dynamic content
                    description = stringResource(
                        R.string.desc_prevent_duplicate_downloads,
                        state.preventDuplicateDownloads.description
                    ),
                    icon = ImageVector.vectorResource(R.drawable.rounded_download_24),
                    onClick = { }
                )
                SettingItem(
                    title = stringResource(R.string.title_preferred_content_type),
                    // Value-based description with dynamic content
                    description = stringResource(
                        R.string.desc_preferred_content_type,
                        state.preferredDownloadType.toString().uppercase()
                    ),
                    icon = ImageVector.vectorResource(R.drawable.baseline_perm_media_24),
                    onClick = { }
                )
            }

            GroupSettingItem(
                title = stringResource(R.string.group_title_privacy_security)
            ) {
                SwitchSettingItem(
                    title = stringResource(R.string.title_private_mode),
                    description = stringResource(R.string.desc_private_mode),
                    checked = state.privateMode,
                    icon = ImageVector.vectorResource(R.drawable.incognito),
                    onSelectionChange = { checked ->
                        eventHandler(GeneralSettingsEvent.UpdatePrivateMode(checked))
                    }
                )
            }

            GroupSettingItem(
                title = stringResource(R.string.group_title_advanced)
            ) {
                SwitchSettingItem(
                    title = stringResource(R.string.title_automatic_updates),
                    description = stringResource(R.string.desc_automatic_updates),
                    checked = state.autoUpdate,
                    icon = ImageVector.vectorResource(R.drawable.rounded_update_24),
                    onSelectionChange = { checked ->
                        eventHandler(GeneralSettingsEvent.UpdateAutoUpdate(checked))
                    }
                )
                SwitchSettingItem(
                    title = stringResource(R.string.title_enable_debug_logging),
                    description = stringResource(R.string.desc_enable_debug_logging),
                    checked = state.debug,
                    icon = ImageVector.vectorResource(R.drawable.round_bug_report_24),
                    onSelectionChange = { checked ->
                        eventHandler(GeneralSettingsEvent.UpdateDebug(checked))
                    }
                )
            }
            Button(
                onClick = { eventHandler(GeneralSettingsEvent.ResetToDefaults) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.button_reset_all_settings),
                    modifier = Modifier
                )
                Icon(
                    painter = painterResource(R.drawable.rounded_reset_wrench_24),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
private fun GeneralSettingsPreview() {
    ZekuTheme {
        GeneralSettings(
            state = GeneralSettingsState(
                configure = false,
                debug = true,
                welcomeDialog = true,
                notification = true,
                autoUpdate = true,
                updateChannel = "Stable",
                privateMode = false,
                preventDuplicateDownloads = PreventDuplicateDownload.TYPE_AND_URL,
                preferredDownloadType = DownloadType.Video
            ), eventHandler = {}, onBackClick = {})
    }
}