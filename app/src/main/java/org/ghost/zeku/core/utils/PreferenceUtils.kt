package org.ghost.zeku.core.utils

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey


object PreferenceKeys {
    // --- System Keys ---
    val IS_INITIALIZED = booleanPreferencesKey("is_initialized")
    val SETTINGS_VERSION = intPreferencesKey("settings_version")

    // --- General & Core Settings ---
    val CONFIGURE = booleanPreferencesKey("configure")
    val DEBUG = booleanPreferencesKey("debug")
    val WELCOME_DIALOG = booleanPreferencesKey("welcome_dialog")
    val NOTIFICATION = booleanPreferencesKey("notification")
    val AUTO_UPDATE = booleanPreferencesKey("auto_update")
    val UPDATE_CHANNEL = stringPreferencesKey("update_channel")
    val PRIVATE_MODE = booleanPreferencesKey("private_mode")
    val PREVENT_DUPLICATE_DOWNLOADS = intPreferencesKey("prevent_duplicate_downloads")
    val DOWNLOAD_TYPE_INITIALIZATION = booleanPreferencesKey("download_type_init")
    val PREFERRED_DOWNLOAD_TYPE = stringPreferencesKey("preferred_download_type")

    // --- File & Directory Management ---
    val VIDEO_DIRECTORY = stringPreferencesKey("download_dir")
    val AUDIO_DIRECTORY = stringPreferencesKey("audio_dir")
    val COMMAND_DIRECTORY = stringPreferencesKey("command_directory")
    val SUBDIRECTORY_PLAYLIST_TITLE = booleanPreferencesKey("subdirectory_playlist_title")
    val FILENAME_TEMPLATE_VIDEO = stringPreferencesKey("filename_template_video")
    val FILENAME_TEMPLATE_AUDIO = stringPreferencesKey("filename_template_audio")
    val DOWNLOAD_ARCHIVE = booleanPreferencesKey("download_archive")
    val RESTRICT_FILENAMES = booleanPreferencesKey("restrict_filenames")

    // --- UI & Theme Settings ---
    val AMOLED_THEME = booleanPreferencesKey("amoled_theme")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val ACCENT_COLOR = stringPreferencesKey("accent_color")
    val CONTRAST_VALUE = floatPreferencesKey("contrast_value")
    val THEME_COLOR = stringPreferencesKey("theme_color")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")


    // --- Video Settings ---
    val VIDEO_FORMAT = stringPreferencesKey("video_format")
    val VIDEO_ENCODING = stringPreferencesKey("video_encoding")
    val VIDEO_QUALITY = stringPreferencesKey("quality")
    val AV1_HARDWARE_ACCELERATED = booleanPreferencesKey("av1_hardware_accelerated")
    val VIDEO_CLIP = booleanPreferencesKey("video_clip")
    val PLAYLIST = booleanPreferencesKey("playlist")

    // --- Audio Settings ---
    val EXTRACT_AUDIO = booleanPreferencesKey("extract_audio")
    val AUDIO_CONVERT = booleanPreferencesKey("audio_convert")
    val AUDIO_CONVERSION_FORMAT = stringPreferencesKey("audio_convert_format")
    val AUDIO_FORMAT = stringPreferencesKey("audio_format_preferred")
    val AUDIO_ENCODING = stringPreferencesKey("audio_encoding")
    val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
    val USE_CUSTOM_AUDIO_PRESET = booleanPreferencesKey("custom_audio_preset")

    // --- Subtitle Settings ---
    val SUBTITLE = booleanPreferencesKey("subtitle")
    val EMBED_SUBTITLE = booleanPreferencesKey("embed_subtitle")
    val KEEP_SUBTITLE_FILES = booleanPreferencesKey("keep_subtitle")
    val SUBTITLE_LANGUAGE = stringPreferencesKey("sub_lang")
    val AUTO_SUBTITLE = booleanPreferencesKey("auto_subtitle")
    val CONVERT_SUBTITLE = stringPreferencesKey("convert_subtitle")
    val AUTO_TRANSLATED_SUBTITLES = booleanPreferencesKey("translated_subs")

    // --- Network & Performance ---
    val CONCURRENT = intPreferencesKey("concurrent_fragments")
    val CELLULAR_DOWNLOAD = booleanPreferencesKey("cellular_download")
    val ARIA2C = booleanPreferencesKey("aria2c")
    val PROXY = booleanPreferencesKey("proxy")
    val PROXY_URL = stringPreferencesKey("proxy_url")
    val COOKIES = stringPreferencesKey("cookies")
    val USER_AGENT = stringPreferencesKey("user_agent")
    val USER_AGENT_STRING = stringPreferencesKey("user_agent_string")
    val RATE_LIMIT = booleanPreferencesKey("rate_limit")
    val MAX_RATE = intPreferencesKey("max_rate")
    val RETRIES = intPreferencesKey("retries")
    val FRAGMENT_RETRIES = intPreferencesKey("fragment_retries")
    val FORCE_IPV4 = booleanPreferencesKey("force_ipv4")

