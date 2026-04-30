package com.ghost.zeku.presentation.viewmodel.category

import androidx.lifecycle.ViewModel
import com.ghost.zeku.domain.repository.MediaRepository
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.viewmodel.detail.Destination
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    // Initialized with empty defaults until LoadCategory is called
    private val _state = MutableStateFlow(CategoryContract.State())
    val state: StateFlow<CategoryContract.State> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CategoryContract.Effect>()
    val effects: SharedFlow<CategoryContract.Effect> = _effects.asSharedFlow()

    fun onEvent(event: CategoryContract.Event) {
        when (event) {
            is CategoryContract.Event.LoadCategory -> {
                // Prevent reloading if we rotate the screen and the data is already there
                if (_state.value.categoryId != event.categoryId || _state.value.type != event.mediaType) {
                    loadData(event.categoryId, event.title, event.mediaType)
                }
            }

            is CategoryContract.Event.OnMediaAction -> handleMediaAction(event.action)
            is CategoryContract.Event.OnRefresh -> {
                Napier.d { "Refresh requested for category: ${_state.value.categoryId}" }
            }

            is CategoryContract.Event.OnBack -> {
                sendEffect(CategoryContract.Effect.Navigate(Destination.Back)) // Adjust as needed
            }
        }
    }

    private fun loadData(categoryId: String, title: String, mediaType: MediaType) {
        Napier.d { "Loading Category -> ID: $categoryId, Type: $mediaType, Title: $title" }

        // 1. Update state with the incoming UI arguments
        _state.update {
            it.copy(
                categoryId = categoryId,
                title = title,
                type = mediaType,
                isLoading = true
            )
        }

        // 2. Fetch PagingData based on Media Type
        val pagingDataFlow = try {
            if (mediaType == MediaType.ANIME) {
                val categoryEnum = AnimeCategory.valueOf(categoryId)
                Napier.e { "Category: $categoryEnum, ID: $categoryId" }
                repository.getAnimeList(categoryEnum, perPage = 20)
                    .map { pagingData -> pagingData.map { it as Media } }
            } else {
                val categoryEnum = MangaCategory.valueOf(categoryId)
                repository.getMangaList(categoryEnum, perPage = 20)
                    .map { pagingData -> pagingData.map { it as Media } }
            }
        } catch (e: IllegalArgumentException) {
            Napier.e(e) { "Invalid category ID passed: $categoryId" }
            sendEffect(CategoryContract.Effect.ShowMessage("Invalid Category", MessageType.Error.Unknown))
            _state.update { it.copy(error = "Invalid Category", isLoading = false) }
            emptyFlow()
        }
            .cachedIn(viewModelScope) // Cache paging data in ViewModel scope

        // 3. Update state with the Flow
        _state.update {
            it.copy(
                data = pagingDataFlow,
                isLoading = false
            )
        }
    }

    private fun handleMediaAction(action: MediaAction) {
        Napier.d { "Category MediaAction: $action" }
        viewModelScope.launch {
            when (action) {
                is MediaAction.MediaClick -> {
                    sendEffect(CategoryContract.Effect.Navigate(Destination.MediaDetail(action.id, action.type)))
                }

                is MediaAction.ToggleFavorite -> Napier.d { "Toggle favorite: ${action.id}" }
                is MediaAction.AddToList -> Napier.d { "Add to list: ${action.id}" }
                is MediaAction.Share -> sendEffect(
                    CategoryContract.Effect.ShowMessage(
                        "Share not implemented yet",
                        MessageType.Info
                    )
                )

                is MediaAction.RevealNsfw -> Napier.d { "Reveal NSFW: ${action.id}" }
                is MediaAction.LongClick -> Napier.d { "Long click: ${action.id}" }
                is MediaAction.Custom -> Napier.w { "Unhandled custom action: ${action.key}" }
                is MediaAction.GenreClick -> Napier.d { "Genre clicked: ${action.genre}" }
            }
        }
    }

    private fun sendEffect(effect: CategoryContract.Effect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}