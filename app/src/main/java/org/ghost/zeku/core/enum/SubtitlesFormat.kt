package org.ghost.zeku.core.enum

import androidx.annotation.StringRes

/**
 * Represents the desired format for subtitles.
 * @param uiName The user-friendly string to display in the UI.
 * @param commandArg The type for the downloader's `--sub-format` argument.
 */
enum class SubtitlesFormat(
    override val value: String,      // The stable ID for storage. THIS NEVER CHANGES.
    override val label: String,     // The display name for the UI. Can be changed freely.
    val commandArg: String,  // The argument for the downloader process.
    @param: StringRes override val descriptionResId: Int? = null,
) : SettingEnum {
    SRT("srt", "SRT", "srt"),
    VTT("vtt", "VTT", "vtt"),
    ASS("ass", "ASS", "ass"),
    LRC("lrc", "LRC", "lrc"),
    DEFAULT("default", "Default", "best");

    companion object {
        /**
         * Finds a SubtitlesFormat enum by its stable storage 'type'.
         */
        fun fromValue(value: String?): SubtitlesFormat {
            return values().find { it.value == value } ?: DEFAULT
        }
    }
}