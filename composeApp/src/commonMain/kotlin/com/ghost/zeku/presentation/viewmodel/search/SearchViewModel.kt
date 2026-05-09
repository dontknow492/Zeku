package com.ghost.zeku.presentation.viewmodel.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.filter.search.MediaSearchFilter
import com.ghost.zeku.domain.model.filter.search.SearchCapabilities
import com.ghost.zeku.domain.model.filter.search.SearchSort
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.domain.repository.UserSettings
import com.ghost.zeku.presentation.navigation.Destination
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel(
    private val repository: MediaRepository,
    private val userSettings: UserSettings,
) : ViewModel() {

    val displayMode = userSettings.preferences.map { it.displayPreferences.category }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        MediaDisplayPreference()
    )

    private val _state = MutableStateFlow(SearchContract.State())
    val state: StateFlow<SearchContract.State> = _state.asStateFlow()

    private val _effect = Channel<SearchContract.Effect>()
    val effect = _effect.receiveAsFlow()

    // ----------------------------------------------------------------------
    // PAGING FLOWS (Reacts automatically to State changes!)
    // ----------------------------------------------------------------------

    /**
     * This is where the magic happens.
     * It observes the state, waits 500ms for the user to stop tweaking filters/typing,
     * checks if we are searching Anime, and then triggers the Paging request.
     */

    val mediaSearchResults = _state
        .debounce(500.milliseconds)
        .filter { !it.isInitializing }
        .distinctUntilChanged { old, new ->
            old.query == new.query && old.activeProvider == new.activeProvider &&
                    old.selectedGenres == new.selectedGenres && old.selectedFormat == new.selectedFormat &&
                    old.selectedSort == new.selectedSort
        }.flatMapLatest { currentState ->
            val filter = MediaSearchFilter(
                mediaType = currentState.mediaType,
                includedGenres = currentState.selectedGenres,
                includedTags = currentState.selectedTags,
                year = currentState.selectedYear,
                season = currentState.selectedSeason,
                format = currentState.selectedFormat,
                status = currentState.selectedStatus,
                sort = currentState.selectedSort,
            )
            repository.searchMedia(
                query = currentState.query,
                filter = filter,
            )
        }.cachedIn(viewModelScope)


    // ----------------------------------------------------------------------
    // EVENT HANDLER
    // ----------------------------------------------------------------------

    fun onEvent(event: SearchContract.Event) {
        when (event) {
            is SearchContract.Event.Initialize -> {
                if (_state.value.isInitializing) {
                    _state.update { it.copy(mediaType = event.defaultType) }
                    fetchCapabilities()
                }
            }

            is SearchContract.Event.ChangeProvider -> {
                Napier.i(tag = "SearchVM") { "Switching provider to ${event.provider}" }
                _state.update { it.copy(activeProvider = event.provider) }
                fetchCapabilities() // Fetch new capabilities and sanitize filters!
            }

            is SearchContract.Event.ChangeMediaType -> {
                Napier.i(tag = "SearchVM") { "Switching media type to ${event.type}" }
                _state.update { it.copy(mediaType = event.type) }
                fetchCapabilities()
            }

            is SearchContract.Event.OnQueryChange -> {
                _state.update { it.copy(query = event.query) }
            }

            // --- Filters ---
            is SearchContract.Event.ToggleGenre -> toggleListItem(event.genre, isGenre = true)
            is SearchContract.Event.ToggleTag -> toggleListItem(event.tag, isGenre = false)
            is SearchContract.Event.SelectFormat -> _state.update { it.copy(selectedFormat = event.format) }
            is SearchContract.Event.SelectSeason -> _state.update { it.copy(selectedSeason = event.season) }
            is SearchContract.Event.SelectYear -> _state.update { it.copy(selectedYear = event.year) }
            is SearchContract.Event.SelectStatus -> _state.update { it.copy(selectedStatus = event.status) }
            is SearchContract.Event.SelectSort -> _state.update { it.copy(selectedSort = event.sort) }

            is SearchContract.Event.ClearAllFilters -> {
                _state.update {
                    it.copy(
                        selectedGenres = emptyList(), selectedTags = emptyList(),
                        selectedYear = null, selectedSeason = null,
                        selectedFormat = null, selectedStatus = null,
                        selectedSort = it.capabilities.supportedSorts.firstOrNull() ?: SearchSort.TRENDING_DESC
                    )
                }
            }

            // --- UI & Nav ---
            is SearchContract.Event.SetFilterSheetVisibility -> {
                _state.update { it.copy(isFilterSheetOpen = event.isOpen) }
            }

            is SearchContract.Event.OnMediaClick -> {
                viewModelScope.launch {
                    _effect.send(SearchContract.Effect.Navigate(Destination.MediaDetail(event.mediaId, event.type)))
                }
            }

            is SearchContract.Event.SetTuneSheetVisibility -> {
                _state.update { it.copy(isTuneSheetOpen = event.isOpen) }
            }

            is SearchContract.Event.OnMediaDisplayPreferencesChange -> {
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
            _effect.send(SearchContract.Effect.ShowMessage(result.getOrThrow().toString(), type))
        }
    }

    // ----------------------------------------------------------------------
    // CAPABILITY LOGIC
    // ----------------------------------------------------------------------

    private fun fetchCapabilities() {
        viewModelScope.launch {
            _state.update { it.copy(isInitializing = true) }

            val currentState = _state.value
            val capabilities = repository.getSearchCapabilities(
                provider = currentState.activeProvider,
                mediaType = currentState.mediaType,
            )

            _state.update { state ->
                // IMPORTANT: We must sanitize the current selections against the NEW capabilities!
                val sanitizedState = sanitizeFilters(state, capabilities)

                sanitizedState.copy(
                    capabilities = capabilities,
                    isInitializing = false
                )
            }
            Napier.d(tag = "SearchVM") { "Capabilities updated for ${currentState.activeProvider}" }
        }
    }

    /**
     * Protects the app from crashing.
     * If you switch from AniList to MAL, MAL doesn't support tags.
     * This function automatically clears `selectedTags` so they don't get sent to the MAL API.
     */
    private fun sanitizeFilters(state: SearchContract.State, caps: SearchCapabilities): SearchContract.State {
        return state.copy(
            selectedGenres = if (caps.supportsGenres) state.selectedGenres.filter { caps.availableGenres.contains(it) } else emptyList(),
            selectedTags = if (caps.supportsTags) state.selectedTags.filter { caps.availableTags.contains(it) } else emptyList(),
            selectedYear = if (caps.supportsYear) state.selectedYear else null,
            selectedSeason = if (caps.supportsSeason) state.selectedSeason else null,

            // Revert formats/statuses to null if the new provider doesn't support the current selection
            selectedFormat = if (caps.supportedFormats.contains(state.selectedFormat)) state.selectedFormat else null,
            selectedStatus = if (caps.supportedStatus.contains(state.selectedStatus)) state.selectedStatus else null,

            // Always ensure a valid sort is selected
            selectedSort = if (caps.supportedSorts.contains(state.selectedSort)) state.selectedSort
            else caps.supportedSorts.firstOrNull() ?: SearchSort.TRENDING_DESC
        )
    }

    private fun toggleListItem(item: String, isGenre: Boolean) {
        _state.update { state ->
            val currentList = if (isGenre) state.selectedGenres else state.selectedTags
            val newList = if (currentList.contains(item)) currentList - item else currentList + item

            if (isGenre) state.copy(selectedGenres = newList) else state.copy(selectedTags = newList)
        }
    }
}