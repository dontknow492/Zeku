package org.ghost.zeku.ui.screen.settings.page

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.R
import org.ghost.zeku.core.enum.SubtitlesFormat
import org.ghost.zeku.ui.common.SettingScaffold
import org.ghost.zeku.ui.component.InputSettingItem
import org.ghost.zeku.ui.component.RadioSettingEnumItem
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.SubtitleSettingsState


sealed interface SubtitlesSettingsEvent {
    data class OnSubtitleSettingsEvent(val subtitles: Boolean) : SubtitlesSettingsEvent
    data class OnEmbedSubtitleEvent(val embedSubtitle: Boolean) : SubtitlesSettingsEvent
    data class OnKeepSubtitleFilesEvent(val keepSubtitleFiles: Boolean) : SubtitlesSettingsEvent
    data class OnSubtitleLanguageEvent(val subtitleLanguage: String) : SubtitlesSettingsEvent
    data class OnAutoSubtitleEvent(val autoSubtitle: Boolean) : SubtitlesSettingsEvent
    data class OnConvertSubtitleEvent(val convertSubtitle: SubtitlesFormat) : SubtitlesSettingsEvent
    data class OnAutoTranslatedSubtitlesEvent(val autoTranslatedSubtitles: Boolean) :
        SubtitlesSettingsEvent
}

@Composable
fun SubtitlesSettings(
    modifier: Modifier = Modifier,
    state: SubtitleSettingsState,
    eventHandler: (SubtitlesSettingsEvent) -> Unit,
    onBackClick: () -> Unit
) {
    SettingScaffold(
        modifier = modifier,
        title = stringResource(R.string.settings_subtitle_title),
        error = state.error,
        onBackClick = onBackClick
    ) {
        SwitchSettingItem(
            title = stringResource(R.string.enable_subtitles_title),
            description = stringResource(R.string.enable_subtitles_description),
            checked = state.subtitle,
            icon = ImageVector.vectorResource(R.drawable.baseline_subtitles_24),
            onSelectionChange = { checked ->
                eventHandler(SubtitlesSettingsEvent.OnSubtitleSettingsEvent(checked))
            }
        )

        InputSettingItem(
            value = state.subtitleLanguage,
            onValueChange = {
                eventHandler(SubtitlesSettingsEvent.OnSubtitleLanguageEvent(it))
            },
            title = stringResource(R.string.subtitle_language_title),
            description = "${stringResource(R.string.subtitle_language_description)} [${state.subtitleLanguage}]",
            label = stringResource(R.string.subtitle_language_placeholder),
            placeholder = stringResource(R.string.subtitle_language_placeholder),
            icon = ImageVector.vectorResource(R.drawable.outline_language_24),
            enabled = state.subtitle,
            isError = false
        )

        SwitchSettingItem(
            title = stringResource(R.string.embed_subtitles_title),
            description = stringResource(R.string.embed_subtitles_description),
            checked = state.embedSubtitle,
            enabled = state.subtitle,
            icon = Icons.Filled.Edit,
            onSelectionChange = { checked ->
                eventHandler(SubtitlesSettingsEvent.OnEmbedSubtitleEvent(checked))
            }
        )

        SwitchSettingItem(
            title = stringResource(R.string.keep_subtitle_files_title),
            description = stringResource(R.string.keep_subtitle_files_description),
            checked = state.keepSubtitleFiles,
            icon = ImageVector.vectorResource(R.drawable.baseline_save_24),
            onSelectionChange = { checked ->
                eventHandler(SubtitlesSettingsEvent.OnKeepSubtitleFilesEvent(checked))
            },
            enabled = state.subtitle,
        )

        SwitchSettingItem(
            title = stringResource(R.string.auto_subtitle_title),
            description = stringResource(R.string.auto_subtitle_description),
            checked = state.autoSubtitle,
            icon = ImageVector.vectorResource(R.drawable.rounded_download_24),
            onSelectionChange = { checked ->
                eventHandler(SubtitlesSettingsEvent.OnAutoSubtitleEvent(checked))
            },
            enabled = state.subtitle,
        )

        RadioSettingEnumItem(
            title = stringResource(R.string.convert_subtitle_format_title),
            description = "${stringResource(R.string.convert_subtitle_format_description)} [${SubtitlesFormat.DEFAULT.name}]",
            icon = ImageVector.vectorResource(R.drawable.rounded_output_24),
            selectedValue = SubtitlesFormat.DEFAULT,
            items = SubtitlesFormat.entries,
            onValueChange = {
                eventHandler(SubtitlesSettingsEvent.OnConvertSubtitleEvent(it))
            },
            enabled = state.subtitle,
        )

        SwitchSettingItem(
            title = stringResource(R.string.auto_translated_subtitles_title),
            description = stringResource(R.string.auto_translated_subtitles_description),
            checked = state.autoTranslatedSubtitles,
            icon = ImageVector.vectorResource(R.drawable.rounded_g_translate_24),
            onSelectionChange = { checked ->
                eventHandler(SubtitlesSettingsEvent.OnAutoTranslatedSubtitlesEvent(checked))
            },
            enabled = state.subtitle,
        )

    }

}

@Preview
@Composable
fun SubtitlesSettingsPreview() {
    val state = SubtitleSettingsState(
        subtitle = true,
        embedSubtitle = false,
        keepSubtitleFiles = true,
        subtitleLanguage = "en",
        autoSubtitle = true,
        convertSubtitle = SubtitlesFormat.LRC,
        autoTranslatedSubtitles = false
    )
    SubtitlesSettings(state = state, eventHandler = {}, onBackClick = {})
}
