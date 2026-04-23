package com.ghost.zeku.domain.model.enum

enum class MediaType {
    ANIME, MANGA, UNKNOWN;

    companion object {
        fun fromString(value: String?): MediaType {
            if (value.isNullOrBlank()) return UNKNOWN
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}