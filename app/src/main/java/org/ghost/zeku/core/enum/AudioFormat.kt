package org.ghost.zeku.core.enum

/**
 * Represents the desired audio format after extraction.
 * @param uiName The user-friendly string to display in the UI.
 * @param commandArg The value for the downloader's `--audio-format` argument.
 */
enum class AudioFormat(
    val value: String,      // The stable ID for storage. THIS NEVER CHANGES.
    val uiName: String,     // The display name for the UI. Can be changed freely.
    val commandArg: String  // The argument for the downloader process.
) {
    AAC("aac", "AAC", "aac"),
    MP3("mp3", "MP3", "mp3"),
    FLAC("flac", "FLAC", "flac"),
    OGG("ogg_vorbis", "OGG (Vorbis)", "vorbis"),
    OPUS("opus", "Opus", "opus"),
    M4A("m4a", "M4A", "m4a"),
    WAV("wav", "WAV", "wav"),

    // m4a is a common, high-quality default.
    DEFAULT("default", "Default (m4a)", "m4a");

    companion object {
        /**
         * Finds an AudioFormat enum by its stable storage 'value'.
         */
        fun fromValue(value: String?): AudioFormat {
            return entries.find { it.value == value } ?: DEFAULT
        }
    }
}