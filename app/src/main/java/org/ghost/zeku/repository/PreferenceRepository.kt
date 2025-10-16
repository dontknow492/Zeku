package org.ghost.zeku.repository

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ghost.zeku.core.DownloadType
import org.ghost.zeku.core.enum.AudioEncoding
import org.ghost.zeku.core.enum.AudioFormat
import org.ghost.zeku.core.enum.AudioQuality
import org.ghost.zeku.core.enum.PreventDuplicateDownload
import org.ghost.zeku.core.enum.SubtitlesFormat
import org.ghost.zeku.core.enum.ThemeMode
import org.ghost.zeku.core.enum.VideoEncoding
import org.ghost.zeku.core.enum.VideoFormat
import org.ghost.zeku.core.enum.VideoQuality
import org.ghost.zeku.ui.theme.AppTheme
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import org.ghost.zeku.core.utils.PreferenceKeyDefaults as Defaults
import org.ghost.zeku.core.utils.PreferenceKeys as Keys


fun Color.toHexCode(): String {
    return String.format("#%08X", this.toArgb())
}

fun String.isValidHex(): Boolean = this.matches(Regex("^#?([0-9a-fA-F]{6})$"))

fun String.toColor(default: Color = AppTheme.SkyBlue.seedColor): Color {
    return try {
        Color(this.toColorInt())
    } catch (e: Exception) {
        default
    }
}

/**
 * A repository for managing all user preferences using Jetpack DataStore.
 * This class provides type-safe accessors for reading and writing settings.
 */
