package com.ghost.zeku.presentation.viewmodel.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.repository.MediaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        // Defaulting to ANIME, but this is immediately overridden by LoadHomeData
        HomeContract.State(mediaType = MediaType.ANIME)
    )
    val state: StateFlow<HomeContract.State> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HomeContract.Effect>()
    val effects: SharedFlow<HomeContract.Effect> = _effects.asSharedFlow()

    fun onEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.LoadHomeData -> loadData(event.mediaType)
            is HomeContract.Event.OnMediaClick -> {
                sendEffect(HomeContract.Effect.NavigateToDetail(event.mediaId, _state.value.mediaType))
            }

            is HomeContract.Event.OnViewAllClick -> {
                sendEffect(HomeContract.Effect.NavigateToViewAll(event.categoryId, event.title, _state.value.mediaType))
            }

            is HomeContract.Event.OnRefresh -> {
                // Refresh logic - Paging items handle their own refresh,
                // but we might want to re-fetch the hero banner
                loadHeroBanner(_state.value.mediaType)
            }
        }
    }

    private fun loadData(mediaType: MediaType) {
        _state.update { it.copy(isLoading = true, mediaType = mediaType) }

        // 1. Load Hero Banner
        loadHeroBanner(mediaType)

        // 2. Load Categories Dynamically based on what the active provider supports
        viewModelScope.launch {
            if (mediaType == MediaType.ANIME) {
                repository.getAvailableAnimeCategories().collectLatest { categories ->
                    buildAnimeSections(categories)
                }
            } else {
                repository.getAvailableMangaCategories().collectLatest { categories ->
                    buildMangaSections(categories)
                }
            }
        }
    }

    private fun loadHeroBanner(mediaType: MediaType) {
        viewModelScope.launch {
            try {
                repository.getHeroBanner(mediaType, limit = 10)
                    .catch { e -> _state.update { it.copy(error = e.message) } }
                    .collect { items ->
                        _state.update { it.copy(heroItems = items, isLoading = false) }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun buildAnimeSections(categories: List<AnimeCategory>) {
        // Define which category should be the massive vertical list at the bottom
        val verticalCategoryEnum = AnimeCategory.SEASONAL

        val horizontalSections = mutableListOf<HomeContract.MediaSection>()
        var verticalSection: HomeContract.MediaSection? = null

        categories.forEach { category ->
            // Format Enum to a Readable Title (e.g., TOP_RATED -> Top Rated)
            val title = category.name.lowercase().split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

            // Fetch PagingData and cache it to the ViewModel scope to survive config changes
            val pagingDataFlow = repository.getAnimeList(category, perPage = 20)
                .map { pagingData -> pagingData.map { it as Media } } // Cast to generic Media interface
                .cachedIn(viewModelScope)

            val section = HomeContract.MediaSection(
                title = title,
                categoryId = category.name,
                data = pagingDataFlow
            )

            if (category == verticalCategoryEnum) {
                verticalSection = section
            } else {
                horizontalSections.add(section)
            }
        }

        // If the provider doesn't support SEASONAL, fallback the vertical list to the last available category
        if (verticalSection == null && horizontalSections.isNotEmpty()) {
            verticalSection = horizontalSections.removeAt(horizontalSections.lastIndex)
        }

        _state.update {
            it.copy(
                horizontalSections = horizontalSections,
                verticalSection = verticalSection
            )
        }
    }


    private fun buildMangaSections(categories: List<MangaCategory>) {
        // Define which category should be the massive vertical list at the bottom for Manga
        val verticalCategoryEnum = MangaCategory.NEWLY_ADDED

        val horizontalSections = mutableListOf<HomeContract.MediaSection>()
        var verticalSection: HomeContract.MediaSection? = null

        categories.forEach { category ->
            val title = category.name.lowercase().split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

            val pagingDataFlow = repository.getMangaList(category, perPage = 20)
                .map { pagingData -> pagingData.map { it as Media } }
                .cachedIn(viewModelScope)

            val section = HomeContract.MediaSection(
                title = title,
                categoryId = category.name,
                data = pagingDataFlow
            )

            if (category == verticalCategoryEnum) {
                verticalSection = section
            } else {
                horizontalSections.add(section)
            }
        }

        if (verticalSection == null && horizontalSections.isNotEmpty()) {
            verticalSection = horizontalSections.removeAt(horizontalSections.lastIndex)
        }

        _state.update {
            it.copy(
                horizontalSections = horizontalSections,
                verticalSection = verticalSection
            )
        }
    }

    private fun sendEffect(effect: HomeContract.Effect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}