package com.ghost.zeku.domain.model.search

import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus

/**
 * This class tells the UI what filters to display.
 * If a provider doesn't support Tags, it returns 'supportsTags = false',
 * and the UI completely hides the Tags section!
 */
data class SearchCapabilities(
    val supportsGenres: Boolean = false,
    val supportsTags: Boolean = false, // MAL doesn't have tags!
    val supportsYear: Boolean = false,
    val supportsSeason: Boolean = false,

    // Lists of exactly what this provider allows
    val supportedFormats: List<MediaFormat> = emptyList(),
    val supportedStatus: List<MediaReleaseStatus> = emptyList(),
    val supportedSorts: List<SearchSort> = emptyList(),

    // This allows you to fetch the actual string lists for the UI chips
    val availableGenres: List<String> = emptyList(),
    val availableTags: List<String> = emptyList()
)