package com.ghost.zeku.domain.model.search

import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaSeason
import com.ghost.zeku.domain.model.enum.MediaStatus

/**
 * The "Mega Filter" object.
 * This represents the User's intent from the UI. Everything is nullable or has a default.
 */
data class AnimeSearchFilter(
    // Included items (AND/OR depends on the provider, but usually AND is best for filtering)
    val includedGenres: List<String> = emptyList(),
    val includedTags: List<String> = emptyList(),

    // Excluded items (NOT)
    val excludedGenres: List<String> = emptyList(),
    val excludedTags: List<String> = emptyList(),

    // Specific attributes
    val year: Int? = null,
    val season: MediaSeason? = null,
    val format: MediaFormat? = null,
    val status: MediaReleaseStatus? = null,

    // Sorting
    val sort: SearchSort = SearchSort.TRENDING_DESC
)

data class MangaSearchFilter(
    val includedGenres: List<String> = emptyList(),
    val excludedGenres: List<String> = emptyList(),
    val includedTags: List<String> = emptyList(),
    val excludedTags: List<String> = emptyList(),
    val format: MediaFormat? = null, // e.g., Manga vs Manhwa vs Novel
    val status: MediaReleaseStatus? = null,
    val sort: SearchSort = SearchSort.TRENDING_DESC
)

// --- Enums to standardize the UI choices ---

enum class SearchSort {
    TRENDING_DESC, POPULARITY_DESC, SCORE_DESC, START_DATE_DESC, ALPHABETICAL_ASC
}


