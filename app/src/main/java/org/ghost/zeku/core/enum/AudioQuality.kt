package org.ghost.zeku.core.enum

/**
 * Represents the desired audio quality for a download.
 * @param uiName The user-friendly string to display in the UI (e.g., in a dropdown).
 * @param commandArg The command-line argument for the downloader's format selection.
 * This is based on yt-dlp's format selection syntax.
 */
enum class AudioQuality(
    val value: String,      // The stable ID for storage. THIS NEVER CHANGES.
    val uiName: String,     // The display name for the UI. Can be changed freely.
    val commandArg: String  // The argument for the downloader process.
) {
    HIGHEST("highest", "Best quality", "bestaudio"),
    BIT_320("320k", "320 kbps", "bestaudio[abr<=320]"),
    BIT_256("256k", "256 kbps", "bestaudio[abr<=256]"),
    BIT_192("192k", "192 kbps", "bestaudio[abr<=192]"),
    BIT_128("128k", "128 kbps", "bestaudio[abr<=128]"),
    BIT_96("96k", "96 kbps", "bestaudio[abr<=96]"),
    BIT_64("64k", "64 kbps", "bestaudio[abr<=64]"),
    BIT_32("32k", "32 kbps", "bestaudio[abr<=32]"),
    LOWEST("lowest", "Lowest quality", "worstaudio"),

    // A sensible default for most users.
    DEFAULT("default", "Default quality", "bestaudio[abr<=192]");

    companion object {
        /**
         * Finds an AudioQuality enum by its stable storage 'value'.
         */
        fun fromValue(value: String?): AudioQuality {
            return values().find { it.value == value } ?: DEFAULT
        }
    }
}