package com.ghost.zeku.domain.model.media

/**
 * Represents how a related media is connected.
 */
enum class RelationType {
    ADAPTATION, PREQUEL, SEQUEL, PARENT, SIDE_STORY, CHARACTER, SUMMARY, ALTERNATIVE, SPIN_OFF, OTHER, SOURCE, COMPILATION, CONTAINS, UNKNOWN;

    companion object {
        fun fromString(value: String?): RelationType {
            if (value.isNullOrBlank()) return UNKNOWN
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}


