package com.ghost.zeku.domain.model.enum

import kotlinx.serialization.Serializable


@Serializable
// Your fixed media type
enum class MediaType {
    ANIME, MANGA, UNKNOWN;

    companion object {
        fun fromString(value: String?): MediaType {
            if (value.isNullOrBlank()) return UNKNOWN
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}