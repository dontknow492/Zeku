package com.ghost.zeku.domain.model.filter.search

import com.ghost.zeku.domain.model.media.MediaFormat
import com.ghost.zeku.domain.model.media.MediaReleaseStatus
import com.ghost.zeku.domain.model.media.MediaType

/**
 * This class tells the UI what filters to display.
 * If a provider doesn't support Tags, it returns 'supportsTags = false',
 * and the UI completely hides the Tags section!
 */
data class SearchCapabilities(

    // ------------------------------------------------------------------------
    // Feature Support
    // ------------------------------------------------------------------------

    val supportsGenres: Boolean = false,

    val supportsTags: Boolean = false,

    val supportsYear: Boolean = false,

    val supportsSeason: Boolean = false,

    val supportsCountry: Boolean = false,

    val supportsSort: Boolean = true,

    val supportsAdult: Boolean = false,

    // ------------------------------------------------------------------------
    // Supported Values
    // ------------------------------------------------------------------------

    val supportedMediaTypes: List<MediaType> = emptyList(),

    val supportedFormats: List<MediaFormat> = emptyList(),

    val supportedStatus: List<MediaReleaseStatus> = emptyList(),

    val supportedSorts: List<SearchSort> = emptyList(),

    // ------------------------------------------------------------------------
    // Dynamic Filter Values
    // ------------------------------------------------------------------------

    val availableGenres: List<String> = emptyList(),

    val availableTags: List<String> = emptyList(),

    val availableCountries: List<String> = emptyList()
)