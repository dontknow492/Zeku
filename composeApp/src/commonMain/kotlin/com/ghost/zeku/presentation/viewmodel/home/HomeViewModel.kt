package com.ghost.zeku.presentation.viewmodel.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.media.MediaCategory
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.navigation.Destination
import com.ghost.zeku.presentation.navigation.Destination.MediaDetail
import com.ghost.zeku.presentation.viewmodel.home.HomeContract.Effect.Navigate
import io.github.aakira.napier.Napier
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
            is HomeContract.Event.OnMediaAction -> handleMediaAction(event.action)

            is HomeContract.Event.OnViewAllClick -> {
                Napier.d("View all click: ${event.categoryId}, title: ${event.title}, mediaType: ${state.value.mediaType}")
                sendEffect(
                    Navigate(
                        Destination.ViewAllCategories(
                            categoryId = event.categoryId,
                            title = event.title,
                            type = _state.value.mediaType
                        )
                    )
                )
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
            repository.getAvailableCategories(mediaType).collectLatest { categories ->
                buildMediaSections(mediaType, categories)
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


    private fun buildMediaSections(mediaType: MediaType, categories: List<MediaCategory>) {
        // Define which category should be the massive vertical list at the bottom
        val verticalCategoryEnum = categories.last()

        val horizontalSections = mutableListOf<HomeContract.MediaSection>()
        var verticalSection: HomeContract.MediaSection? = null

        categories.forEach { category ->
            // Format Enum to a Readable Title (e.g., TOP_RATED -> Top Rated)
            val title = category.name.lowercase().split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

            // Fetch PagingData and cache it to the ViewModel scope to survive config changes
            val pagingDataFlow = repository.getMediaList(
                mediaType = mediaType,
                category = category,
                perPage = 20
            )
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


    private fun sendEffect(effect: HomeContract.Effect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }

    private fun handleMediaAction(action: MediaAction) {
        Napier.d { "MediaAction: $action" }

        viewModelScope.launch {
            when (action) {

                is MediaAction.MediaClick -> {
                    emitEffect(
                        Navigate(
                            MediaDetail(action.id, action.type)
                        )
                    )
                }

                is MediaAction.ToggleFavorite -> {
                    Napier.d { "Toggle favorite: ${action.id}" }
                    TODO("Implement adding to fav logic")
                }

                is MediaAction.AddToList -> {
                    Napier.d { "Add to list: ${action.id}" }
                    TODO("Implement saving to library logic")
                }

                is MediaAction.Share -> {
                    emitEffectMessage("Share not implemented yet")
                }

                is MediaAction.RevealNsfw -> {
                    Napier.d { "Reveal NSFW: ${action.id}" }
                }

                is MediaAction.LongClick -> {
                    Napier.d { "Long click: ${action.id}" }
                }

                is MediaAction.Custom -> {
                    Napier.w { "Unhandled custom action: ${action.key}" }
                }

                is MediaAction.GenreClick ->
                    Napier.d { "Genre clicked: ${action.genre}" }

                is MediaAction.TrailingClick -> Napier.d { "Trailing clicked: ${action.id}" }
            }
        }
    }

    private suspend fun emitEffectMessage(
        message: String,
        type: MessageType = MessageType.Info
    ) {
        emitEffect(HomeContract.Effect.ShowMessage(message, type))
    }


    private suspend fun emitEffect(effect: HomeContract.Effect) {
        Napier.v { "Effect emitted: $effect" }
        _effects.emit(effect)
    }
}