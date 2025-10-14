package org.ghost.zeku.ui.screen.settings.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.R
import org.ghost.zeku.ui.component.SettingTitle
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.BackButton
import org.ghost.zeku.ui.screen.settings.PostProcessingSettingsState

sealed interface PostProcessingSettingsEvent {
    data class OnThumbnailChange(val thumbnail: Boolean) : PostProcessingSettingsEvent
    data class OnEmbedThumbnailChange(val embedThumbnail: Boolean) : PostProcessingSettingsEvent
    data class OnEmbedMetadataChange(val embedMetadata: Boolean) : PostProcessingSettingsEvent
    data class OnCropArtworkChange(val cropArtwork: Boolean) : PostProcessingSettingsEvent
    data class OnMergeOutputMkvChange(val mergeOutputMkv: Boolean) : PostProcessingSettingsEvent
    data class OnMergeMultiAudioStreamChange(val mergeMultiAudioStream: Boolean) :
        PostProcessingSettingsEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostProcessingSettings(
    modifier: Modifier = Modifier,
    state: PostProcessingSettingsState,
    eventHandler: (PostProcessingSettingsEvent) -> Unit,
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
            SettingTitle(stringResource(R.string.settings_post_processing_title))

            SwitchSettingItem(
                title = stringResource(R.string.title_download_thumbnail_image),
                description = stringResource(R.string.desc_download_thumbnail_image),
                icon = Icons.Filled.Settings, // Replace with R.drawable.rounded_image_24 or similar
                checked = state.thumbnail,
                onSelectionChange = { checked ->
                    eventHandler(
                        PostProcessingSettingsEvent.OnThumbnailChange(
                            checked
                        )
                    )
                }
            )
            SwitchSettingItem(
                title = stringResource(R.string.title_embed_thumbnail),
                description = stringResource(R.string.desc_embed_thumbnail),
                icon = Icons.Filled.Settings, // Replace with a relevant icon
                checked = state.embedThumbnail,
                onSelectionChange = { checked ->
                    eventHandler(
                        PostProcessingSettingsEvent.OnEmbedThumbnailChange(
                            checked
                        )
                    )
                }
            )
            SwitchSettingItem(
                title = stringResource(R.string.title_embed_metadata),
                description = stringResource(R.string.desc_embed_metadata),
                icon = Icons.Filled.Settings, // Replace with a relevant icon
                checked = state.embedMetadata,
                onSelectionChange = { checked ->
                    eventHandler(
                        PostProcessingSettingsEvent.OnEmbedMetadataChange(
                            checked
                        )
                    )
                }
            )
            SwitchSettingItem(
                title = stringResource(R.string.title_crop_artwork_to_square),
                description = stringResource(R.string.desc_crop_artwork_to_square),
                icon = Icons.Filled.Settings, // Replace with a relevant icon (e.g., crop icon)
                checked = state.cropArtwork,
                onSelectionChange = { checked ->
                    eventHandler(
                        PostProcessingSettingsEvent.OnCropArtworkChange(
                            checked
                        )
                    )
                }
            )
            SwitchSettingItem(
                title = stringResource(R.string.title_merge_to_mkv_container),
                description = stringResource(R.string.desc_merge_to_mkv_container),
                icon = Icons.Filled.Settings, // Replace with a relevant icon (e.g., movie or file icon)
                checked = state.mergeOutputMkv,
                onSelectionChange = { checked ->
                    eventHandler(
                        PostProcessingSettingsEvent.OnMergeOutputMkvChange(
                            checked
                        )
                    )
                }
            )
            SwitchSettingItem(
                title = stringResource(R.string.title_merge_multiple_audio_streams),
                description = stringResource(R.string.desc_merge_multiple_audio_streams),
                icon = Icons.Filled.Settings, // Replace with a relevant icon (e.g., multi-track icon)
                checked = state.mergeMultiAudioStream,
                onSelectionChange = { checked ->
                    eventHandler(
                        PostProcessingSettingsEvent.OnMergeMultiAudioStreamChange(
                            checked
                        )
                    )
                }
            )
        }
    }

}

@Preview
@Composable
private fun PostProcessingSettingsPreview() {
    PostProcessingSettings(
        state = PostProcessingSettingsState(
            thumbnail = true,
            embedThumbnail = false,
            embedMetadata = true,
            cropArtwork = false,
            mergeOutputMkv = true,
            mergeMultiAudioStream = false
        ),
        eventHandler = {},
        onBackClick = {}
    )
}