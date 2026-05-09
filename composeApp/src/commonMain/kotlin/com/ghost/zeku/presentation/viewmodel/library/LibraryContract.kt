package com.ghost.zeku.presentation.viewmodel.library

import com.ghost.zeku.domain.model.filter.MediaFilterState
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference

object LibraryContract {

    // Simple model to represent a Tab in your UI
    data class LibraryTab(
        val id: String,
        val title: String,
        val categoryId: Long? = null // Null if it's not a category-based tab
    )

    // ─── STATE ──────────────────────────────────────────────────────────────
    data class State(
        val isLoading: Boolean = false,

        // Settings
        val filterState: MediaFilterState = MediaFilterState(),
        val displayPreference: MediaDisplayPreference = MediaDisplayPreference(),

        // UI Layout mapping (Tabs for the Horizontal Pager)
        val tabs: List<LibraryTab> = emptyList(),
        val selectedTabId: String = "ALL", // Keeps Pager and TabRow in sync

        // ui state
        val filterSheetVisible: Boolean = false,
    )

    // ─── EVENTS (From UI to ViewModel) ──────────────────────────────────────
    sealed interface Event {
        // Filter & Display
        data class UpdateFilter(val newFilter: MediaFilterState) : Event
        data class UpdateDisplayPref(val newPref: MediaDisplayPreference) : Event
        data class SearchQueryChanged(val query: String) : Event
        object ClearFilters : Event

        // Pager Sync
        data class OnTabSelected(val tabId: String) : Event

        // Library Actions
        data class ToggleFavorite(val mediaId: Int, val mediaType: MediaType) : Event

        data class ToggleFilterSheet(val status: Boolean) : Event

        // UI Interaction
        object OnFilterClicked : Event // Triggers the BottomSheet effect
    }

    // ─── EFFECTS (One-off events like Navigation/Snackbars) ─────────────────
    sealed interface Effect {
        data class ShowSnackbar(val message: String) : Effect
        data class NavigateToDetail(val mediaId: Int) : Effect
        object OpenFilterBottomSheet : Effect
    }
}

