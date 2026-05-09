package com.ghost.zeku.domain.model.media

/**
 * Represents the role of a character.
 */
enum class CharacterRole {
    MAIN, SUPPORTING, BACKGROUND, UNKNOWN;

    companion object {
        fun fromString(value: String?): CharacterRole {
            if (value.isNullOrBlank()) return UNKNOWN
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}