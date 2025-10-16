package org.ghost.zeku.core.enum

import androidx.annotation.StringRes

/**
 * Represents the desired final video container/encoding.
 * @param uiName The user-friendly string to display in the UI.
 * @param commandArg The type for the downloader's `--recode-video` argument.
 * An empty string means no recoding is performed.
 */
enum class VideoEncoding(
    override val value: String,      // The stable ID for storage. THIS NEVER CHANGES.
    override val label: String,     // The display name for the UI. Can be changed freely.
    val commandArg: String,  // The argument for the downloader process.
    @param: StringRes override val descriptionResId: Int? = null,
) : SettingEnum {
    H264("h264", "H.264", "h264"),
    H265("h265", "H.265 (HEVC)", "h265"),
    VP9("vp9", "VP9", "vp9"),
    AV1("av1", "AV1", "av1"),
    DEFAULT(
        "no_recode",
        "No Recoding",
        ""
    ); // Empty string signifies not to use the recode argument

    companion object {
        /**
         * Finds a VideoEncoding enum by its stable storage 'type'.
         */
        fun fromValue(value: String?): VideoEncoding {
            return entries.find { it.value == value } ?: DEFAULT
        }
    }
}


