package org.ghost.zeku.core.enum

/**
 * Represents the desired video quality (resolution) for a download.
 * @param uiName The user-friendly string to display in the UI.
 * @param commandArg The command-line argument for the video stream selection.
 * This will typically be combined with an audio stream (e.g., "+bestaudio").
 */
enum class VideoQuality(
    val value: String,      // The stable ID for storage. THIS NEVER CHANGES.
    val uiName: String,     // The display name for the UI. Can be changed freely.
    val commandArg: String  // The argument for the downloader process.
) {
    BEST("best", "Best quality", "bestvideo"),
    RESOLUTION_2160P("2160p", "2160p (4K)", "bestvideo[height<=2160]"),
    RESOLUTION_1440P("1440p", "1440p (2K)", "bestvideo[height<=1440]"),
    RESOLUTION_1080P("1080p", "1080p (HD)", "bestvideo[height<=1080]"),
    RESOLUTION_720P("720p", "720p (HD)", "bestvideo[height<=720]"),
    RESOLUTION_480P("480p", "480p", "bestvideo[height<=480]"),
    RESOLUTION_360P("360p", "360p", "bestvideo[height<=360]"),
    LOWEST("lowest", "Lowest quality", "worstvideo"),

    // A sensible default for most users.
    DEFAULT("default", "Default", "bestvideo[height<=1080]");

    companion object {
        /**
         * Finds a VideoQuality enum by its stable storage 'value'.
         * This is the correct and safe way to deserialize the stored preference.
         */
        fun fromValue(value: String?): VideoQuality {
            return values().find { it.value == value } ?: DEFAULT
        }
    }
}


