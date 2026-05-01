package com.ghost.zeku.presentation.viewmodel.search

import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaSeason
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.search.SearchCapabilities
import com.ghost.zeku.domain.model.search.SearchSort
import com.ghost.zeku.presentation.navigation.Destination

interface SearchContract {

    data class State(
        // Core Config
        val query: String = "",
        val mediaType: MediaType = MediaType.ANIME,
        val activeProvider: ProviderType = ProviderType.MYANIMELIST,

        // The Capabilities (Drives what the UI is allowed to draw!)
        val capabilities: SearchCapabilities = SearchCapabilities(),

        // Active Filters
        val selectedGenres: List<String> = emptyList(),
        val selectedTags: List<String> = emptyList(),
        val selectedYear: Int? = null,
        val selectedSeason: MediaSeason? = null,
        val selectedFormat: MediaFormat? = null,
        val selectedStatus: MediaReleaseStatus? = null,
        val selectedSort: SearchSort = SearchSort.TRENDING_DESC,

        // UI State
        val isFilterSheetOpen: Boolean = false,
        val isInitializing: Boolean = true // True while fetching initial capabilities
    )

    sealed interface Event {
        // Initialization & Core Config
        data class Initialize(val defaultType: MediaType = MediaType.ANIME) : Event
        data class OnQueryChange(val query: String) : Event
        data class ChangeProvider(val provider: ProviderType) : Event
        data class ChangeMediaType(val type: MediaType) : Event

        // Filter Interactions
        data class ToggleGenre(val genre: String) : Event
        data class ToggleTag(val tag: String) : Event
        data class SelectYear(val year: Int?) : Event
        data class SelectSeason(val season: MediaSeason?) : Event
        data class SelectFormat(val format: MediaFormat?) : Event
        data class SelectStatus(val status: MediaReleaseStatus?) : Event
        data class SelectSort(val sort: SearchSort) : Event

        // UI Actions
        data class SetFilterSheetVisibility(val isOpen: Boolean) : Event
        object ClearAllFilters : Event

        // Navigation
        data class OnMediaClick(val mediaId: Int, val type: MediaType) : Event
    }

    sealed interface Effect {
        data class Navigate(val destination: Destination) : Effect
        data class ShowMessage(val message: String, val isError: Boolean = false) : Effect
    }
}