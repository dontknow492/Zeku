package org.ghost.zeku.core

sealed class DownloadType {
    object Audio : DownloadType()
    object Video : DownloadType()
    object Command : DownloadType()
    data class Playlist(val startIndex: Int? = null, val endIndex: Int? = null) : DownloadType()

    companion object {
        fun fromString(type: String): DownloadType {
            return when (type) {
                "audio" -> Audio
                "video" -> Video
                "command" -> Command
                "playlist" -> Playlist()
                else -> throw IllegalArgumentException("Invalid download type: $type")
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            is Audio -> "audio"
            is Video -> "video"
            is Command -> "command"
            is Playlist -> "playlist"
        }

    }
}