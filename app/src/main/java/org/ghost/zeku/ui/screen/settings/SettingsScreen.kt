package org.ghost.zeku.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import org.ghost.zeku.ui.navigation.SettingsRoute


@Composable
fun BackButton(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    IconButton(onClick = onBackClick) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSettingClick: (SettingsRoute) -> Unit
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
            SettingTitle(stringResource(R.string.settings))
            // General
            SettingItem(
                title = stringResource(R.string.settings_general_title),
                description = stringResource(R.string.settings_general_description),
                icon = Icons.Filled.Settings,
                onClick = { onSettingClick(SettingsRoute.GeneralSettings) }
            )

            // Files & Folders
            SettingItem(
                title = stringResource(R.string.settings_files_title),
                description = stringResource(R.string.settings_files_description),
                icon = ImageVector.vectorResource(R.drawable.baseline_folder_24),
                onClick = { onSettingClick(SettingsRoute.FileAndDirectorySettings) }
            )

            // Appearance
            SettingItem(
                title = stringResource(R.string.settings_appearance_title),
                description = stringResource(R.string.settings_appearance_description),
                icon = ImageVector.vectorResource(R.drawable.baseline_palette_24),
                onClick = { onSettingClick(SettingsRoute.InterfaceSettings) }
            )

            // Media & Formats
            SettingItem(
                title = stringResource(R.string.settings_media_title),
                description = stringResource(R.string.settings_media_description),
                icon = ImageVector.vectorResource(R.drawable.outline_album_24),
                onClick = { onSettingClick(SettingsRoute.MediaSettings) }
            )

            // Network
            SettingItem(
                title = stringResource(R.string.settings_network_title),
                description = stringResource(R.string.settings_network_description),
                icon = ImageVector.vectorResource(R.drawable.network_24),
                onClick = { onSettingClick(SettingsRoute.NetworkSettings) }
            )

            // Post-Processing
            SettingItem(
                title = stringResource(R.string.settings_post_processing_title),
                description = stringResource(R.string.settings_post_processing_description),
                icon = Icons.Filled.Star,
                onClick = { onSettingClick(SettingsRoute.PostProcessingSettings) }
            )
            // Subtitle
            SettingItem(
                title = stringResource(R.string.settings_subtitle_title),
                description = stringResource(R.string.settings_subtitle_description),
                icon = ImageVector.vectorResource(R.drawable.baseline_subtitles_24),
                onClick = { onSettingClick(SettingsRoute.SubtitlesSettings) }
            )


            // Advanced
            SettingItem(
                title = stringResource(R.string.settings_advanced_title),
                description = stringResource(R.string.settings_advanced_description),
                icon = Icons.Filled.Build,
                onClick = { onSettingClick(SettingsRoute.AdvancedSettings) }
            )

            // About
            SettingItem(
                title = stringResource(R.string.settings_about_title),
                description = stringResource(R.string.settings_about_description),
                icon = Icons.Filled.Info,
                onClick = { onSettingClick(SettingsRoute.About) }
            )

        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(onBackClick = {}, onSettingClick = {})

}