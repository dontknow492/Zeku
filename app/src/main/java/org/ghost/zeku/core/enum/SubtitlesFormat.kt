package org.ghost.zeku.core.enum

/**
 * Represents the desired format for subtitles.
 * @param uiName The user-friendly string to display in the UI.
 * @param commandArg The value for the downloader's `--sub-format` argument.
 */
enum class SubtitlesFormat(
    val value: String,      // The stable ID for storage. THIS NEVER CHANGES.
    val uiName: String,     // The display name for the UI. Can be changed freely.
    val commandArg: String  // The argument for the downloader process.
) {
    SRT("srt", "SRT", "srt"),
    VTT("vtt", "VTT", "vtt"),
    ASS("ass", "ASS", "ass"),
    LRC("lrc", "LRC", "lrc"),
    DEFAULT("default", "Default", "best");

    companion object {
        /**
         * Finds a SubtitlesFormat enum by its stable storage 'value'.
         */
        fun fromValue(value: String?): SubtitlesFormat {
            return values().find { it.value == value } ?: DEFAULT
        }
    }
}