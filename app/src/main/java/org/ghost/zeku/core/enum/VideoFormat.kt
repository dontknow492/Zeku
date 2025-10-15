package org.ghost.zeku.core.enum

import androidx.annotation.StringRes

/**
 * Represents the desired final video format.
 * @param uiName The user-friendly string to display in the UI.
 * @param commandArg The value for the downloader's `--recode-video` argument.
 * An empty string means no recoding is performed.
 */
enum class VideoFormat(
    override val value: String,      // The stable ID for storage. THIS NEVER CHANGES.
    override val label: String,     // The display name for the UI. Can be changed freely.
    val commandArg: String,  // The argument for the downloader process.
    @param: StringRes override val descriptionResId: Int? = null,
) : SettingEnum {
    MP4("mp4", "MP4", "mp4"),
    MKV("mkv", "MKV", "mkv"),
    WEBM("webm", "WEBM", "webm"),
    MOV("mov", "MOV", "mov"),
    FLV("flv", "FLV", "flv"),
    AVI("avi", "AVI", "avi"),
    GIF("gif", "GIF", "gif"),

    // The default value should also have a stable identifier.
    DEFAULT("default", "Default", "mkv");

    companion object {
        /**
         * Finds a VideoFormat enum by its stable storage 'value'.
         * This is the correct and safe way to deserialize the stored preference.
         * It gracefully falls back to DEFAULT if the stored value is somehow invalid or not found.
         */
        fun fromValue(value: String?): VideoFormat {
            return values().find { it.value == value } ?: DEFAULT
        }
    }
}