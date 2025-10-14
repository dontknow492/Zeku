package org.ghost.zeku.ui.screen.settings

import androidx.compose.ui.graphics.Color
import org.ghost.zeku.core.DownloadType
import org.ghost.zeku.core.enum.AudioEncoding
import org.ghost.zeku.core.enum.AudioFormat
import org.ghost.zeku.core.enum.AudioQuality
import org.ghost.zeku.core.enum.PreventDuplicateDownload
import org.ghost.zeku.core.enum.ThemeMode
import org.ghost.zeku.core.enum.VideoEncoding
import org.ghost.zeku.core.enum.VideoFormat
import org.ghost.zeku.core.enum.VideoQuality
import org.ghost.zeku.repository.toColor
import org.ghost.zeku.ui.theme.AppTheme
import org.ghost.zeku.core.utils.PreferenceKeyDefaults as Defaults

/**
 * Represents the complete UI state for all settings screens.
 * This single data class serves as the source of truth for all preference values,
 * ensuring consistency across the UI.
 *
 * @property isLoading Indicates if the initial settings are being loaded.
 */
@Suppress("KotlinConstantConditions")
data class SettingsUiState(
    val isLoading: Boolean = true,

    val general: GeneralSettingsState = GeneralSettingsState(
        configure = Defaults.CONFIGURE,
        debug = Defaults.DEBUG,
        welcomeDialog = Defaults.WELCOME_DIALOG,
        notification = Defaults.NOTIFICATION,
        autoUpdate = Defaults.AUTO_UPDATE,
        updateChannel = Defaults.UPDATE_CHANNEL,
        privateMode = Defaults.PRIVATE_MODE,
        preventDuplicateDownloads = PreventDuplicateDownload.fromValue(Defaults.PREVENT_DUPLICATE_DOWNLOADS),
        preferredDownloadType = DownloadType.fromString(Defaults.PREFERRED_DOWNLOAD_TYPE),
    ),
    val file: FileSettingsState = FileSettingsState(
        videoDirectory = Defaults.VIDEO_DIRECTORY,
        audioDirectory = Defaults.AUDIO_DIRECTORY,
        commandDirectory = Defaults.COMMAND_DIRECTORY,
        subdirectoryPlaylistTitle = Defaults.SUBDIRECTORY_PLAYLIST_TITLE,
        filenameTemplateVideo = Defaults.FILENAME_TEMPLATE_VIDEO,
        filenameTemplateAudio = Defaults.FILENAME_TEMPLATE_AUDIO,
        downloadArchive = Defaults.DOWNLOAD_ARCHIVE,
        restrictFilenames = Defaults.RESTRICT_FILENAMES,
    ),
    val appearance: AppearanceSettingsState = AppearanceSettingsState(
        themeMode = ThemeMode.fromString(Defaults.THEME_MODE),
        accentColor = Defaults.ACCENT_COLOR.toColor(),
        theme = AppTheme.fromName(Defaults.THEME_COLOR),
        amoled = Defaults.AMOLED_THEME,
        dynamicColor = Defaults.DYNAMIC_COLOR,
        highContrast = Defaults.CONTRAST_VALUE,
    ),
    val media: MediaSettingsState = MediaSettingsState(
        videoFormat = VideoFormat.fromValue(Defaults.VIDEO_FORMAT),
        videoEncoding = VideoEncoding.fromValue(Defaults.VIDEO_ENCODING),
        videoQuality = VideoQuality.fromValue(Defaults.VIDEO_QUALITY),
        av1HardwareAccelerated = Defaults.AV1_HARDWARE_ACCELERATED,
        videoClip = Defaults.VIDEO_CLIP,
        playlist = Defaults.PLAYLIST,
        extractAudio = Defaults.EXTRACT_AUDIO,
        audioConvert = Defaults.AUDIO_CONVERT,
        audioConversionFormat = AudioFormat.fromValue(Defaults.AUDIO_CONVERSION_FORMAT),
        audioFormat = AudioFormat.fromValue(Defaults.AUDIO_FORMAT),
        audioQuality = AudioQuality.fromValue(Defaults.AUDIO_QUALITY),
        useCustomAudioPreset = Defaults.USE_CUSTOM_AUDIO_PRESET,
        audioEncoding = AudioEncoding.fromValue(Defaults.AUDIO_ENCODING)
    ),
    val subtitle: SubtitleSettingsState = SubtitleSettingsState(
        subtitle = Defaults.SUBTITLE,
        embedSubtitle = Defaults.EMBED_SUBTITLE,
        keepSubtitleFiles = Defaults.KEEP_SUBTITLE_FILES,
        subtitleLanguage = Defaults.SUBTITLE_LANGUAGE,
        autoSubtitle = Defaults.AUTO_SUBTITLE,
        convertSubtitle = Defaults.CONVERT_SUBTITLE,
        autoTranslatedSubtitles = Defaults.AUTO_TRANSLATED_SUBTITLES,
    ),
    val network: NetworkSettingsState = NetworkSettingsState(
        concurrent = Defaults.CONCURRENT,
        cellularDownload = Defaults.CELLULAR_DOWNLOAD,
        aria2c = Defaults.ARIA2C,
        proxy = Defaults.PROXY,
        proxyUrl = Defaults.PROXY_URL,
        cookies = Defaults.COOKIES,
        userAgent = Defaults.USER_AGENT,
        userAgentString = Defaults.USER_AGENT_STRING,
        rateLimit = Defaults.RATE_LIMIT,
        maxRate = Defaults.MAX_RATE,
        retries = Defaults.RETRIES,
        fragmentRetries = Defaults.FRAGMENT_RETRIES,
        forceIpv4 = Defaults.FORCE_IPV4,
    ),
    val postProcessing: PostProcessingSettingsState = PostProcessingSettingsState(
        thumbnail = Defaults.THUMBNAIL,
        embedThumbnail = Defaults.EMBED_THUMBNAIL,
        embedMetadata = Defaults.EMBED_METADATA,
        cropArtwork = Defaults.CROP_ARTWORK,
        mergeOutputMkv = Defaults.MERGE_OUTPUT_MKV,
        mergeMultiAudioStream = Defaults.MERGE_MULTI_AUDIO_STREAM,
    ),
    val advanced: AdvancedSettingsState = AdvancedSettingsState(
        customCommand = Defaults.CUSTOM_COMMAND,
        sponsorBlock = Defaults.SPONSORBLOCK,
        sponsorBlockCategories = Defaults.SPONSORBLOCK_CATEGORIES.split(",").toSet(),
        templateId = Defaults.TEMPLATE_ID,
    ),

    )


