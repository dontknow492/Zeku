package com.ghost.zeku.domain.model.media

enum class MediaReleaseStatus {
    RELEASING,
    FINISHED,
    NOT_YET_RELEASED,
    CANCELLED,
    HIATUS,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): MediaReleaseStatus {
            if (value.isNullOrBlank()) return UNKNOWN
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: UNKNOWN
        }
    }
}