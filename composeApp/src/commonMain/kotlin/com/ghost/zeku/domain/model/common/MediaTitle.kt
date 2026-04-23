package com.ghost.zeku.domain.model.common

import kotlinx.serialization.Serializable


@Serializable
data class MediaTitle(
    val romaji: String?,
    val english: String?,
    val native: String?
) {
    /**
     * Returns the best available title based on fallback logic.
     */
    fun getPreferred(): String {
        return romaji ?: english ?: native ?: "Unknown Title"
    }
}

