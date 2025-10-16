package org.ghost.zeku.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.ghost.zeku.ui.screen.settings.SettingsScreen
import org.ghost.zeku.ui.screen.settings.SettingsViewModel
import org.ghost.zeku.ui.screen.settings.page.AboutPage
import org.ghost.zeku.ui.screen.settings.page.AdvanceSettings
import org.ghost.zeku.ui.screen.settings.page.AppearanceSettings
import org.ghost.zeku.ui.screen.settings.page.AppearanceSettingsEvent
import org.ghost.zeku.ui.screen.settings.page.FileSettings
import org.ghost.zeku.ui.screen.settings.page.FilesSettingsEvent
import org.ghost.zeku.ui.screen.settings.page.GeneralSettings
import org.ghost.zeku.ui.screen.settings.page.GeneralSettingsEvent
import org.ghost.zeku.ui.screen.settings.page.MediaSettings
import org.ghost.zeku.ui.screen.settings.page.MediaSettingsEvent
import org.ghost.zeku.ui.screen.settings.page.NetworkSettings
import org.ghost.zeku.ui.screen.settings.page.NetworkSettingsEvent
import org.ghost.zeku.ui.screen.settings.page.PostProcessingSettings
import org.ghost.zeku.ui.screen.settings.page.PostProcessingSettingsEvent
import org.ghost.zeku.ui.screen.settings.page.SubtitlesSettings
import org.ghost.zeku.ui.screen.settings.page.SubtitlesSettingsEvent
import timber.log.Timber

@Composable
fun AppNavigationGraph(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    startDestination: NavRoute,
    settingsViewModel: SettingsViewModel
) {

    val popBackStack: () -> Unit = {
        Timber.d("Popping back stack")
        navHostController.popBackStack()
    }

    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination
    ) {
        settingGraph(
            navHostController,
            settingsViewModel,
            popBackStack,
        )
    }
}


