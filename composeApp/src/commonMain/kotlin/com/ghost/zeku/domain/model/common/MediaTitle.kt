package com.ghost.zeku.domain.model.common

import kotlinx.serialization.Serializable


@Serializable
data class MediaTitle(
    val romaji: String? = null,
    val english: String? = null,
    val native: String? = null,
    val userPreferred: String? = null,
) {
    /**
     * Returns the best available title based on fallback logic.
     */
    fun getDisplayTitle(): String {
        return this.userPreferred ?: this.english ?: this.romaji ?: this.native ?: "Unknown Title"
    }
}

