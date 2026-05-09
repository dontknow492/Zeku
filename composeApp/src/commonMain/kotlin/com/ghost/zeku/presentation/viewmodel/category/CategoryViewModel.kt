package com.ghost.zeku.presentation.viewmodel.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.media.MediaCategory
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.domain.repository.UserSettings
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.navigation.Destination
import com.ghost.zeku.presentation.navigation.Destination.MediaDetail
import com.ghost.zeku.presentation.viewmodel.category.CategoryContract.Effect.Navigate
import com.ghost.zeku.presentation.viewmodel.category.CategoryContract.Effect.ShowMessage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: MediaRepository,
    private val userSettings: UserSettings,
) : ViewModel() {

    val displayMode: StateFlow<MediaDisplayPreference> =
        userSettings.preferences.map { it.displayPreferences.category }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            MediaDisplayPreference()
        )


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
                sendEffect(Navigate(Destination.Back)) // Adjust as needed
            }

            is CategoryContract.Event.OnMediaDisplayPreferencesChange -> {
                updatePreference(event.displayPreference)
            }
        }
    }

    private fun updatePreference(
        pref: MediaDisplayPreference
    ) {
        Napier.d { "UI Mode changed to: ${pref}" }
        viewModelScope.launch {
            val result = userSettings.updatePreferences {
                it.copy(
                    displayPreferences = it.displayPreferences.copy(
                        category = pref
                    )
                )
            }
            val isError = result.isFailure
            val type = when (isError) {
                true -> MessageType.Error.UserPreference
                false -> MessageType.Success
            }
            _effects.emit(ShowMessage(result.getOrThrow().toString(), type))
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
            val categoryEnum: MediaCategory = MediaCategory.valueOf(categoryId)
            repository.getMediaList(
                mediaType = mediaType,
                category = categoryEnum,
                perPage = 20
            )
        } catch (e: IllegalArgumentException) {
            Napier.e(e) { "Invalid category ID passed: $categoryId" }
            sendEffect(ShowMessage("Invalid Category", MessageType.Error.Unknown))
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
                    sendEffect(Navigate(MediaDetail(action.id, action.type)))
                }

                is MediaAction.ToggleFavorite -> Napier.d { "Toggle favorite: ${action.id}" }
                is MediaAction.AddToList -> Napier.d { "Add to list: ${action.id}" }
                is MediaAction.Share -> sendEffect(
                    ShowMessage(
                        "Share not implemented yet",
                        MessageType.Info
                    )
                )

                is MediaAction.RevealNsfw -> Napier.d { "Reveal NSFW: ${action.id}" }
                is MediaAction.LongClick -> Napier.d { "Long click: ${action.id}" }
                is MediaAction.Custom -> Napier.w { "Unhandled custom action: ${action.key}" }
                is MediaAction.GenreClick -> Napier.d { "Genre clicked: ${action.genre}" }
                is MediaAction.TrailingClick -> Napier.d { "Trailing clicked: ${action.id}" }
            }
        }
    }

    private fun sendEffect(effect: CategoryContract.Effect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}