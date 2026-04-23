package com.ghost.zeku.domain.model.search

/**
 * This class tells the UI what filters to display.
 * If a provider doesn't support Tags, it returns 'supportsTags = false',
 * and the UI completely hides the Tags section!
 */
data class SearchCapabilities(
    // The exact list of genres this specific provider supports (MAL vs AniList differ greatly)
    val supportedGenres: List<String> = emptyList(),

    // The exact list of tags (usually empty for MAL, huge for AniList)
    val supportedTags: List<String> = emptyList(),

    // Booleans to tell the UI whether to show certain sections
    val supportsFormatFilter: Boolean = false,
    val supportsStatusFilter: Boolean = false,
    val supportsYearFilter: Boolean = false,
    val supportsSeasonFilter: Boolean = false,
    val supportsExclusion: Boolean = false, // E.g., Can it filter OUT a genre?

    // Which sorting options this provider can actually handle
    val supportedSorts: List<SearchSort> = listOf(SearchSort.POPULARITY_DESC)
)