package com.ghost.zeku.domain.model.filter

import com.ghost.zeku.domain.model.media.MediaReleaseStatus
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.ProviderType

/**
 * Represents the current state of the library/search filters.
 * A default instance means "Show everything, no filters applied."
 */
data class MediaFilterState(
    // ------------------------------------------------------------------------
    // 1. Searching
    // ------------------------------------------------------------------------
    val searchQuery: String = "",

    // ------------------------------------------------------------------------
    // 2. Filtering (Include / Exclude)
    // ------------------------------------------------------------------------
    val includedGenres: List<String> = emptyList(),
    val excludedGenres: List<String> = emptyList(),

    val includedTags: List<String> = emptyList(),
    val excludedTags: List<String> = emptyList(),

    // ------------------------------------------------------------------------
    // 3. Filtering (Categorical / Enums)
    // If a list is empty, it means "Don't filter by this" (show all).
    // ------------------------------------------------------------------------
    val providers: List<ProviderType> = emptyList(),
    val mediaTypes: List<MediaType> = emptyList(),
    val statuses: List<MediaReleaseStatus> = emptyList(),
    val formats: List<String> = emptyList(),

    // ------------------------------------------------------------------------
    // 4. Filtering (Ranges)
    // null means "no limit".
    // Example: 1999..2024
    // ------------------------------------------------------------------------
    val yearRange: IntRange? = null,

    // Example: 70..100 (if using AniList 100-point scale)
    val scoreRange: IntRange? = null,

    // ------------------------------------------------------------------------
    // 5. Sorting
    // ------------------------------------------------------------------------

    val sortBy: SortOption = SortOption.UPDATED_AT,
    val sortDirection: SortDirection = SortDirection.DESCENDING,

    // ------------------------------------------------------------------------
    // 6. Grouping (Handled in Kotlin, not SQL)
    // ------------------------------------------------------------------------
    val groupBy: GroupOption = GroupOption.NONE
) {
    /**
     * Helper to quickly check if any heavy filters are active.
     * Useful if you want to show a "Clear Filters" button in the UI.
     */
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
                includedGenres.isNotEmpty() || excludedGenres.isNotEmpty() ||
                includedTags.isNotEmpty() || excludedTags.isNotEmpty() ||
                providers.isNotEmpty() || mediaTypes.isNotEmpty() ||
                statuses.isNotEmpty() || formats.isNotEmpty() ||
                yearRange != null || scoreRange != null
}