data class GeneralSettingsState(
    val configure: Boolean,
    val debug: Boolean,
    val welcomeDialog: Boolean,
    val notification: Boolean,
    val autoUpdate: Boolean,
    val updateChannel: String,
    val privateMode: Boolean,
    val preventDuplicateDownloads: PreventDuplicateDownload,
    val preferredDownloadType: DownloadType
)

data class FileSettingsState(
    val videoDirectory: String,
    val audioDirectory: String,
    val commandDirectory: String,
    val subdirectoryPlaylistTitle: Boolean,
    val filenameTemplateVideo: String,
    val filenameTemplateAudio: String,
    val downloadArchive: Boolean,
    val restrictFilenames: Boolean
)

data class AppearanceSettingsState(
    val themeMode: ThemeMode,
    val accentColor: Color,
    val theme: AppTheme,
    val amoled: Boolean,
    val dynamicColor: Boolean,
    val highContrast: Float,
)

data class MediaSettingsState(
    // Video
    val videoFormat: VideoFormat,
    val videoEncoding: VideoEncoding,
    val videoQuality: VideoQuality,
    val av1HardwareAccelerated: Boolean,
    val videoClip: Boolean,
    val playlist: Boolean,
    // Audio
    val extractAudio: Boolean,
    val audioConvert: Boolean,
    val audioEncoding: AudioEncoding,
    val audioConversionFormat: AudioFormat,
    val audioFormat: AudioFormat,
    val audioQuality: AudioQuality,
    val useCustomAudioPreset: Boolean
)

data class SubtitleSettingsState(
    val subtitle: Boolean,
    val embedSubtitle: Boolean,
    val keepSubtitleFiles: Boolean,
    val subtitleLanguage: String,
    val autoSubtitle: Boolean,
    val convertSubtitle: String,
    val autoTranslatedSubtitles: Boolean
)

data class NetworkSettingsState(
    val concurrent: Int,
    val cellularDownload: Boolean,
    val aria2c: Boolean,
    val proxy: Boolean,
    val proxyUrl: String,
    val cookies: String,
    val userAgent: String,
    val userAgentString: String,
    val rateLimit: Boolean,
    val maxRate: Int,
    val retries: Int,
    val fragmentRetries: Int,
    val forceIpv4: Boolean
)

data class PostProcessingSettingsState(
    val thumbnail: Boolean,
    val embedThumbnail: Boolean,
    val embedMetadata: Boolean,
    val cropArtwork: Boolean,
    val mergeOutputMkv: Boolean,
    val mergeMultiAudioStream: Boolean
)

data class AdvancedSettingsState(
    val customCommand: String,
    val sponsorBlock: Boolean,
    val sponsorBlockCategories: Set<String>,
    val templateId: Int
)
