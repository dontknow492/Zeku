package com.ghost.zeku.domain.model.filter.search

import com.ghost.zeku.domain.model.media.MediaFormat
import com.ghost.zeku.domain.model.media.MediaReleaseStatus
import com.ghost.zeku.domain.model.media.MediaSeason
import com.ghost.zeku.domain.model.media.MediaType

/**
 * The "Mega Filter" object.
 * This represents the User's intent from the UI. Everything is nullable or has a default.
 */
data class MediaSearchFilter(

    // ------------------------------------------------------------------------
    // Media Type
    // ------------------------------------------------------------------------

    val mediaType: MediaType,

    // ------------------------------------------------------------------------
    // Include Filters
    // ------------------------------------------------------------------------

    val includedGenres: List<String> = emptyList(),

    val includedTags: List<String> = emptyList(),

    // ------------------------------------------------------------------------
    // Exclude Filters
    // ------------------------------------------------------------------------

    val excludedGenres: List<String> = emptyList(),

    val excludedTags: List<String> = emptyList(),

    // ------------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------------

    val year: Int? = null,

    val season: MediaSeason? = null,

    val format: MediaFormat? = null,

    val status: MediaReleaseStatus? = null,

    val countryOfOrigin: String? = null,

    val adult: Boolean? = null,

    // ------------------------------------------------------------------------
    // Anime Filters
    // ------------------------------------------------------------------------

    val minEpisodes: Int? = null,

    val maxEpisodes: Int? = null,

    // ------------------------------------------------------------------------
    // Manga / Novel Filters
    // ------------------------------------------------------------------------

    val minChapters: Int? = null,

    val maxChapters: Int? = null,

    // ------------------------------------------------------------------------
    // Score / Popularity
    // ------------------------------------------------------------------------

    val minScore: Float? = null,

    val maxScore: Float? = null,

    // ------------------------------------------------------------------------
    // Sorting
    // ------------------------------------------------------------------------

    val sort: SearchSort = SearchSort.TRENDING_DESC
)
// --- Enums to standardize the UI choices ---

enum class SearchSort {
    TRENDING_DESC, POPULARITY_DESC, SCORE_DESC, START_DATE_DESC, ALPHABETICAL_ASC
}


