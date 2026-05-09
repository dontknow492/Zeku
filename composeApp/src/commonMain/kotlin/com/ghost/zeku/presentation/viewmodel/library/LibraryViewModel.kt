package com.ghost.zeku.presentation.viewmodel.library

// LibraryViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ghost.zeku.data.local.room.MediaQueryFactory
import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity
import com.ghost.zeku.data.local.room.view.MediaLibraryView
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.model.filter.GroupOption
import com.ghost.zeku.domain.model.filter.MediaFilterState
import com.ghost.zeku.domain.model.media.MediaFormat
import com.ghost.zeku.domain.model.media.MediaReleaseStatus
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference
import com.ghost.zeku.domain.repository.CategoryRepository
import com.ghost.zeku.domain.repository.LibraryRepository
import com.ghost.zeku.domain.repository.UserSettings
//import com.ghost.zeku.presentation.viewmodel.library.LibraryContract.*

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class LibraryViewModel(
    private val userSettings: UserSettings,
    private val libraryRepository: LibraryRepository,
    private val categoryRepository: CategoryRepository // Needed to fetch categories for tabs
) : ViewModel() {


    private data class LocalUiState(
        val tabs: List<LibraryContract.LibraryTab> = emptyList(),
        val selectedTabId: String = "ALL",
        val filterSheetVisible: Boolean = false
    )


    private val _localUiState = MutableStateFlow(LocalUiState())

    private val tag = "LibraryVM"

    val state: StateFlow<LibraryContract.State> = combine(
        _localUiState,
        userSettings.preferences.map { it.libraryPreferences }
    ) { local, prefs ->
        LibraryContract.State(
            isLoading = false,
            filterState = prefs.filter,
            displayPreference = prefs.displayPreferences,
            tabs = local.tabs,
            selectedTabId = local.selectedTabId,
            filterSheetVisible = local.filterSheetVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryContract.State()
    )

    private val _effect = MutableSharedFlow<LibraryContract.Effect>()
    val effect = _effect.asSharedFlow()

    // Cache of Pagers so we don't recreate them every time the user swipes between tabs
    private val pagerCache = mutableMapOf<String, Flow<PagingData<MediaLibraryView>>>()

    init {
        Napier.d("ViewModel initialized", tag = tag)
        observeGroupingChanges()
    }

    fun onEvent(event: LibraryContract.Event) {
        when (event) {
            // --- PERSISTENT SETTINGS UPDATES ---
            is LibraryContract.Event.UpdateFilter -> {
                viewModelScope.launch {
                    // Update the actual DataStore/Settings!
                    // Assuming you have an update method in UserSettings
                    updateLibraryFilter(event.newFilter)
                    clearPagerCache() // Filters changed, data must reload
                }
            }

            is LibraryContract.Event.UpdateDisplayPref -> {
                viewModelScope.launch {
                    updateLibraryDisplayPref(event.newPref)
                }
            }

            is LibraryContract.Event.SearchQueryChanged -> {
                viewModelScope.launch {
                    val currentFilter = state.value.filterState
                    updateLibraryFilter(currentFilter.copy(searchQuery = event.query))
                    clearPagerCache()
                }
            }

            is LibraryContract.Event.ClearFilters -> {
                viewModelScope.launch {
                    updateLibraryFilter(MediaFilterState())
                    clearPagerCache()
                }
            }

            // --- LOCAL UI UPDATES ---
            is LibraryContract.Event.OnTabSelected -> {
                _localUiState.update { it.copy(selectedTabId = event.tabId) }
            }

            is LibraryContract.Event.ToggleFilterSheet -> {
                _localUiState.update { it.copy(filterSheetVisible = event.status) }
            }

            // --- ACTIONS ---
            is LibraryContract.Event.ToggleFavorite -> toggleFavorite(event.mediaId, event.mediaType)
            LibraryContract.Event.OnFilterClicked -> {
                _localUiState.update { it.copy(filterSheetVisible = !it.filterSheetVisible) }
            }
        }
    }


    /**
     * Observes grouping logic to automatically generate Tabs for the UI.
     */
    private fun observeGroupingChanges() {
        viewModelScope.launch {
            combine(
                userSettings.preferences.map { it.libraryPreferences.filter.groupBy }.distinctUntilChanged(),
                categoryRepository.observeAll()
            ) { groupOption, categories ->
                // Pass BOTH pieces of data into our tab generator
                generateTabsForGroup(groupOption, categories)
            }
                .distinctUntilChanged()
                .collect { newTabs ->
                    _localUiState.update { currentState ->
                        // Try to keep the user on their current tab if it still exists,
                        // otherwise fall back to the first tab.

                        val safeTabId = if (newTabs.any { it.id == currentState.selectedTabId }) {
                            currentState.selectedTabId
                        } else {
                            newTabs.firstOrNull()?.id ?: "ALL"
                        }

                        currentState.copy(
                            tabs = newTabs,
                            selectedTabId = safeTabId
                        )
                    }
                    clearPagerCache()
                }
        }
    }

    private fun generateTabsForGroup(
        groupOption: GroupOption,
        categories: List<LibraryCategoryEntity> // Passed in from the combine block
    ): List<LibraryContract.LibraryTab> {

        return when (groupOption) {
            GroupOption.NONE -> listOf(LibraryContract.LibraryTab("ALL", "All Library"))
            GroupOption.TYPE -> MediaType.entries.map { LibraryContract.LibraryTab(it.name, it.name) }
            GroupOption.SOURCE -> ProviderType.entries.map { LibraryContract.LibraryTab(it.name, it.name) }

            GroupOption.CATEGORY -> {
                // Check if the DB is still empty
                if (categories.isEmpty()) {
                    listOf(LibraryContract.LibraryTab("ALL", "Loading/No Categories..."))
                } else {
                    categories.map { category ->
                        LibraryContract.LibraryTab(
                            id = category.id.toString(), // Safest to use DB ID as the tab ID
                            title = category.name,
                            categoryId = category.id
                        )
                    }
                }
            }

            GroupOption.STATUS -> MediaReleaseStatus.entries.map { LibraryContract.LibraryTab(it.name, it.name) }
            GroupOption.FORMAT -> MediaFormat.entries.map { LibraryContract.LibraryTab(it.name, it.name) }
        }
    }

    /**
     * This is called by your UI (HorizontalPager content).
     * It dynamically generates or retrieves a cached Paging Flow for a specific tab.
     */
    fun getPagingFlowForTab(tab: LibraryContract.LibraryTab): Flow<PagingData<MediaLibraryView>> {
        // Return cached flow if it exists
        pagerCache[tab.id]?.let { return it }

        Napier.d("Creating new Pager for tab: ${tab.id}", tag = tag)

        // 1. Get base filter state
        val baseFilter = userSettings.preferences.value.libraryPreferences.filter

        // 2. Modify the filter for THIS specific tab based on GroupOption
        val tabSpecificFilter = when (baseFilter.groupBy) {
            GroupOption.TYPE -> baseFilter.copy(mediaTypes = listOf(MediaType.valueOf(tab.id)))
            GroupOption.STATUS -> baseFilter.copy(statuses = listOf(MediaReleaseStatus.valueOf(tab.id)))
            GroupOption.NONE -> baseFilter
            GroupOption.SOURCE -> baseFilter.copy(providers = listOf(ProviderType.valueOf(tab.id)))
            GroupOption.CATEGORY -> {
                baseFilter
            }

            GroupOption.FORMAT -> baseFilter

        }

        // 3. Build the Raw Query for this tab
        val rawQuery = MediaQueryFactory.buildQuery(tabSpecificFilter)

        // 4. Create the Pager
        val pagerFlow = Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true),
            pagingSourceFactory = { libraryRepository.getPagingSource(rawQuery) }
        ).flow.cachedIn(viewModelScope)

        // Cache and return
        pagerCache[tab.id] = pagerFlow
        return pagerFlow
    }


    private fun updateLibraryFilter(filter: MediaFilterState) {
        viewModelScope.launch {
            userSettings.updatePreferences {
                it.copy(
                    libraryPreferences = it.libraryPreferences.copy(
                        filter = filter,
                    )
                )
            }
        }
    }

    private fun updateLibraryDisplayPref(pref: MediaDisplayPreference) {
        viewModelScope.launch {
            userSettings.updatePreferences {
                it.copy(
                    libraryPreferences = it.libraryPreferences.copy(
                        displayPreferences = pref
                    )
                )
            }
        }
    }


    private fun toggleFavorite(mediaId: Int, mediaType: MediaType) {
        viewModelScope.launch {
            try {
                libraryRepository.toggleFavorite(mediaId, mediaType)
                Napier.d("Toggled favorite for $mediaType ID: $mediaId", tag = tag)
            } catch (e: Exception) {
                Napier.e("Failed to toggle favorite", e, tag)
                sendEffect(LibraryContract.Effect.ShowSnackbar("Failed to update favorite"))
            }
        }
    }

    private fun clearPagerCache() {
        Napier.d("Clearing Pager cache", tag = tag)
        pagerCache.clear()
    }

    private fun sendEffect(effect: LibraryContract.Effect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}