fun NavGraphBuilder.settingGraph(
    navHostController: NavHostController,
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    composable<NavRoute.Settings> {
        SettingsScreen(
            modifier = Modifier,
            onBackClick = onBackClick,
            onSettingClick = { route ->
                navHostController.navigate(route)
            }
        )
    }
    composable<SettingsRoute.GeneralSettings> {
        val settingsUiState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()
        GeneralSettings(
            state = settingsUiState.general,
            eventHandler = { event ->
                Timber.d("Event: $event")
                when (event) {
                    is GeneralSettingsEvent.UpdateDebug -> settingsViewModel.setDebug(event.isEnabled)
                    is GeneralSettingsEvent.ResetToDefaults -> settingsViewModel.resetSettingsToDefault()
                    is GeneralSettingsEvent.UpdateAutoUpdate -> settingsViewModel.setAutoUpdate(
                        event.isEnabled
                    )

                    is GeneralSettingsEvent.UpdateChannel -> settingsViewModel.setUpdateChannel(
                        event.channel
                    )

                    is GeneralSettingsEvent.UpdateConfigure -> settingsViewModel.setConfigure(event.isEnabled)
                    is GeneralSettingsEvent.UpdateNotification -> settingsViewModel.setNotification(
                        event.isEnabled
                    )

                    is GeneralSettingsEvent.UpdatePreferredDownloadType -> settingsViewModel.setPreferredDownloadType(
                        event.type
                    )

                    is GeneralSettingsEvent.UpdatePreventDuplicates -> settingsViewModel.setPreventDuplicateDownloads(
                        event.type
                    )

                    is GeneralSettingsEvent.UpdatePrivateMode -> settingsViewModel.setPrivateMode(
                        event.isEnabled
                    )
                }
            },
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.FileAndDirectorySettings> {
        val settingsUiState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()
        FileSettings(
            state = settingsUiState.file,
            eventHandler = { event ->
                when (event) {
                    is FilesSettingsEvent.OnRestrictFilenames -> settingsViewModel.setRestrictFilenames(
                        event.value
                    )

                    is FilesSettingsEvent.OnDownloadArchive -> settingsViewModel.setDownloadArchive(
                        event.value
                    )

                    is FilesSettingsEvent.OnSubdirectoryPlaylistTitle -> settingsViewModel.setSubdirectoryPlaylistTitle(
                        event.value
                    )

                    is FilesSettingsEvent.OnAudioFilenameTemplateChange -> settingsViewModel.setFilenameTemplateAudio(
                        event.value
                    )

                    is FilesSettingsEvent.OnVideoFilenameTemplateChange -> settingsViewModel.setFilenameTemplateVideo(
                        event.value
                    )

                    is FilesSettingsEvent.OnCommandDirectoryChange -> settingsViewModel.setCommandDirectory(
                        event.value
                    )

                    is FilesSettingsEvent.OnAudioDirectoryChange -> settingsViewModel.setAudioDirectory(
                        event.value
                    )

                    is FilesSettingsEvent.OnVideoDirectoryChange -> settingsViewModel.setVideoDirectory(
                        event.value
                    )
                }
            },
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.InterfaceSettings> {
        val settingsUiState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()
        AppearanceSettings(
            state = settingsUiState.appearance,
            eventHandler = { event ->
                Timber.d("Appearance event: $event")
                when (event) {
                    is AppearanceSettingsEvent.OnThemeChange -> settingsViewModel.setThemeColor(
                        event.theme
                    )

                    is AppearanceSettingsEvent.OnAmoledChange -> settingsViewModel.setAmoled(event.amoled)
                    is AppearanceSettingsEvent.OnThemeModeChange -> settingsViewModel.setThemeMode(
                        event.themeMode
                    )

                    is AppearanceSettingsEvent.OnDynamicColorChange -> settingsViewModel.setDynamicColor(
                        event.dynamicColor
                    )

                    is AppearanceSettingsEvent.OnHighContrastChange -> settingsViewModel.setContrastValue(
                        event.highContrast
                    )

                    is AppearanceSettingsEvent.OnAccentColorChange -> settingsViewModel.setAccentColor(
                        event.accentColor
                    )
                }
            },
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.MediaSettings> {
        val settingsUiState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()
        MediaSettings(
            state = settingsUiState.media,
            eventHandler = { event ->
                when (event) {
                    is MediaSettingsEvent.OnExtractAudioChange -> settingsViewModel.setExtractAudio(
                        event.extractAudio
                    )

                    is MediaSettingsEvent.OnAudioConversionFormatChange -> settingsViewModel.setAudioConversionFormat(
                        event.audioConversionFormat
                    )

                    is MediaSettingsEvent.OnAudioConvertChange -> settingsViewModel.setAudioConvert(
                        event.audioConvert
                    )

                    is MediaSettingsEvent.OnAudioEncodingChange -> settingsViewModel.setAudioEncoding(
                        event.audioEncoding
                    )

                    is MediaSettingsEvent.OnAudioFormatChange -> settingsViewModel.setAudioFormat(
                        event.audioFormat
                    )

                    is MediaSettingsEvent.OnAudioQualityChange -> settingsViewModel.setAudioQuality(
                        event.audioQuality
                    )

                    is MediaSettingsEvent.OnAv1HardwareAcceleratedChange -> settingsViewModel.setAv1HardwareAccelerated(
                        event.av1HardwareAccelerated
                    )

                    is MediaSettingsEvent.OnPlaylistChange -> settingsViewModel.setPlaylist(event.playlist)
                    is MediaSettingsEvent.OnUseCustomAudioPresetChange -> settingsViewModel.setUseCustomAudioPreset(
                        event.useCustomAudioPreset
                    )

                    is MediaSettingsEvent.OnVideoClipChange -> settingsViewModel.setVideoClip(event.videoClip)
                    is MediaSettingsEvent.OnVideoEncodingChange -> settingsViewModel.setVideoEncoding(
                        event.videoEncoding
                    )

                    is MediaSettingsEvent.OnVideoFormatChange -> settingsViewModel.setVideoFormat(
                        event.videoFormat
                    )

                    is MediaSettingsEvent.OnVideoQualityChange -> settingsViewModel.setVideoQuality(
                        event.videoQuality
                    )
                }
            },
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.NetworkSettings> {
        val settingsUiState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()
        NetworkSettings(
            state = settingsUiState.network,
            eventHandler = { event ->
                when (event) {
                    is NetworkSettingsEvent.OnProxyChange -> settingsViewModel.setProxy(event.proxy)
                    is NetworkSettingsEvent.OnCellularDownloadChange -> settingsViewModel.setCellularDownload(
                        event.cellularDownload
                    )

                    is NetworkSettingsEvent.OnConcurrentChange -> settingsViewModel.setConcurrent(
                        event.concurrent
                    )

                    is NetworkSettingsEvent.OnCookiesChange -> settingsViewModel.setCookies(event.cookies)
                    is NetworkSettingsEvent.OnForceIpv4Change -> settingsViewModel.setForceIpv4(
                        event.forceIpv4
                    )

                    is NetworkSettingsEvent.OnFragmentRetriesChange -> settingsViewModel.setFragmentRetries(
                        event.fragmentRetries
                    )

                    is NetworkSettingsEvent.OnMaxRateChange -> settingsViewModel.setMaxRate(event.maxRate)
                    is NetworkSettingsEvent.OnProxyUrlChange -> settingsViewModel.setProxyUrl(event.proxyUrl)
                    is NetworkSettingsEvent.OnRateLimitChange -> settingsViewModel.setRateLimit(
                        event.rateLimit
                    )

                    is NetworkSettingsEvent.OnRetriesChange -> settingsViewModel.setRetries(event.retries)
                    is NetworkSettingsEvent.OnUserAgentChange -> settingsViewModel.setUserAgent(
                        event.userAgent
                    )

                    is NetworkSettingsEvent.OnUserAgentStringChange -> settingsViewModel.setUserAgentString(
                        event.userAgentString
                    )

                    is NetworkSettingsEvent.OnAria2cChange -> settingsViewModel.setAria2c(event.aria2c)
                }
            },
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.PostProcessingSettings> {
        val settingsUiState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()
        PostProcessingSettings(
            state = settingsUiState.postProcessing,
            eventHandler = { event ->
                when (event) {
                    is PostProcessingSettingsEvent.OnCropArtworkChange -> settingsViewModel.setCropArtwork(
                        event.cropArtwork
                    )

                    is PostProcessingSettingsEvent.OnEmbedMetadataChange -> settingsViewModel.setEmbedMetadata(
                        event.embedMetadata
                    )

                    is PostProcessingSettingsEvent.OnEmbedThumbnailChange -> settingsViewModel.setEmbedThumbnail(
                        event.embedThumbnail
                    )

                    is PostProcessingSettingsEvent.OnMergeMultiAudioStreamChange -> settingsViewModel.setMergeMultiAudioStream(
                        event.mergeMultiAudioStream
                    )

                    is PostProcessingSettingsEvent.OnMergeOutputMkvChange -> settingsViewModel.setMergeOutputMkv(
                        event.mergeOutputMkv
                    )

                    is PostProcessingSettingsEvent.OnThumbnailChange -> settingsViewModel.setThumbnail(
                        event.thumbnail
                    )
                }
            },
            onBackClick = onBackClick,
        )
    }
    composable<SettingsRoute.SubtitlesSettings> {
        val settingsUiState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()
        SubtitlesSettings(
            state = settingsUiState.subtitle,
            eventHandler = { event ->
                when (event) {
                    is SubtitlesSettingsEvent.OnAutoSubtitleEvent -> settingsViewModel.setAutoSubtitle(
                        event.autoSubtitle
                    )

                    is SubtitlesSettingsEvent.OnAutoTranslatedSubtitlesEvent -> settingsViewModel.setAutoTranslatedSubtitles(
                        event.autoTranslatedSubtitles
                    )

                    is SubtitlesSettingsEvent.OnConvertSubtitleEvent -> settingsViewModel.setConvertSubtitle(
                        event.convertSubtitle
                    )

                    is SubtitlesSettingsEvent.OnEmbedSubtitleEvent -> settingsViewModel.setEmbedSubtitle(
                        event.embedSubtitle
                    )

                    is SubtitlesSettingsEvent.OnKeepSubtitleFilesEvent -> settingsViewModel.setKeepSubtitleFiles(
                        event.keepSubtitleFiles
                    )

                    is SubtitlesSettingsEvent.OnSubtitleLanguageEvent -> settingsViewModel.setSubtitleLanguage(
                        event.subtitleLanguage
                    )

                    is SubtitlesSettingsEvent.OnSubtitleSettingsEvent -> settingsViewModel.setSubtitle(
                        event.subtitles
                    )
                }
            },
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.AdvancedSettings> {
        val settingsUiState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()
        AdvanceSettings(
            state = settingsUiState.advanced,
            event = {},
            onBackClick = onBackClick,
            onCustomCommandClick = {
                // TODO: Custom command screen
            }
        )
    }

    composable<SettingsRoute.About> {
        AboutPage(
            onBackClick = onBackClick,
        )
    }
}


