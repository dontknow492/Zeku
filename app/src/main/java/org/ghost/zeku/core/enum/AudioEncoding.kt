package org.ghost.zeku.core.enum

/**
 * Represents the desired audio encoding format after extraction.
 * @param uiName The user-friendly string to display in the UI.
 * @param commandArg The value for the downloader's `--audio-format` argument.
 */
enum class AudioEncoding(val value: String, val uiName: String, val commandArg: String) {
    OPUS("opus", "Opus (ogg)", "ogg"),
    M4A("m4a", "M4A", "mp4"),
    DEFAULT("default", "Default (m4a)", "m4a"); // m4a is a common, high-quality default

    companion object {
        fun fromValue(value: String): AudioEncoding {
            return entries.find { it.value == value } ?: DEFAULT
        }
    }
}


