package org.ghost.zeku.ui.screen.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ghost.zeku.core.DownloadType
import org.ghost.zeku.core.enum.AudioFormat
import org.ghost.zeku.core.enum.AudioQuality
import org.ghost.zeku.core.enum.PreventDuplicateDownload
import org.ghost.zeku.core.enum.SubtitlesFormat
import org.ghost.zeku.core.enum.ThemeMode
import org.ghost.zeku.core.enum.VideoEncoding
import org.ghost.zeku.core.enum.VideoFormat
import org.ghost.zeku.core.enum.VideoQuality
import org.ghost.zeku.repository.PreferenceRepository
import org.ghost.zeku.ui.theme.AppTheme
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: PreferenceRepository
) : ViewModel() {


    private val generalSettingsStateFlow: Flow<GeneralSettingsState> = combine(
        // Group 1 (5 flows)
        combine(
            repository.configureFlow, repository.debugFlow, repository.welcomeDialogFlow,
            repository.notificationFlow, repository.autoUpdateFlow
        ) { configure, debug, welcomeDialog, notification, autoUpdate ->
            // Anonymous object to hold results of the first group
            object {
                val configure = configure
                val debug = debug
                val welcomeDialog = welcomeDialog
                val notification = notification
                val autoUpdate = autoUpdate
            }
        },
        // Group 2 (4 flows)
        combine(
            repository.updateChannelFlow, repository.privateModeFlow,
            repository.preventDuplicateDownloadsFlow, repository.preferredDownloadTypeFlow
        ) { updateChannel, privateMode, preventDuplicates, preferredType ->
            // Anonymous object to hold results of the second group
            object {
                val updateChannel = updateChannel
                val privateMode = privateMode
                val preventDuplicates = preventDuplicates
                val preferredType = preferredType
            }
        }
    ) { group1, group2 ->
        // Combine the results from both groups into the final state class
        GeneralSettingsState(
            configure = group1.configure,
            debug = group1.debug,
            welcomeDialog = group1.welcomeDialog,
            notification = group1.notification,
            autoUpdate = group1.autoUpdate,
            updateChannel = group2.updateChannel,
            privateMode = group2.privateMode,
            preventDuplicateDownloads = group2.preventDuplicates,
            preferredDownloadType = group2.preferredType
        )
    }

    private val fileSettingsStateFlow: Flow<FileSettingsState> = combine(
        // Group 1 (5 flows)
        combine(
            repository.videoDirectoryFlow,
            repository.audioDirectoryFlow,
            repository.commandDirectoryFlow,
            repository.subdirectoryPlaylistTitleFlow,
            repository.filenameTemplateVideoFlow
        ) { videoDir, audioDir, cmdDir, subdir, videoTemplate ->
            object {
                val videoDir = videoDir
                val audioDir = audioDir
                val cmdDir = cmdDir
                val subdir = subdir
                val videoTemplate = videoTemplate
            }
        },
        // Group 2 (3 flows)
        combine(
            repository.filenameTemplateAudioFlow,
            repository.downloadArchiveFlow,
            repository.restrictFilenamesFlow
        ) { audioTemplate, archive, restrict ->
            object {
                val audioTemplate = audioTemplate
                val archive = archive
                val restrict = restrict
            }
        }
    ) { group1, group2 ->
        FileSettingsState(
            videoDirectory = group1.videoDir,
            audioDirectory = group1.audioDir,
            commandDirectory = group1.cmdDir,
            subdirectoryPlaylistTitle = group1.subdir,
            filenameTemplateVideo = group1.videoTemplate,
            filenameTemplateAudio = group2.audioTemplate,
            downloadArchive = group2.archive,
            restrictFilenames = group2.restrict
        )
    }

    private val appearanceSettingsStateFlow: Flow<AppearanceSettingsState> = combine(
        combine(
            repository.themeModeFlow, repository.amoledFlow, repository.accentColorFlow,
            repository.themeColorFlow, repository.dynamicColorFlow
        ) { themeModel, amoled, accentColor, themeColor, dynamicColor ->
            object {
                val themeModel = themeModel
                val amoled = amoled
                val accentColor = accentColor
                val themeColor = themeColor
                val dynamicColor = dynamicColor
            }
        },
        repository.highContrastFlow
    ) { group1, highContrast ->
        AppearanceSettingsState(
            themeMode = group1.themeModel,
            amoled = group1.amoled,
            accentColor = group1.accentColor,
            theme = group1.themeColor,
            dynamicColor = group1.dynamicColor,
            highContrast = highContrast
        )
    }

    private val mediaSettingsStateFlow: Flow<MediaSettingsState> = combine(
        combine(
            repository.videoFormatFlow, repository.videoEncodingFlow, repository.videoQualityFlow,
            repository.av1HardwareAcceleratedFlow, repository.videoClipFlow
        ) { v1, v2, v3, v4, v5 ->
            object {
                val videoFormat = v1
                val videoEncoding = v2
                val videoQuality = v3
                val av1 = v4
                val videoClip = v5
            }
        },
        combine(
            repository.playlistFlow, repository.extractAudioFlow, repository.audioConvertFlow,
            repository.audioConversionFormatFlow, repository.audioFormatFlow
        ) { v6, v7, v8, v9, v10 ->
            object {
                val playlist = v6
                val extractAudio = v7
                val audioConvert = v8
                val audioConvFormat = v9
                val audioFormat = v10
            }
        },
        combine(
            repository.audioQualityFlow,
            repository.useCustomAudioPresetFlow,
            repository.audioEncodingFlow
        ) { v11, v12, v13 ->
            object {
                val audioQuality = v11
                val customPreset = v12
                val audioEncoding = v13
            }
        }
    ) { g1, g2, g3 ->
        MediaSettingsState(
            videoFormat = g1.videoFormat,
            videoEncoding = g1.videoEncoding,
            videoQuality = g1.videoQuality,
            av1HardwareAccelerated = g1.av1,
            videoClip = g1.videoClip,
            playlist = g2.playlist,
            extractAudio = g2.extractAudio,
            audioConvert = g2.audioConvert,
            audioConversionFormat = g2.audioConvFormat,
            audioFormat = g2.audioFormat,
            audioQuality = g3.audioQuality,
            useCustomAudioPreset = g3.customPreset,
            audioEncoding = g3.audioEncoding
        )
    }

    private val subtitleSettingsStateFlow: Flow<SubtitleSettingsState> = combine(
        combine(
            repository.subtitleFlow, repository.embedSubtitleFlow, repository.keepSubtitleFilesFlow,
            repository.subtitleLanguageFlow, repository.autoSubtitleFlow
        ) { v1, v2, v3, v4, v5 ->
            object {
                val subtitle = v1
                val embed = v2
                val keep = v3
                val lang = v4
                val auto = v5
            }
        },
        combine(
            repository.convertSubtitleFlow, repository.autoTranslatedSubtitlesFlow
        ) { v6, v7 ->
            object {
                val convert = v6
                val autoTranslated = v7
            }
        }
    ) { g1, g2 ->
        SubtitleSettingsState(
            subtitle = g1.subtitle, embedSubtitle = g1.embed, keepSubtitleFiles = g1.keep,
            subtitleLanguage = g1.lang, autoSubtitle = g1.auto, convertSubtitle = g2.convert,
            autoTranslatedSubtitles = g2.autoTranslated
        )
    }

    private val networkSettingsStateFlow: Flow<NetworkSettingsState> = combine(
        combine(
            repository.concurrentFlow, repository.cellularDownloadFlow, repository.aria2cFlow,
            repository.proxyFlow, repository.proxyUrlFlow
        ) { v1, v2, v3, v4, v5 ->
            object {
                val concurrent = v1
                val cellular = v2
                val aria2c = v3
                val proxy = v4
                val proxyUrl = v5
            }
        },
        combine(
            repository.cookiesFlow, repository.userAgentFlow, repository.userAgentStringFlow,
            repository.rateLimitFlow, repository.maxRateFlow
        ) { v6, v7, v8, v9, v10 ->
            object {
                val cookies = v6
                val userAgent = v7
                val userAgentString = v8
                val rateLimit = v9
                val maxRate = v10
            }
        },
        combine(
            repository.retriesFlow, repository.fragmentRetriesFlow, repository.forceIpv4Flow
        ) { v11, v12, v13 ->
            object {
                val retries = v11
                val fragRetries = v12
                val ipv4 = v13
            }
        }
    ) { g1, g2, g3 ->
        NetworkSettingsState(
            concurrent = g1.concurrent,
            cellularDownload = g1.cellular,
            aria2c = g1.aria2c,
            proxy = g1.proxy,
            proxyUrl = g1.proxyUrl,
            cookies = g2.cookies,
            userAgent = g2.userAgent,
            userAgentString = g2.userAgentString,
            rateLimit = g2.rateLimit,
            maxRate = g2.maxRate,
            retries = g3.retries,
            fragmentRetries = g3.fragRetries,
            forceIpv4 = g3.ipv4
        )
    }

    private val postProcessingSettingsStateFlow: Flow<PostProcessingSettingsState> = combine(
        combine(
            repository.thumbnailFlow, repository.embedThumbnailFlow, repository.embedMetadataFlow,
            repository.cropArtworkFlow, repository.mergeOutputMkvFlow
        ) { v1, v2, v3, v4, v5 ->
            object {
                val thumb = v1
                val embedThumb = v2
                val embedMeta = v3
                val crop = v4
                val mergeMkv = v5
            }
        },
        repository.mergeMultiAudioStreamFlow
    ) { g1, mergeAudio ->
        PostProcessingSettingsState(
            thumbnail = g1.thumb, embedThumbnail = g1.embedThumb, embedMetadata = g1.embedMeta,
            cropArtwork = g1.crop, mergeOutputMkv = g1.mergeMkv, mergeMultiAudioStream = mergeAudio
        )
    }

    private val advancedSettingsStateFlow: Flow<AdvancedSettingsState> = combine(
        repository.customCommandFlow,
        repository.sponsorBlockFlow,
        repository.sponsorBlockCategoriesFlow,
        repository.templateIdFlow
    ) { command, sponsorBlock, categories, templateId ->
        AdvancedSettingsState(command, sponsorBlock, categories, templateId)
    }


    // A single StateFlow that combines all preference flows into one UI state object.
    // The UI can collect this single flow to get updates for any setting.
    val settingsStateFlow: StateFlow<SettingsUiState> = combine(
        combine(
            generalSettingsStateFlow,
            fileSettingsStateFlow,
            appearanceSettingsStateFlow,
            mediaSettingsStateFlow,
            subtitleSettingsStateFlow,
        ) { v1, v2, v3, v4, v5 ->
            object {
                val general = v1
                val file = v2
                val appearance = v3
                val media = v4
                val subtitle = v5
            }
        },
        postProcessingSettingsStateFlow,
        advancedSettingsStateFlow,
        networkSettingsStateFlow,
    ) { group1, postProcessing, advanced, network ->
        SettingsUiState(
            general = group1.general,
            file = group1.file,
            appearance = group1.appearance,
            media = group1.media,
            subtitle = group1.subtitle,
            network = network,
            postProcessing = postProcessing,
            advanced = advanced
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )


    init {
        viewModelScope.launch {
            repository.populateDefaultsIfFirstRun()
        }
    }


    // --- Functions to update settings ---
    // The UI calls these methods to update a preference.
    // Each function launches a coroutine and calls the repository's suspend function.


    // --- General & Core Settings ---
    fun setConfigure(isEnabled: Boolean) =
        viewModelScope.launch { repository.setConfigure(isEnabled) }

    fun setDebug(isEnabled: Boolean) = viewModelScope.launch { repository.setDebug(isEnabled) }
    fun setWelcomeDialog(isEnabled: Boolean) =
        viewModelScope.launch { repository.setWelcomeDialog(isEnabled) }

    fun setNotification(isEnabled: Boolean) =
        viewModelScope.launch { repository.setNotification(isEnabled) }

    fun setAutoUpdate(isEnabled: Boolean) =
        viewModelScope.launch { repository.setAutoUpdate(isEnabled) }

    fun setUpdateChannel(channel: String) =
        viewModelScope.launch { repository.setUpdateChannel(channel) }

    fun setPrivateMode(isEnabled: Boolean) =
        viewModelScope.launch { repository.setPrivateMode(isEnabled) }

    fun setPreventDuplicateDownloads(type: PreventDuplicateDownload) =
        viewModelScope.launch { repository.setPreventDuplicateDownloads(type) }

    fun setDownloadTypeInitialization(isEnabled: Boolean) =
        viewModelScope.launch { repository.setDownloadTypeInitialization(isEnabled) }

    fun setPreferredDownloadType(type: DownloadType) =
        viewModelScope.launch { repository.setPreferredDownloadType(type) }

    // --- File & Directory Management ---
    fun setVideoDirectory(path: String) =
        viewModelScope.launch { repository.setVideoDirectory(path) }

    fun setAudioDirectory(path: String) =
        viewModelScope.launch { repository.setAudioDirectory(path) }

    fun setCommandDirectory(path: String) =
        viewModelScope.launch { repository.setCommandDirectory(path) }

    fun setSubdirectoryPlaylistTitle(isEnabled: Boolean) =
        viewModelScope.launch { repository.setSubdirectoryPlaylistTitle(isEnabled) }

    fun setFilenameTemplateVideo(template: String) =
        viewModelScope.launch { repository.setFilenameTemplateVideo(template) }

    fun setFilenameTemplateAudio(template: String) =
        viewModelScope.launch { repository.setFilenameTemplateAudio(template) }

    fun setDownloadArchive(isEnabled: Boolean) =
        viewModelScope.launch { repository.setDownloadArchive(isEnabled) }

    fun setRestrictFilenames(isEnabled: Boolean) =
        viewModelScope.launch { repository.setRestrictFilenames(isEnabled) }

    // --- UI & Theme Settings ---
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun setAmoled(isEnabled: Boolean) = viewModelScope.launch { repository.setAmoled(isEnabled) }
    fun setAccentColor(color: Color) = viewModelScope.launch { repository.setAccentColor(color) }
    fun setContrastValue(value: Float) =
        viewModelScope.launch { repository.setContrastValue(value) }

    fun setDynamicColor(isEnabled: Boolean) =
        viewModelScope.launch { repository.setDynamicColor(isEnabled) }

    fun setThemeColor(color: AppTheme) = viewModelScope.launch { repository.setThemeColor(color) }

    // --- Video Settings ---
    fun setVideoFormat(format: VideoFormat) =
        viewModelScope.launch { repository.setVideoFormat(format) }

    fun setVideoEncoding(encoding: VideoEncoding) =
        viewModelScope.launch { repository.setVideoEncoding(encoding) }

    fun setVideoQuality(quality: VideoQuality) =
        viewModelScope.launch { repository.setVideoQuality(quality) }

    fun setAv1HardwareAccelerated(isEnabled: Boolean) =
        viewModelScope.launch { repository.setAv1HardwareAccelerated(isEnabled) }

    fun setVideoClip(isEnabled: Boolean) =
        viewModelScope.launch { repository.setVideoClip(isEnabled) }

    fun setPlaylist(isEnabled: Boolean) =
        viewModelScope.launch { repository.setPlaylist(isEnabled) }

    // --- Audio Settings ---
    fun setExtractAudio(isEnabled: Boolean) =
        viewModelScope.launch { repository.setExtractAudio(isEnabled) }

    fun setAudioConvert(isEnabled: Boolean) =
        viewModelScope.launch { repository.setAudioConvert(isEnabled) }

    fun setAudioConversionFormat(format: AudioFormat) =
        viewModelScope.launch { repository.setAudioConversionFormat(format) }

    fun setAudioFormat(format: AudioFormat) =
        viewModelScope.launch { repository.setAudioFormat(format) }

    fun setAudioQuality(quality: AudioQuality) =
        viewModelScope.launch { repository.setAudioQuality(quality) }

    fun setUseCustomAudioPreset(isEnabled: Boolean) =
        viewModelScope.launch { repository.setUseCustomAudioPreset(isEnabled) }

    // --- Subtitle Settings ---
    fun setSubtitle(isEnabled: Boolean) =
        viewModelScope.launch { repository.setSubtitle(isEnabled) }

    fun setEmbedSubtitle(isEnabled: Boolean) =
        viewModelScope.launch { repository.setEmbedSubtitle(isEnabled) }

    fun setKeepSubtitleFiles(isEnabled: Boolean) =
        viewModelScope.launch { repository.setKeepSubtitleFiles(isEnabled) }

    fun setSubtitleLanguage(lang: String) =
        viewModelScope.launch { repository.setSubtitleLanguage(lang) }

    fun setAutoSubtitle(isEnabled: Boolean) =
        viewModelScope.launch { repository.setAutoSubtitle(isEnabled) }

    fun setConvertSubtitle(format: SubtitlesFormat) =
        viewModelScope.launch { repository.setConvertSubtitle(format) }

    fun setAutoTranslatedSubtitles(isEnabled: Boolean) =
        viewModelScope.launch { repository.setAutoTranslatedSubtitles(isEnabled) }

    // --- Network & Performance ---
    fun setConcurrent(count: Int) = viewModelScope.launch { repository.setConcurrent(count) }
    fun setCellularDownload(isEnabled: Boolean) =
        viewModelScope.launch { repository.setCellularDownload(isEnabled) }

    fun setAria2c(isEnabled: Boolean) = viewModelScope.launch { repository.setAria2c(isEnabled) }
    fun setProxy(isEnabled: Boolean) = viewModelScope.launch { repository.setProxy(isEnabled) }
    fun setProxyUrl(url: String) = viewModelScope.launch { repository.setProxyUrl(url) }
    fun setCookies(cookies: String) = viewModelScope.launch { repository.setCookies(cookies) }
    fun setUserAgent(agent: String) = viewModelScope.launch { repository.setUserAgent(agent) }
    fun setUserAgentString(agentString: String) =
        viewModelScope.launch { repository.setUserAgentString(agentString) }

    fun setRateLimit(isEnabled: Boolean) =
        viewModelScope.launch { repository.setRateLimit(isEnabled) }

    fun setMaxRate(rate: Int) = viewModelScope.launch { repository.setMaxRate(rate) }
    fun setRetries(count: Int) = viewModelScope.launch { repository.setRetries(count) }
    fun setFragmentRetries(count: Int) =
        viewModelScope.launch { repository.setFragmentRetries(count) }

    fun setForceIpv4(isEnabled: Boolean) =
        viewModelScope.launch { repository.setForceIpv4(isEnabled) }

    // --- Post-Processing & Metadata ---
    fun setThumbnail(isEnabled: Boolean) =
        viewModelScope.launch { repository.setThumbnail(isEnabled) }

    fun setEmbedThumbnail(isEnabled: Boolean) =
        viewModelScope.launch { repository.setEmbedThumbnail(isEnabled) }

    fun setEmbedMetadata(isEnabled: Boolean) =
        viewModelScope.launch { repository.setEmbedMetadata(isEnabled) }

    fun setCropArtwork(isEnabled: Boolean) =
        viewModelScope.launch { repository.setCropArtwork(isEnabled) }

    fun setMergeOutputMkv(isEnabled: Boolean) =
        viewModelScope.launch { repository.setMergeOutputMkv(isEnabled) }

    fun setMergeMultiAudioStream(isEnabled: Boolean) =
        viewModelScope.launch { repository.setMergeMultiAudioStream(isEnabled) }

    // --- Advanced & Experimental ---
    fun setCustomCommand(command: String) =
        viewModelScope.launch { repository.setCustomCommand(command) }

    fun setSponsorBlock(isEnabled: Boolean) =
        viewModelScope.launch { repository.setSponsorBlock(isEnabled) }

    fun setSponsorBlockCategories(categories: Set<String>) =
        viewModelScope.launch { repository.setSponsorBlockCategories(categories) }

    fun setTemplateId(id: Int) = viewModelScope.launch { repository.setTemplateId(id) }

}
