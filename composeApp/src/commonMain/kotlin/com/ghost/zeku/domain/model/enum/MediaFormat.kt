package com.ghost.zeku.domain.model.enum

enum class MediaFormat {
    TV, TV_SHORT, MOVIE, SPECIAL, OVA, ONA, MUSIC, MANGA, NOVEL, ONE_SHOT, UNKNOWN;

    companion object {
        fun fromString(value: String?): MediaFormat {
            if (value.isNullOrBlank()) return UNKNOWN
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}