@Singleton
@Suppress("KotlinConstantConditions")
class PreferenceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    /**
     * Populates the DataStore with default values on the very first app run.
     * This function checks an initialization flag and, if not set, writes all
     * default values from the [PreferenceKeyDefaults] object in a single transaction.
     */
    suspend fun populateDefaultsIfFirstRun() {
        Timber.d("Populate Defaults Settings Request")
        dataStore.edit { prefs ->
            if (prefs[Keys.IS_INITIALIZED] != true) {
                Timber.d("Populating Settings")
                // System
                prefs[Keys.SETTINGS_VERSION] = Defaults.SETTINGS_VERSION
                // General
                prefs[Keys.CONFIGURE] = Defaults.CONFIGURE
                prefs[Keys.DEBUG] = Defaults.DEBUG
                prefs[Keys.WELCOME_DIALOG] = Defaults.WELCOME_DIALOG
                prefs[Keys.NOTIFICATION] = Defaults.NOTIFICATION
                prefs[Keys.AUTO_UPDATE] = Defaults.AUTO_UPDATE
                prefs[Keys.UPDATE_CHANNEL] = Defaults.UPDATE_CHANNEL
                prefs[Keys.PRIVATE_MODE] = Defaults.PRIVATE_MODE
                prefs[Keys.PREVENT_DUPLICATE_DOWNLOADS] = Defaults.PREVENT_DUPLICATE_DOWNLOADS
                prefs[Keys.DOWNLOAD_TYPE_INITIALIZATION] = Defaults.DOWNLOAD_TYPE_INITIALIZATION
                prefs[Keys.PREFERRED_DOWNLOAD_TYPE] = Defaults.PREFERRED_DOWNLOAD_TYPE
                // File & Directory
                prefs[Keys.VIDEO_DIRECTORY] = Defaults.VIDEO_DIRECTORY
                prefs[Keys.AUDIO_DIRECTORY] = Defaults.AUDIO_DIRECTORY
                prefs[Keys.COMMAND_DIRECTORY] = Defaults.COMMAND_DIRECTORY
                prefs[Keys.SUBDIRECTORY_PLAYLIST_TITLE] = Defaults.SUBDIRECTORY_PLAYLIST_TITLE
                prefs[Keys.FILENAME_TEMPLATE_VIDEO] = Defaults.FILENAME_TEMPLATE_VIDEO
                prefs[Keys.FILENAME_TEMPLATE_AUDIO] = Defaults.FILENAME_TEMPLATE_AUDIO
                prefs[Keys.DOWNLOAD_ARCHIVE] = Defaults.DOWNLOAD_ARCHIVE
                prefs[Keys.RESTRICT_FILENAMES] = Defaults.RESTRICT_FILENAMES
                // UI & Theme
                prefs[Keys.AMOLED_THEME] = Defaults.AMOLED_THEME
                prefs[Keys.CONTRAST_VALUE] = Defaults.CONTRAST_VALUE
                prefs[Keys.THEME_MODE] = Defaults.THEME_MODE
                prefs[Keys.THEME_COLOR] = Defaults.THEME_COLOR
                prefs[Keys.DYNAMIC_COLOR] = Defaults.DYNAMIC_COLOR
                prefs[Keys.ACCENT_COLOR] = Defaults.ACCENT_COLOR
                // Video
                prefs[Keys.VIDEO_FORMAT] = Defaults.VIDEO_FORMAT
                prefs[Keys.VIDEO_ENCODING] = Defaults.VIDEO_ENCODING
                prefs[Keys.VIDEO_QUALITY] = Defaults.VIDEO_QUALITY
                prefs[Keys.AV1_HARDWARE_ACCELERATED] = Defaults.AV1_HARDWARE_ACCELERATED
                prefs[Keys.VIDEO_CLIP] = Defaults.VIDEO_CLIP
                prefs[Keys.PLAYLIST] = Defaults.PLAYLIST
                // Audio
                prefs[Keys.EXTRACT_AUDIO] = Defaults.EXTRACT_AUDIO
                prefs[Keys.AUDIO_CONVERT] = Defaults.AUDIO_CONVERT
                prefs[Keys.AUDIO_CONVERSION_FORMAT] = Defaults.AUDIO_CONVERSION_FORMAT
                prefs[Keys.AUDIO_FORMAT] = Defaults.AUDIO_FORMAT
                prefs[Keys.AUDIO_QUALITY] = Defaults.AUDIO_QUALITY
                prefs[Keys.USE_CUSTOM_AUDIO_PRESET] = Defaults.USE_CUSTOM_AUDIO_PRESET
                // Subtitle
                prefs[Keys.SUBTITLE] = Defaults.SUBTITLE
                prefs[Keys.EMBED_SUBTITLE] = Defaults.EMBED_SUBTITLE
                prefs[Keys.KEEP_SUBTITLE_FILES] = Defaults.KEEP_SUBTITLE_FILES
                prefs[Keys.SUBTITLE_LANGUAGE] = Defaults.SUBTITLE_LANGUAGE
                prefs[Keys.AUTO_SUBTITLE] = Defaults.AUTO_SUBTITLE
                prefs[Keys.CONVERT_SUBTITLE] = Defaults.CONVERT_SUBTITLE
                prefs[Keys.AUTO_TRANSLATED_SUBTITLES] = Defaults.AUTO_TRANSLATED_SUBTITLES
                // Network
                prefs[Keys.CONCURRENT] = Defaults.CONCURRENT
                prefs[Keys.CELLULAR_DOWNLOAD] = Defaults.CELLULAR_DOWNLOAD
                prefs[Keys.ARIA2C] = Defaults.ARIA2C
                prefs[Keys.PROXY] = Defaults.PROXY
                prefs[Keys.PROXY_URL] = Defaults.PROXY_URL
                prefs[Keys.COOKIES] = Defaults.COOKIES
                prefs[Keys.USER_AGENT] = Defaults.USER_AGENT
                prefs[Keys.USER_AGENT_STRING] = Defaults.USER_AGENT_STRING
                prefs[Keys.RATE_LIMIT] = Defaults.RATE_LIMIT
                prefs[Keys.MAX_RATE] = Defaults.MAX_RATE
                prefs[Keys.RETRIES] = Defaults.RETRIES
                prefs[Keys.FRAGMENT_RETRIES] = Defaults.FRAGMENT_RETRIES
                prefs[Keys.FORCE_IPV4] = Defaults.FORCE_IPV4
                // Post-Processing
                prefs[Keys.THUMBNAIL] = Defaults.THUMBNAIL
                prefs[Keys.EMBED_THUMBNAIL] = Defaults.EMBED_THUMBNAIL
                prefs[Keys.EMBED_METADATA] = Defaults.EMBED_METADATA
                prefs[Keys.CROP_ARTWORK] = Defaults.CROP_ARTWORK
                prefs[Keys.MERGE_OUTPUT_MKV] = Defaults.MERGE_OUTPUT_MKV
                prefs[Keys.MERGE_MULTI_AUDIO_STREAM] = Defaults.MERGE_MULTI_AUDIO_STREAM
                // Advanced
                prefs[Keys.CUSTOM_COMMAND] = Defaults.CUSTOM_COMMAND
                prefs[Keys.SPONSORBLOCK] = Defaults.SPONSORBLOCK
                prefs[Keys.SPONSORBLOCK_CATEGORIES] =
                    Defaults.SPONSORBLOCK_CATEGORIES.split(",").toSet()
                prefs[Keys.TEMPLATE_ID] = Defaults.TEMPLATE_ID

                // Set the initialization flag to true so this doesn't run again
                prefs[Keys.IS_INITIALIZED] = true
            } else {
                Timber.d("Settings already populated")
            }
        }

    }

    //region --- Flows for Reading Preferences ---

    // --- System ---
    val settingsVersionFlow: Flow<Int> =
        dataStore.data.map { it[Keys.SETTINGS_VERSION] ?: Defaults.SETTINGS_VERSION }

    // --- General & Core Settings ---
    val configureFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.CONFIGURE] ?: Defaults.CONFIGURE }
    val debugFlow: Flow<Boolean> = dataStore.data.map { it[Keys.DEBUG] ?: Defaults.DEBUG }
    val welcomeDialogFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.WELCOME_DIALOG] ?: Defaults.WELCOME_DIALOG }
    val notificationFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.NOTIFICATION] ?: Defaults.NOTIFICATION }
    val autoUpdateFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.AUTO_UPDATE] ?: Defaults.AUTO_UPDATE }
    val updateChannelFlow: Flow<String> =
        dataStore.data.map { it[Keys.UPDATE_CHANNEL] ?: Defaults.UPDATE_CHANNEL }
    val privateModeFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.PRIVATE_MODE] ?: Defaults.PRIVATE_MODE }
    val preventDuplicateDownloadsFlow: Flow<PreventDuplicateDownload> = dataStore.data.map {
        PreventDuplicateDownload.fromValue(
            it[Keys.PREVENT_DUPLICATE_DOWNLOADS] ?: Defaults.PREVENT_DUPLICATE_DOWNLOADS
        )
    }
    val downloadTypeInitializationFlow: Flow<Boolean> = dataStore.data.map {
        it[Keys.DOWNLOAD_TYPE_INITIALIZATION] ?: Defaults.DOWNLOAD_TYPE_INITIALIZATION
    }
    val preferredDownloadTypeFlow: Flow<DownloadType> = dataStore.data.map {
        DownloadType.fromString(
            it[Keys.PREFERRED_DOWNLOAD_TYPE] ?: Defaults.PREFERRED_DOWNLOAD_TYPE
        )
    }

    // --- File & Directory Management ---
    val videoDirectoryFlow: Flow<String> =
        // dataStore: DataStore<Preferences> (assumed to be available)
        dataStore.data.map { preferences ->
            preferences[Keys.VIDEO_DIRECTORY]
            // Use stored value, OR compute default path
                ?: (Defaults.DOWNLOADS_COLLECTION_URI.path + "/" + Defaults.VIDEO_DIRECTORY)
                // Use a default relative directory name if the path is null (e.g., on content:// URIs)
                ?: Defaults.VIDEO_DIRECTORY
        }
    val audioDirectoryFlow: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[Keys.AUDIO_DIRECTORY]
            // Use stored value, OR compute default path
                ?: (Defaults.DOWNLOADS_COLLECTION_URI.path + "/" + Defaults.AUDIO_DIRECTORY)
                // Use a default relative directory name if the path is null
                ?: Defaults.AUDIO_DIRECTORY
        }

    val commandDirectoryFlow: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[Keys.COMMAND_DIRECTORY]
            // Use stored value, OR compute default path
                ?: (Defaults.DOWNLOADS_COLLECTION_URI.path + "/" + Defaults.COMMAND_DIRECTORY)
                // Use a default relative directory name if the path is null
                ?: Defaults.COMMAND_DIRECTORY
        }

    val subdirectoryPlaylistTitleFlow: Flow<Boolean> = dataStore.data.map {
        it[Keys.SUBDIRECTORY_PLAYLIST_TITLE] ?: Defaults.SUBDIRECTORY_PLAYLIST_TITLE
    }
    val filenameTemplateVideoFlow: Flow<String> =
        dataStore.data.map { it[Keys.FILENAME_TEMPLATE_VIDEO] ?: Defaults.FILENAME_TEMPLATE_VIDEO }
    val filenameTemplateAudioFlow: Flow<String> =
        dataStore.data.map { it[Keys.FILENAME_TEMPLATE_AUDIO] ?: Defaults.FILENAME_TEMPLATE_AUDIO }
    val downloadArchiveFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.DOWNLOAD_ARCHIVE] ?: Defaults.DOWNLOAD_ARCHIVE }
    val restrictFilenamesFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.RESTRICT_FILENAMES] ?: Defaults.RESTRICT_FILENAMES }

    // --- UI & Theme Settings ---
