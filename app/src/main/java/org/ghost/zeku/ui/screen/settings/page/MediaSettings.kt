package org.ghost.zeku.ui.screen.settings.page

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.R
import org.ghost.zeku.core.enum.AudioEncoding
import org.ghost.zeku.core.enum.AudioFormat
import org.ghost.zeku.core.enum.AudioQuality
import org.ghost.zeku.core.enum.VideoEncoding
import org.ghost.zeku.core.enum.VideoFormat
import org.ghost.zeku.core.enum.VideoQuality
import org.ghost.zeku.ui.common.SettingScaffold
import org.ghost.zeku.ui.component.GroupSettingItem
import org.ghost.zeku.ui.component.RadioSettingEnumItem
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.MediaSettingsState

sealed interface MediaSettingsEvent {
    data class OnVideoFormatChange(val videoFormat: VideoFormat) : MediaSettingsEvent
    data class OnVideoEncodingChange(val videoEncoding: VideoEncoding) : MediaSettingsEvent
    data class OnVideoQualityChange(val videoQuality: VideoQuality) : MediaSettingsEvent
    data class OnAv1HardwareAcceleratedChange(val av1HardwareAccelerated: Boolean) :
        MediaSettingsEvent

    data class OnVideoClipChange(val videoClip: Boolean) : MediaSettingsEvent
    data class OnPlaylistChange(val playlist: Boolean) : MediaSettingsEvent
    data class OnExtractAudioChange(val extractAudio: Boolean) : MediaSettingsEvent
    data class OnAudioConvertChange(val audioConvert: Boolean) : MediaSettingsEvent
    data class OnAudioEncodingChange(val audioEncoding: AudioEncoding) : MediaSettingsEvent
    data class OnAudioConversionFormatChange(val audioConversionFormat: AudioFormat) :
        MediaSettingsEvent