    // --- Post-Processing & Metadata ---
    val THUMBNAIL = booleanPreferencesKey("create_thumbnail")
    val EMBED_THUMBNAIL = booleanPreferencesKey("embed_thumbnail")
    val EMBED_METADATA = booleanPreferencesKey("embed_metadata")
    val CROP_ARTWORK = booleanPreferencesKey("crop_artwork")
    val MERGE_OUTPUT_MKV = booleanPreferencesKey("merge_to_mkv")
    val MERGE_MULTI_AUDIO_STREAM = booleanPreferencesKey("multi_audio_stream")

    // --- Advanced & Experimental ---
    val CUSTOM_COMMAND = stringPreferencesKey("custom_command")
    val SPONSORBLOCK = booleanPreferencesKey("sponsorblock")
    val SPONSORBLOCK_CATEGORIES = stringSetPreferencesKey("sponsorblock_categories")
    val TEMPLATE_ID = intPreferencesKey("template_id")
}

object PreferenceKeyDefaults {

    // --- System Defaults ---
    const val SETTINGS_VERSION = 1

    // General & Core
    const val CONFIGURE = false
    const val DEBUG = false
    const val WELCOME_DIALOG = true
    const val NOTIFICATION = true
    const val AUTO_UPDATE = true
    const val UPDATE_CHANNEL = "stable"
    const val PRIVATE_MODE = false
    const val PREVENT_DUPLICATE_DOWNLOADS = 0
    const val DOWNLOAD_TYPE_INITIALIZATION = false
    const val PREFERRED_DOWNLOAD_TYPE = "video"

    // File & Directory
    const val VIDEO_DIRECTORY = "Downloads/Videos"
    const val AUDIO_DIRECTORY = "Downloads/Audio"
    const val COMMAND_DIRECTORY = "Downloads"
    const val SUBDIRECTORY_PLAYLIST_TITLE = false
    const val FILENAME_TEMPLATE_VIDEO = "%(title)s.%(ext)s"
    const val FILENAME_TEMPLATE_AUDIO = "%(title)s.%(ext)s"
    const val DOWNLOAD_ARCHIVE = false
    const val RESTRICT_FILENAMES = false

    // UI & Theme
    const val AMOLED_THEME = false
    const val THEME_MODE = "system"

    const val ACCENT_COLOR = "#000000"
    const val CONTRAST_VALUE = 0.0f
    const val THEME_COLOR = "sky_blue"
    const val DYNAMIC_COLOR = true

    // Video
    const val VIDEO_FORMAT = "mkv"
    const val VIDEO_ENCODING = "h264"
    const val VIDEO_QUALITY = "best"
    const val AV1_HARDWARE_ACCELERATED = false
    const val VIDEO_CLIP = false
    const val PLAYLIST = true

    // Audio
    const val EXTRACT_AUDIO = false
    const val AUDIO_CONVERT = false
    const val AUDIO_CONVERSION_FORMAT = "m4a"
    const val AUDIO_FORMAT = "m4a"
    const val AUDIO_ENCODING = "opus"
    const val AUDIO_QUALITY = "192k"
    const val USE_CUSTOM_AUDIO_PRESET = false

    // Subtitle
    const val SUBTITLE = false
    const val EMBED_SUBTITLE = false
    const val KEEP_SUBTITLE_FILES = true
    const val SUBTITLE_LANGUAGE = "en"
    const val AUTO_SUBTITLE = false
    const val CONVERT_SUBTITLE = "default"
    const val AUTO_TRANSLATED_SUBTITLES = false

    // Network
    const val CONCURRENT = 4
    const val CELLULAR_DOWNLOAD = false
    const val ARIA2C = false
    const val PROXY = false
    const val PROXY_URL = ""
    const val COOKIES = ""
    const val USER_AGENT = "default"
    const val USER_AGENT_STRING = "Mozilla/5.0"
    const val RATE_LIMIT = false
    const val MAX_RATE = 0
    const val RETRIES = 3
    const val FRAGMENT_RETRIES = 2
    const val FORCE_IPV4 = false

    // Post-Processing
    const val THUMBNAIL = true
    const val EMBED_THUMBNAIL = false
    const val EMBED_METADATA = true
    const val CROP_ARTWORK = false
    const val MERGE_OUTPUT_MKV = false
    const val MERGE_MULTI_AUDIO_STREAM = false

    // Advanced
    const val CUSTOM_COMMAND = ""
    const val SPONSORBLOCK = false
    const val SPONSORBLOCK_CATEGORIES = "sponsor, intro, outro"
    const val TEMPLATE_ID = 0
}