//    const val AMOLED_THEME = false
//    const val THEME_MODE = "system"
//
//    const val ACCENT_COLOR = "#000000"
//    const val CONTRAST_VALUE = 0.0f
//    const val THEME_COLOR = "sky_blue"
//    const val DYNAMIC_COLOR = true


    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map {
        ThemeMode.fromString(it[Keys.THEME_MODE] ?: Defaults.THEME_MODE)
    }

    val accentColorFlow: Flow<Color> = dataStore.data.map {
        val color = it[Keys.ACCENT_COLOR] ?: Defaults.ACCENT_COLOR
        if (color.isValidHex()) color.toColor()
        else Defaults.ACCENT_COLOR.toColor()
    }

    val amoledFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.AMOLED_THEME] ?: Defaults.AMOLED_THEME }
    val themeColorFlow: Flow<AppTheme> = dataStore.data.map {
        val color = it[Keys.THEME_COLOR] ?: Defaults.THEME_COLOR
        AppTheme.fromName(color)
    }
    val dynamicColorFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: Defaults.DYNAMIC_COLOR }
    val highContrastFlow: Flow<Float> =
        dataStore.data.map { it[Keys.CONTRAST_VALUE] ?: Defaults.CONTRAST_VALUE }

    // --- Video Settings ---
    val videoFormatFlow: Flow<VideoFormat> = dataStore.data.map { p ->
        VideoFormat.fromValue(
            p[Keys.VIDEO_FORMAT] ?: Defaults.VIDEO_FORMAT
        )
    }
    val videoEncodingFlow: Flow<VideoEncoding> = dataStore.data.map { p ->
        VideoEncoding.fromValue(
            p[Keys.VIDEO_ENCODING] ?: Defaults.VIDEO_ENCODING
        )
    }
    val videoQualityFlow: Flow<VideoQuality> = dataStore.data.map { p ->
        VideoQuality.fromValue(
            p[Keys.VIDEO_QUALITY] ?: Defaults.VIDEO_QUALITY
        )
    }
    val av1HardwareAcceleratedFlow: Flow<Boolean> = dataStore.data.map {
        it[Keys.AV1_HARDWARE_ACCELERATED] ?: Defaults.AV1_HARDWARE_ACCELERATED
    }
    val videoClipFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.VIDEO_CLIP] ?: Defaults.VIDEO_CLIP }
    val playlistFlow: Flow<Boolean> = dataStore.data.map { it[Keys.PLAYLIST] ?: Defaults.PLAYLIST }

    // --- Audio Settings ---
    val extractAudioFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.EXTRACT_AUDIO] ?: Defaults.EXTRACT_AUDIO }
    val audioConvertFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.AUDIO_CONVERT] ?: Defaults.AUDIO_CONVERT }
    val audioConversionFormatFlow: Flow<AudioFormat> = dataStore.data.map { p ->
        AudioFormat.fromValue(
            p[Keys.AUDIO_CONVERSION_FORMAT] ?: Defaults.AUDIO_CONVERSION_FORMAT
        )
    }
    val audioFormatFlow: Flow<AudioFormat> = dataStore.data.map { p ->
        AudioFormat.fromValue(
            p[Keys.AUDIO_FORMAT] ?: Defaults.AUDIO_FORMAT
        )
    }
    val audioQualityFlow: Flow<AudioQuality> = dataStore.data.map { p ->
        AudioQuality.fromValue(
            p[Keys.AUDIO_QUALITY] ?: Defaults.AUDIO_QUALITY
        )
    }
    val useCustomAudioPresetFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.USE_CUSTOM_AUDIO_PRESET] ?: Defaults.USE_CUSTOM_AUDIO_PRESET }

    val audioEncodingFlow: Flow<AudioEncoding> = dataStore.data.map {
        AudioEncoding.fromValue(it[Keys.AUDIO_ENCODING] ?: Defaults.AUDIO_ENCODING)
    }


    // --- Subtitle Settings ---
    val subtitleFlow: Flow<Boolean> = dataStore.data.map { it[Keys.SUBTITLE] ?: Defaults.SUBTITLE }
    val embedSubtitleFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.EMBED_SUBTITLE] ?: Defaults.EMBED_SUBTITLE }
    val keepSubtitleFilesFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.KEEP_SUBTITLE_FILES] ?: Defaults.KEEP_SUBTITLE_FILES }
    val subtitleLanguageFlow: Flow<String> =
        dataStore.data.map { it[Keys.SUBTITLE_LANGUAGE] ?: Defaults.SUBTITLE_LANGUAGE }
    val autoSubtitleFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.AUTO_SUBTITLE] ?: Defaults.AUTO_SUBTITLE }
    val convertSubtitleFlow: Flow<SubtitlesFormat> =
        dataStore.data.map {
            SubtitlesFormat.fromValue(it[Keys.CONVERT_SUBTITLE] ?: Defaults.CONVERT_SUBTITLE)
        }
    val autoTranslatedSubtitlesFlow: Flow<Boolean> = dataStore.data.map {
        it[Keys.AUTO_TRANSLATED_SUBTITLES] ?: Defaults.AUTO_TRANSLATED_SUBTITLES
    }

    // --- Network & Performance ---
    val concurrentFlow: Flow<Int> =
        dataStore.data.map { it[Keys.CONCURRENT] ?: Defaults.CONCURRENT }
    val cellularDownloadFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.CELLULAR_DOWNLOAD] ?: Defaults.CELLULAR_DOWNLOAD }
    val aria2cFlow: Flow<Boolean> = dataStore.data.map { it[Keys.ARIA2C] ?: Defaults.ARIA2C }
    val proxyFlow: Flow<Boolean> = dataStore.data.map { it[Keys.PROXY] ?: Defaults.PROXY }
    val proxyUrlFlow: Flow<String> = dataStore.data.map { it[Keys.PROXY_URL] ?: Defaults.PROXY_URL }
    val cookiesFlow: Flow<String> = dataStore.data.map { it[Keys.COOKIES] ?: Defaults.COOKIES }
    val userAgentFlow: Flow<String> =
        dataStore.data.map { it[Keys.USER_AGENT] ?: Defaults.USER_AGENT }
    val userAgentStringFlow: Flow<String> =
        dataStore.data.map { it[Keys.USER_AGENT_STRING] ?: Defaults.USER_AGENT_STRING }
    val rateLimitFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.RATE_LIMIT] ?: Defaults.RATE_LIMIT }
    val maxRateFlow: Flow<Int> = dataStore.data.map { it[Keys.MAX_RATE] ?: Defaults.MAX_RATE }
    val retriesFlow: Flow<Int> = dataStore.data.map { it[Keys.RETRIES] ?: Defaults.RETRIES }
    val fragmentRetriesFlow: Flow<Int> =
        dataStore.data.map { it[Keys.FRAGMENT_RETRIES] ?: Defaults.FRAGMENT_RETRIES }
    val forceIpv4Flow: Flow<Boolean> =
        dataStore.data.map { it[Keys.FORCE_IPV4] ?: Defaults.FORCE_IPV4 }

    // --- Post-Processing & Metadata ---
    val thumbnailFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.THUMBNAIL] ?: Defaults.THUMBNAIL }
    val embedThumbnailFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.EMBED_THUMBNAIL] ?: Defaults.EMBED_THUMBNAIL }
    val embedMetadataFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.EMBED_METADATA] ?: Defaults.EMBED_METADATA }
    val cropArtworkFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.CROP_ARTWORK] ?: Defaults.CROP_ARTWORK }
    val mergeOutputMkvFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.MERGE_OUTPUT_MKV] ?: Defaults.MERGE_OUTPUT_MKV }
    val mergeMultiAudioStreamFlow: Flow<Boolean> = dataStore.data.map {
        it[Keys.MERGE_MULTI_AUDIO_STREAM] ?: Defaults.MERGE_MULTI_AUDIO_STREAM
    }

    // --- Advanced & Experimental ---
    val customCommandFlow: Flow<String> =
        dataStore.data.map { it[Keys.CUSTOM_COMMAND] ?: Defaults.CUSTOM_COMMAND }
    val sponsorBlockFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.SPONSORBLOCK] ?: Defaults.SPONSORBLOCK }
    val sponsorBlockCategoriesFlow: Flow<Set<String>> = dataStore.data.map {
        it[Keys.SPONSORBLOCK_CATEGORIES] ?: Defaults.SPONSORBLOCK_CATEGORIES.split(",").toSet()
    }
    val templateIdFlow: Flow<Int> =
        dataStore.data.map { it[Keys.TEMPLATE_ID] ?: Defaults.TEMPLATE_ID }

    //endregion

    //region --- Suspend Functions for Writing Preferences ---

    // --- General & Core Settings ---
    suspend fun setConfigure(isEnabled: Boolean) {
        dataStore.edit { it[Keys.CONFIGURE] = isEnabled }
    }

    suspend fun setDebug(isEnabled: Boolean) = dataStore.edit { it[Keys.DEBUG] = isEnabled }
    suspend fun setWelcomeDialog(isEnabled: Boolean) =
        dataStore.edit { it[Keys.WELCOME_DIALOG] = isEnabled }

    suspend fun setNotification(isEnabled: Boolean) =
        dataStore.edit { it[Keys.NOTIFICATION] = isEnabled }

    suspend fun setAutoUpdate(isEnabled: Boolean) =
        dataStore.edit { it[Keys.AUTO_UPDATE] = isEnabled }

    suspend fun setUpdateChannel(channel: String) =
        dataStore.edit { it[Keys.UPDATE_CHANNEL] = channel }

    suspend fun setPrivateMode(isEnabled: Boolean) =
        dataStore.edit { it[Keys.PRIVATE_MODE] = isEnabled }

    suspend fun setPreventDuplicateDownloads(type: PreventDuplicateDownload) =
        dataStore.edit { it[Keys.PREVENT_DUPLICATE_DOWNLOADS] = type.value }

    suspend fun setDownloadTypeInitialization(isEnabled: Boolean) =
        dataStore.edit { it[Keys.DOWNLOAD_TYPE_INITIALIZATION] = isEnabled }

    suspend fun setPreferredDownloadType(type: DownloadType) =
        dataStore.edit { it[Keys.PREFERRED_DOWNLOAD_TYPE] = type.toString().lowercase() }

    // --- File & Directory Management ---
    /**
     * Saves or clears the video directory Uri.
     * @param uri The Uri to save. Pass null to clear the setting.
     */
    suspend fun setVideoDirectory(uri: Uri?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[Keys.VIDEO_DIRECTORY] = uri.path
                    ?: (Defaults.DOWNLOADS_COLLECTION_URI.path + "/" + Defaults.VIDEO_DIRECTORY)
            }
        }
    }

    /**
     * Saves or clears the audio directory Uri.
     * @param uri The Uri to save. Pass null to clear the setting.
     */
    suspend fun setAudioDirectory(uri: Uri?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[Keys.AUDIO_DIRECTORY] = uri.path
                    ?: (Defaults.DOWNLOADS_COLLECTION_URI.path + "/" + Defaults.AUDIO_DIRECTORY)
            }
        }
    }

    /**
     * Saves or clears the command directory Uri.
     * @param uri The Uri to save. Pass null to clear the setting.
     */
    suspend fun setCommandDirectory(uri: Uri?) {
        dataStore.edit { preferences ->
            if (uri != null) {
                preferences[Keys.COMMAND_DIRECTORY] = uri.path
                    ?: (Defaults.DOWNLOADS_COLLECTION_URI.path + "/" + Defaults.COMMAND_DIRECTORY)
            }
        }
    }

    suspend fun setSubdirectoryPlaylistTitle(isEnabled: Boolean) =
        dataStore.edit { it[Keys.SUBDIRECTORY_PLAYLIST_TITLE] = isEnabled }

    suspend fun setFilenameTemplateVideo(template: String) =
        dataStore.edit { it[Keys.FILENAME_TEMPLATE_VIDEO] = template }

    suspend fun setFilenameTemplateAudio(template: String) =
        dataStore.edit { it[Keys.FILENAME_TEMPLATE_AUDIO] = template }

    suspend fun setDownloadArchive(isEnabled: Boolean) =
        dataStore.edit { it[Keys.DOWNLOAD_ARCHIVE] = isEnabled }

    suspend fun setRestrictFilenames(isEnabled: Boolean) =
        dataStore.edit { it[Keys.RESTRICT_FILENAMES] = isEnabled }

    // --- UI & Theme Settings ---
    suspend fun setThemeMode(mode: ThemeMode) =
        dataStore.edit { it[Keys.THEME_MODE] = mode.toString() }

    suspend fun setAmoled(isEnabled: Boolean) = dataStore.edit { it[Keys.AMOLED_THEME] = isEnabled }
    suspend fun setAccentColor(color: Color) =
        dataStore.edit { it[Keys.ACCENT_COLOR] = color.toHexCode() }

    suspend fun setThemeColor(appTheme: AppTheme) {
        when (appTheme) {
            is AppTheme.Custom ->
                setAccentColor(appTheme.seedColor)

            else -> {
                dataStore.edit { it[Keys.THEME_COLOR] = appTheme.name }
            }
        }
    }

    suspend fun setDynamicColor(isEnabled: Boolean) =
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = isEnabled }

    suspend fun setContrastValue(value: Float) = dataStore.edit { it[Keys.CONTRAST_VALUE] = value }

    // --- Video Settings ---
    suspend fun setVideoFormat(format: VideoFormat) =
        dataStore.edit { it[Keys.VIDEO_FORMAT] = format.value }

    suspend fun setVideoEncoding(encoding: VideoEncoding) =
        dataStore.edit { it[Keys.VIDEO_ENCODING] = encoding.value }

    suspend fun setVideoQuality(quality: VideoQuality) =
        dataStore.edit { it[Keys.VIDEO_QUALITY] = quality.value }

    suspend fun setAv1HardwareAccelerated(isEnabled: Boolean) =
        dataStore.edit { it[Keys.AV1_HARDWARE_ACCELERATED] = isEnabled }

    suspend fun setVideoClip(isEnabled: Boolean) =
        dataStore.edit { it[Keys.VIDEO_CLIP] = isEnabled }

    suspend fun setPlaylist(isEnabled: Boolean) = dataStore.edit { it[Keys.PLAYLIST] = isEnabled }

    // --- Audio Settings ---
    suspend fun setExtractAudio(isEnabled: Boolean) =
        dataStore.edit { it[Keys.EXTRACT_AUDIO] = isEnabled }

    suspend fun setAudioConvert(isEnabled: Boolean) =
        dataStore.edit { it[Keys.AUDIO_CONVERT] = isEnabled }

    suspend fun setAudioEncoding(encoding: AudioEncoding) =
        dataStore.edit { it[Keys.AUDIO_ENCODING] = encoding.value }

    suspend fun setAudioConversionFormat(format: AudioFormat) =
        dataStore.edit { it[Keys.AUDIO_CONVERSION_FORMAT] = format.value }

    suspend fun setAudioFormat(format: AudioFormat) =
        dataStore.edit { it[Keys.AUDIO_FORMAT] = format.value }

    suspend fun setAudioQuality(quality: AudioQuality) =
        dataStore.edit { it[Keys.AUDIO_QUALITY] = quality.value }

    suspend fun setUseCustomAudioPreset(isEnabled: Boolean) =
        dataStore.edit { it[Keys.USE_CUSTOM_AUDIO_PRESET] = isEnabled }

    // --- Subtitle Settings ---
    suspend fun setSubtitle(isEnabled: Boolean) = dataStore.edit { it[Keys.SUBTITLE] = isEnabled }
    suspend fun setEmbedSubtitle(isEnabled: Boolean) =
        dataStore.edit { it[Keys.EMBED_SUBTITLE] = isEnabled }

    suspend fun setKeepSubtitleFiles(isEnabled: Boolean) =
        dataStore.edit { it[Keys.KEEP_SUBTITLE_FILES] = isEnabled }

    suspend fun setSubtitleLanguage(lang: String) =
        dataStore.edit { it[Keys.SUBTITLE_LANGUAGE] = lang }

    suspend fun setAutoSubtitle(isEnabled: Boolean) =
        dataStore.edit { it[Keys.AUTO_SUBTITLE] = isEnabled }

    suspend fun setConvertSubtitle(format: SubtitlesFormat) =
        dataStore.edit { it[Keys.CONVERT_SUBTITLE] = format.value }

    suspend fun setAutoTranslatedSubtitles(isEnabled: Boolean) =
        dataStore.edit { it[Keys.AUTO_TRANSLATED_SUBTITLES] = isEnabled }

    // --- Network & Performance ---
    suspend fun setConcurrent(count: Int) = dataStore.edit { it[Keys.CONCURRENT] = count }
    suspend fun setCellularDownload(isEnabled: Boolean) =
        dataStore.edit { it[Keys.CELLULAR_DOWNLOAD] = isEnabled }

    suspend fun setAria2c(isEnabled: Boolean) = dataStore.edit { it[Keys.ARIA2C] = isEnabled }
    suspend fun setProxy(isEnabled: Boolean) = dataStore.edit { it[Keys.PROXY] = isEnabled }
    suspend fun setProxyUrl(url: String) = dataStore.edit { it[Keys.PROXY_URL] = url }
    suspend fun setCookies(cookies: String) = dataStore.edit { it[Keys.COOKIES] = cookies }
    suspend fun setUserAgent(agent: String) = dataStore.edit { it[Keys.USER_AGENT] = agent }
    suspend fun setUserAgentString(agentString: String) =
        dataStore.edit { it[Keys.USER_AGENT_STRING] = agentString }

    suspend fun setRateLimit(isEnabled: Boolean) =
        dataStore.edit { it[Keys.RATE_LIMIT] = isEnabled }

    suspend fun setMaxRate(rate: Int) = dataStore.edit { it[Keys.MAX_RATE] = rate }
    suspend fun setRetries(count: Int) = dataStore.edit { it[Keys.RETRIES] = count }
    suspend fun setFragmentRetries(count: Int) =
        dataStore.edit { it[Keys.FRAGMENT_RETRIES] = count }

    suspend fun setForceIpv4(isEnabled: Boolean) =
        dataStore.edit { it[Keys.FORCE_IPV4] = isEnabled }

    // --- Post-Processing & Metadata ---
    suspend fun setThumbnail(isEnabled: Boolean) = dataStore.edit { it[Keys.THUMBNAIL] = isEnabled }
    suspend fun setEmbedThumbnail(isEnabled: Boolean) =
        dataStore.edit { it[Keys.EMBED_THUMBNAIL] = isEnabled }

    suspend fun setEmbedMetadata(isEnabled: Boolean) =
        dataStore.edit { it[Keys.EMBED_METADATA] = isEnabled }

    suspend fun setCropArtwork(isEnabled: Boolean) =
        dataStore.edit { it[Keys.CROP_ARTWORK] = isEnabled }

    suspend fun setMergeOutputMkv(isEnabled: Boolean) =
        dataStore.edit { it[Keys.MERGE_OUTPUT_MKV] = isEnabled }

    suspend fun setMergeMultiAudioStream(isEnabled: Boolean) =
        dataStore.edit { it[Keys.MERGE_MULTI_AUDIO_STREAM] = isEnabled }

    // --- Advanced & Experimental ---
    suspend fun setCustomCommand(command: String) =
        dataStore.edit { it[Keys.CUSTOM_COMMAND] = command }

    suspend fun setSponsorBlock(isEnabled: Boolean) =
        dataStore.edit { it[Keys.SPONSORBLOCK] = isEnabled }

    suspend fun setSponsorBlockCategories(categories: Set<String>) =
        dataStore.edit { it[Keys.SPONSORBLOCK_CATEGORIES] = categories }

    suspend fun setTemplateId(id: Int) = dataStore.edit { it[Keys.TEMPLATE_ID] = id }

    //endregion
}