    data class OnAudioFormatChange(val audioFormat: AudioFormat) : MediaSettingsEvent
    data class OnAudioQualityChange(val audioQuality: AudioQuality) : MediaSettingsEvent
    data class OnUseCustomAudioPresetChange(val useCustomAudioPreset: Boolean) : MediaSettingsEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaSettings(
    modifier: Modifier = Modifier,
    state: MediaSettingsState,
    eventHandler: (MediaSettingsEvent) -> Unit,
    onBackClick: () -> Unit
) {
    SettingScaffold(
        modifier = modifier,
        title = stringResource(R.string.settings_media_title),
        error = state.error,
        onBackClick = onBackClick
    ) {
        GroupSettingItem(
            title = stringResource(R.string.group_title_audio_settings)
        ) {
            RadioSettingEnumItem(
                selectedValue = state.audioFormat,
                items = AudioFormat.entries,
                title = stringResource(R.string.title_target_audio_format),
                description = stringResource(
                    R.string.desc_target_audio_format,
                    state.audioFormat.label
                ),
                icon = ImageVector.vectorResource(R.drawable.rounded_music_note_24),
                onValueChange = { format ->
                    eventHandler(
                        MediaSettingsEvent.OnAudioFormatChange(
                            format
                        )
                    )
                }
            )

            RadioSettingEnumItem(
                selectedValue = state.audioEncoding,
                items = AudioEncoding.entries,
                title = stringResource(R.string.title_audio_codec),
                description = stringResource(
                    R.string.desc_audio_codec,
                    state.audioEncoding.label
                ),
                icon = ImageVector.vectorResource(R.drawable.rounded_music_history_24),
                onValueChange = { encoding ->
                    eventHandler(
                        MediaSettingsEvent.OnAudioEncodingChange(
                            encoding
                        )
                    )
                }
            )
            RadioSettingEnumItem(
                selectedValue = state.audioQuality,
                items = AudioQuality.entries,
                title = stringResource(R.string.title_audio_quality),
                description = stringResource(
                    R.string.desc_audio_quality,
                    state.audioQuality.label
                ),
                icon = ImageVector.vectorResource(R.drawable.baseline_speaker_24),
                onValueChange = { quality ->
                    eventHandler(
                        MediaSettingsEvent.OnAudioQualityChange(
                            quality
                        )
                    )
                }
            )
            SwitchSettingItem(
                title = stringResource(R.string.title_convert_audio_format),
                description = stringResource(R.string.desc_convert_audio_format),
                icon = ImageVector.vectorResource(R.drawable.baseline_diamond_24),
                checked = state.audioConvert,
                onSelectionChange = { convert ->
                    eventHandler(
                        MediaSettingsEvent.OnAudioConvertChange(
                            convert
                        )
                    )
                }
            )
            RadioSettingEnumItem(
                selectedValue = state.audioConversionFormat,
                items = AudioFormat.entries,
                title = stringResource(R.string.title_conversion_target_format),
                description = stringResource(
                    R.string.desc_conversion_target_format,
                    state.audioConversionFormat.label
                ),
                icon = Icons.Filled.Settings,
                enabled = state.audioConvert,
                onValueChange = { format ->
                    eventHandler(
                        MediaSettingsEvent.OnAudioConversionFormatChange(
                            format
                        )
                    )
                }
            )
        }

        GroupSettingItem(
            title = stringResource(R.string.group_title_video_settings)
        ) {
            RadioSettingEnumItem(
                selectedValue = state.videoFormat,
                items = VideoFormat.entries,
                title = stringResource(R.string.title_target_video_format),
                description = stringResource(
                    R.string.desc_target_video_format,
                    state.videoFormat.label
                ),
                icon = ImageVector.vectorResource(R.drawable.round_videocam_24),
                onValueChange = { format ->
                    eventHandler(
                        MediaSettingsEvent.OnVideoFormatChange(
                            format
                        )
                    )
                }
            )
            RadioSettingEnumItem(
                selectedValue = state.videoQuality,
                items = VideoQuality.entries,
                title = stringResource(R.string.title_video_resolution_quality),
                description = stringResource(
                    R.string.desc_video_resolution_quality,
                    state.videoQuality.label
                ),
                icon = ImageVector.vectorResource(R.drawable.rounded_high_quality_24),
                onValueChange = { quality ->
                    eventHandler(
                        MediaSettingsEvent.OnVideoQualityChange(
                            quality
                        )
                    )
                }
            )
            RadioSettingEnumItem(
                selectedValue = state.videoEncoding,
                items = VideoEncoding.entries,
                title = stringResource(R.string.title_video_codec),
                description = stringResource(
                    R.string.desc_video_codec,
                    state.videoEncoding.label
                ),
                icon = ImageVector.vectorResource(R.drawable.round_video_settings_24),
                onValueChange = { encoding ->
                    eventHandler(
                        MediaSettingsEvent.OnVideoEncodingChange(
                            encoding
                        )
                    )
                }
            )
            SwitchSettingItem(
                title = stringResource(R.string.title_enable_video_clipping),
                description = stringResource(R.string.desc_enable_video_clipping),
                icon = ImageVector.vectorResource(R.drawable.rounded_content_cut_24),
                checked = state.videoClip,
                onSelectionChange = { convert ->
                    eventHandler(
                        MediaSettingsEvent.OnVideoClipChange(
                            convert
                        )
                    )
                }
            )
        }

        GroupSettingItem(
            title = stringResource(R.string.group_title_additional_options)
        ) {
            SwitchSettingItem(
                title = stringResource(R.string.title_always_extract_audio),
                description = stringResource(R.string.desc_always_extract_audio),
                icon = ImageVector.vectorResource(R.drawable.rounded_brightness_6_24),
                checked = state.extractAudio,
                onSelectionChange = { convert ->
                    eventHandler(
                        MediaSettingsEvent.OnExtractAudioChange(
                            convert
                        )
                    )
                }
            )
            SwitchSettingItem(
                title = stringResource(R.string.title_enable_av1_hardware_acceleration),
                description = stringResource(R.string.desc_enable_av1_hardware_acceleration),
                icon = ImageVector.vectorResource(R.drawable.rounded_speed_24),
                checked = state.extractAudio, // Assuming this should be a different state type related to AV1
                onSelectionChange = { convert ->
                    eventHandler(
                        MediaSettingsEvent.OnAv1HardwareAcceleratedChange(
                            convert
                        )
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun MediaSettingsPreview() {
    MediaSettings(
        state = MediaSettingsState(
            videoFormat = VideoFormat.DEFAULT,
            videoEncoding = VideoEncoding.DEFAULT,
            videoQuality = VideoQuality.DEFAULT,
            av1HardwareAccelerated = false,
            videoClip = false,
            playlist = false,
            extractAudio = false,
            audioConvert = false,
            audioConversionFormat = AudioFormat.DEFAULT,
            audioFormat = AudioFormat.DEFAULT,
            audioQuality = AudioQuality.DEFAULT,
            useCustomAudioPreset = false,
            audioEncoding = AudioEncoding.M4A
        ),
        eventHandler = {},
        onBackClick = {}
    )
}