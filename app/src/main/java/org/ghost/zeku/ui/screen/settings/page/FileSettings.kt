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
import org.ghost.zeku.ui.component.GroupSettingItem
import org.ghost.zeku.ui.component.SettingItem
import org.ghost.zeku.ui.component.SettingTitle
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.BackButton
import org.ghost.zeku.ui.screen.settings.FileSettingsState


sealed interface FilesSettingsEvent {
    data class OnAudioDirectoryChange(val value: String) : FilesSettingsEvent
    data class OnVideoDirectoryChange(val value: String) : FilesSettingsEvent
    data class OnCommandDirectoryChange(val value: String) : FilesSettingsEvent
    data class OnSubdirectoryPlaylistTitle(val value: Boolean) : FilesSettingsEvent
    data class OnVideoFilenameTemplateChange(val value: String) : FilesSettingsEvent
    data class OnAudioFilenameTemplateChange(val value: String) : FilesSettingsEvent
    data class OnDownloadArchive(val value: Boolean) : FilesSettingsEvent
    data class OnRestrictFilenames(val value: Boolean) : FilesSettingsEvent
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSettings(
    modifier: Modifier = Modifier,
    state: FileSettingsState,
    eventHandler: (FilesSettingsEvent) -> Unit,
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
            SettingTitle(stringResource(R.string.settings_files_title))
            SwitchSettingItem(
                title = stringResource(R.string.title_download_archive_files),
                description = stringResource(R.string.desc_download_archive_files),
                icon = ImageVector.vectorResource(R.drawable.rounded_download_24),
                checked = state.downloadArchive,
                onSelectionChange = { checked ->
                    eventHandler(
                        FilesSettingsEvent.OnDownloadArchive(
                            checked
                        )
                    )
                },
            )
            GroupSettingItem(
                title = stringResource(R.string.group_title_file_locations)
            ) {
                SettingItem(
                    title = stringResource(R.string.title_audio_save_location),
                    // Combines a static string with the dynamic path value
                    description = stringResource(
                        R.string.desc_audio_save_location,
                        state.audioDirectory
                    ),
                    icon = ImageVector.vectorResource(R.drawable.rounded_music_note_24),
                    onClick = { /*TODO*/ }
                )
                SettingItem(
                    title = stringResource(R.string.title_video_save_location),
                    description = stringResource(
                        R.string.desc_video_save_location,
                        state.videoDirectory
                    ),
                    icon = ImageVector.vectorResource(R.drawable.round_videocam_24),
                    onClick = { /*Todo*/ }
                )
                SettingItem(
                    title = stringResource(R.string.title_command_script_location),
                    description = stringResource(
                        R.string.desc_command_script_location,
                        state.commandDirectory
                    ),
                    icon = ImageVector.vectorResource(R.drawable.rounded_terminal_24),
                    onClick = { /*Todo*/ }
                )
            }
            GroupSettingItem(
                title = stringResource(R.string.group_title_filename_formatting),
            ) {
                SettingItem(
                    title = stringResource(R.string.title_audio_filename_format),
                    description = stringResource(
                        R.string.desc_audio_filename_format,
                        state.filenameTemplateAudio
                    ),
                    icon = ImageVector.vectorResource(R.drawable.rounded_music_note_24),
                    onClick = { /*Todo*/ }
                )
                SettingItem(
                    title = stringResource(R.string.title_video_filename_format),
                    description = stringResource(
                        R.string.desc_video_filename_format,
                        state.filenameTemplateVideo
                    ),
                    icon = ImageVector.vectorResource(R.drawable.round_videocam_24),
                    onClick = { /*Todo*/ }
                )

                SwitchSettingItem(
                    title = stringResource(R.string.title_limit_filename_characters),
                    description = stringResource(R.string.desc_limit_filename_characters),
                    icon = ImageVector.vectorResource(R.drawable.rounded_abc_24),
                    checked = state.restrictFilenames,
                    onSelectionChange = { checked ->
                        eventHandler(
                            FilesSettingsEvent.OnRestrictFilenames(
                                checked
                            )
                        )
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun FileSettingsPreview() {
    FileSettings(
        state = FileSettingsState(
            videoDirectory = "/storage/emulated/0/Download/Zeku/Video",
            audioDirectory = "/storage/emulated/0/Download/Zeku/Audio",
            commandDirectory = "/data/user/0/org.ghost.zeku/files/zeku",
            subdirectoryPlaylistTitle = false,
            filenameTemplateVideo = "%(title)s.%(ext)s",
            filenameTemplateAudio = "%(title)s.%(ext)s",
            downloadArchive = false,
            restrictFilenames = false
        ),
        onBackClick = {},
        eventHandler = {}
    )
}