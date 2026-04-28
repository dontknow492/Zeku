package com.ghost.zeku.presentation.viewmodel.home

import androidx.paging.PagingData
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import kotlinx.coroutines.flow.Flow

class HomeContract {

    // Represents a single horizontal or vertical row in the UI
    data class MediaSection(
        val title: String, // e.g., "Trending Now", "Popular"
        val categoryId: String, // The enum name used to trigger "View All"
        val data: Flow<PagingData<Media>>
    )

    data class State(
        val mediaType: MediaType, // ANIME or MANGA
        val isLoading: Boolean = true,
        val heroItems: List<Media> = emptyList(),

        // Dynamic horizontal categories (e.g., Trending, Popular, Top Rated)
        val horizontalSections: List<MediaSection> = emptyList(),

        // The one category reserved for the infinite vertical list at the bottom
        val verticalSection: MediaSection? = null,

        val error: String? = null
    )

    sealed interface Event {
        // User Actions
        data class OnMediaClick(val mediaId: Int) : Event
        data class OnViewAllClick(val categoryId: String, val title: String) : Event
        data object OnRefresh : Event

        // System / ViewModel Actions
        data class LoadHomeData(val mediaType: MediaType) : Event
    }

    sealed interface Effect {
        data class NavigateToDetail(val mediaId: Int, val mediaType: MediaType) : Effect
        data class NavigateToViewAll(val categoryId: String, val title: String, val mediaType: MediaType) : Effect
        data class ShowToast(val message: String) : Effect
    }
}