package com.ghost.zeku.domain.model.filter

import kotlinx.serialization.Serializable

/**
 * Maps the UI sort options directly to your Room Entity column names.
 */

@Serializable
enum class SortOption(val sqlColumnName: String) {
    TITLE("title"),
    NATIVE_TITLE("nativeTitle"), // Only if you add this to your entity
    POPULARITY("popularity"),
    SCORE("score"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt"),

    // These require joining the LibraryEntity in your query
    ADDED_AT("addedAt"),
    SYNCED_AT("syncedAt")
}