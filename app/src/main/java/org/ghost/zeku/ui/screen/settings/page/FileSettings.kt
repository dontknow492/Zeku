package org.ghost.zeku.ui.screen.settings.page

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.zeku.R
import org.ghost.zeku.core.utils.FileTemplateUtils
import org.ghost.zeku.ui.common.SettingScaffold
import org.ghost.zeku.ui.component.GroupSettingItem
import org.ghost.zeku.ui.component.InputSettingItem
import org.ghost.zeku.ui.component.SettingItem
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.FileSettingsState
import org.ghost.zeku.ui.screen.settings.FilenameTemplateSuggestion
import org.ghost.zeku.ui.screen.settings.Template


sealed interface FilesSettingsEvent {
    data class OnAudioDirectoryChange(val value: Uri?) : FilesSettingsEvent
    data class OnVideoDirectoryChange(val value: Uri?) : FilesSettingsEvent
    data class OnCommandDirectoryChange(val value: Uri?) : FilesSettingsEvent
    data class OnSubdirectoryPlaylistTitle(val value: Boolean) : FilesSettingsEvent
    data class OnVideoFilenameTemplateChange(val value: String) : FilesSettingsEvent
    data class OnAudioFilenameTemplateChange(val value: String) : FilesSettingsEvent
    data class OnDownloadArchive(val value: Boolean) : FilesSettingsEvent
    data class OnRestrictFilenames(val value: Boolean) : FilesSettingsEvent

    data class OnAudioTemplateChange(val value: Template) : FilesSettingsEvent

    data class OnVideoTemplateChange(val value: Template) : FilesSettingsEvent
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSettings(
    modifier: Modifier = Modifier,
    state: FileSettingsState,
    eventHandler: (FilesSettingsEvent) -> Unit,
    onBackClick: () -> Unit
) {
    SettingScaffold(
        modifier = modifier,
        title = stringResource(R.string.settings_files_title),
        onBackClick = onBackClick
    ) {

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
            DirectorySettingItem(
                title = stringResource(R.string.title_audio_save_location),
                // Combines a static string with the dynamic path value
                description = stringResource(
                    R.string.desc_audio_save_location,
                    state.audioDirectory
                ),
                icon = ImageVector.vectorResource(R.drawable.rounded_music_note_24),
                onDirectoryChange = { uri ->
                    eventHandler(FilesSettingsEvent.OnAudioDirectoryChange(uri))
                }
            )
            DirectorySettingItem(
                title = stringResource(R.string.title_video_save_location),
                description = stringResource(
                    R.string.desc_video_save_location,
                    state.videoDirectory
                ),
                icon = ImageVector.vectorResource(R.drawable.round_videocam_24),
                onDirectoryChange = { uri ->
                    eventHandler(FilesSettingsEvent.OnVideoDirectoryChange(uri))
                }
            )
            DirectorySettingItem(
                title = stringResource(R.string.title_command_script_location),
                description = stringResource(
                    R.string.desc_command_script_location,
                    state.commandDirectory
                ),
                icon = ImageVector.vectorResource(R.drawable.rounded_terminal_24),
                onDirectoryChange = { uri ->
                    eventHandler(FilesSettingsEvent.OnCommandDirectoryChange(uri))
                }

            )
        }
        GroupSettingItem(
            title = stringResource(R.string.group_title_filename_formatting),
        ) {
            InputSettingItem(
                value = state.filenameTemplateAudio,
                onValueChange = { value ->
                    eventHandler(
                        FilesSettingsEvent.OnAudioFilenameTemplateChange(
                            value
                        )
                    )
                },
                title = stringResource(R.string.title_audio_filename_format),
                description = stringResource(
                    R.string.desc_audio_filename_format,
                    state.filenameTemplateAudio
                ),
                label = stringResource(R.string.audio_template_label),
                placeholder = stringResource(R.string.filename_template_placeholder),
                icon = ImageVector.vectorResource(R.drawable.rounded_music_note_24),
                isError = !state.isValidFilenameTemplateAudio,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                extraContent = {
                    Column(
                        modifier = Modifier.fillMaxHeight(0.5f),
                    ) {
                        Text(
                            modifier = Modifier.padding(top = 16.dp),
                            text = stringResource(R.string.filename_template_hint),
                            style = MaterialTheme.typography.titleSmall
                        )
                        FilenameTemplateSuggestion(
                            templates = state.audioTemplates,
                            onTemplateClick = { template ->
                                eventHandler(
                                    FilesSettingsEvent.OnAudioTemplateChange(
                                        template
                                    )
                                )
                            }
                        )
                    }
                }
            )
            InputSettingItem(
                value = state.filenameTemplateVideo,
                onValueChange = { value ->
                    eventHandler(FilesSettingsEvent.OnVideoFilenameTemplateChange(value))
                },
                title = stringResource(R.string.title_video_filename_format),
                description = stringResource(
                    R.string.desc_video_filename_format,
                    state.filenameTemplateVideo
                ),
                label = stringResource(R.string.video_template_title),
                placeholder = stringResource(R.string.filename_template_placeholder),
                icon = ImageVector.vectorResource(R.drawable.round_videocam_24),
                isError = !state.isValidFilenameTemplateVideo,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                extraContent = {
                    Column(
                        modifier = Modifier.fillMaxHeight(0.5f),
                    ) {
                        Text(
                            modifier = Modifier.padding(top = 16.dp),
                            text = stringResource(R.string.filename_template_hint),
                            style = MaterialTheme.typography.titleSmall
                        )
                        FilenameTemplateSuggestion(
                            templates = state.videoTemplates,
                            onTemplateClick = { template ->
                                eventHandler(
                                    FilesSettingsEvent.OnVideoTemplateChange(
                                        template
                                    )
                                )
                            }
                        )
                    }
                }
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

@Composable
private fun DirectorySettingItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector?,
    enabled: Boolean = true,
    onDirectoryChange: (Uri?) -> Unit,
) {
    val context = LocalContext.current

    // 1. The modern way to handle activity results in Compose.
    //    We remember a launcher for the "Open Document Tree" contract.
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            // 4. When the user picks a folder, the result (a Uri) comes back here.

            // CRUCIAL: To maintain access to this folder across device reboots,
            // you must take persistable URI permissions.
            if (uri != null) {
                val contentResolver = context.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            }

            // 5. We then pass the Uri up to the caller.
            onDirectoryChange(uri)
        }
    )

    // 2. We use your base SettingItem.
    SettingItem(
        modifier = modifier,
        title = title,
        description = description,
        icon = icon,
        enabled = enabled,
        onClick = {
            // 3. When the user clicks, we launch the system's folder picker.
            directoryPickerLauncher.launch(null)
        }
    )
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
            audioTemplates = FileTemplateUtils.getAvailableTemplates().map { Template(it.key) },
            videoTemplates = FileTemplateUtils.getAvailableTemplates().map { Template(it.key) },
            downloadArchive = false,
            restrictFilenames = false
        ),
        onBackClick = {},
        eventHandler = {}
    )
}