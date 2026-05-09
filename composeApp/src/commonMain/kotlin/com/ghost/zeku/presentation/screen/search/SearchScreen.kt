package com.ghost.zeku.presentation.screen.search

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference
import com.ghost.zeku.presentation.common.isWideScreen
import com.ghost.zeku.presentation.common.rememberPlatformConfiguration
import com.ghost.zeku.presentation.components.SearchTopBar
import com.ghost.zeku.presentation.components.media.MediaGrid
import com.ghost.zeku.presentation.components.media.settings.DisplaySettingsPanel
import com.ghost.zeku.presentation.navigation.Destination
import com.ghost.zeku.presentation.viewmodel.search.SearchContract
import com.ghost.zeku.presentation.viewmodel.search.SearchViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onNavigate: (Destination) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val displayPreferences by viewModel.displayMode.collectAsState()

    val mediaItems = viewModel.mediaSearchResults.collectAsLazyPagingItems()


    // Handle single-fire effects
    LaunchedEffect(Unit) {
        viewModel.onEvent(SearchContract.Event.Initialize())
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchContract.Effect.Navigate -> onNavigate(effect.destination)
                is SearchContract.Effect.ShowMessage -> { /* TODO: Snackbar */
                }
            }
        }
    }

    SearchScreenContent(
        state = state,
        displayPreferences = displayPreferences,
        mediaItems = mediaItems,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenContent(
    state: SearchContract.State,
    displayPreferences: MediaDisplayPreference,
    mediaItems: LazyPagingItems<Media>,
    onEvent: (SearchContract.Event) -> Unit
) {
    val isWideScreen = rememberPlatformConfiguration().isWideScreen
    // Determine the layout based on available screen width


    Scaffold(
        topBar = {
            SearchTopBar(
                query = state.query,
                onQueryChange = { onEvent(SearchContract.Event.OnQueryChange(it)) },
                onFilterClick = {
                    // Toggle sheet visibility
                    onEvent(SearchContract.Event.SetFilterSheetVisibility(!state.isFilterSheetOpen))
                },
                badgeCount = getActiveFilterCount(state),
                isFilterPanelOpen = state.isFilterSheetOpen,
                placeholder = "Search anime, manga...."
                )
        }
    ) { paddingValues ->

        if (isWideScreen) {
            // ==========================================
            // WIDE SCREEN LAYOUT: Side Panel + Grid
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Main Grid takes up remaining space
                Box(modifier = Modifier.weight(1f)) {
                    MediaGrid(
                        displayPreferences = displayPreferences,
                        pagingItems = mediaItems,
                        onMediaAction = {
                            TODO("Not Yet Implemented")
                        }
                    )
                }

                // Side Panel for Filters (Animated slide in/out)
                AnimatedVisibility(
                    visible = state.isFilterSheetOpen,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .width(480.dp)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp, // Gives it a slight pop from the background
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // A nice header for the side panel to close it
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { onEvent(SearchContract.Event.SetFilterSheetVisibility(false)) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Close Filters")
                                }
                            }
                            SearchFilterContent(
                                state = state,
                                displayPreference = displayPreferences,
                                onEvent = onEvent
                            )
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // COMPACT SCREEN LAYOUT: Grid + BottomSheet
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MediaGrid(
                    displayPreferences = displayPreferences,
                    pagingItems = mediaItems,
                    onMediaAction = {
                        TODO("Not Yet Implemented")
                    }
                )

                // Filter Bottom Sheet
                if (state.isFilterSheetOpen) {
                    ModalBottomSheet(
                        onDismissRequest = { onEvent(SearchContract.Event.SetFilterSheetVisibility(false)) },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        SearchFilterContent(state = state, displayPreference = displayPreferences, onEvent = onEvent)
                    }
                }
            }
        }

        // 👈 Bottom Sheet implementation
        if (state.isTuneSheetOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { onEvent(SearchContract.Event.SetFilterSheetVisibility(false)) },
                sheetState = sheetState,
//                windowInsets = BottomSheetDefaults.windowInsets
            ) {
                DisplaySettingsPanel(
                    displayPreferences = displayPreferences,
                    onMediaDisplayPreferenceChange = { newMode ->
                        onEvent(SearchContract.Event.OnMediaDisplayPreferencesChange(newMode))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 32.dp) // Padding for system navigation bars
                )
            }
        }
    }
}


// ========================================================================
// CAPABILITY-DRIVEN FILTER SHEET
// ========================================================================

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SearchFilterContent(
    state: SearchContract.State,
    displayPreference: MediaDisplayPreference,
    onEvent: (SearchContract.Event) -> Unit
) {
    val tabs = listOf("Filters", "Display")
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- 1. Tab Row ---
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = title, fontWeight = FontWeight.Bold) }
                    )
                }

            }

            // --- 2. Pager Content ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize() // Takes up remaining space
            ) { page ->
                when (page) {
                    0 -> {
                        // Page 0: The existing Filters UI
                        FiltersPage(state = state, onEvent = onEvent)
                    }

                    1 -> {
                        // Page 1: The Display Settings Panel
                        DisplaySettingsPanel(
                            displayPreferences = displayPreference,
                            onMediaDisplayPreferenceChange = { newMode ->
                                onEvent(SearchContract.Event.OnMediaDisplayPreferencesChange(newMode))
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 16.dp)
                                .padding(bottom = 32.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- Extracted Filters Page (Your exact existing code, just moved here) ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltersPage(
    state: SearchContract.State,
    onEvent: (SearchContract.Event) -> Unit
) {
    val scrollState = rememberScrollState()
    val caps = state.capabilities

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 32.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Header & Clear All ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filters & Discovery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Note: Assuming you have this helper function defined somewhere
            if (getActiveFilterCount(state) > 0) {
                TextButton(onClick = { onEvent(SearchContract.Event.ClearAllFilters) }) {
                    Text("Clear All")
                }
            }
        }

        // --- Core Toggles (Always Visible) ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Media Type Toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf("ANIME", "MANGA")
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = state.mediaType.name == option,
                        onClick = {
                            val type = MediaType.valueOf(option)
                            Napier.d(tag = "SearchUI") { "Switching to Type: $type" }
                            onEvent(SearchContract.Event.ChangeMediaType(type))
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                    ) {
                        Text(option)
                    }
                }
            }
        }

        // --- Capability Flags UI Rendering ---

        // 1. Sort
        if (caps.supportedSorts.isNotEmpty()) {
            FilterSection("Sort By") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    caps.supportedSorts.forEach { sort ->
                        FilterChip(
                            selected = state.selectedSort == sort,
                            onClick = { onEvent(SearchContract.Event.SelectSort(sort)) },
                            label = { Text(sort.name.replace("_", " ")) }
                        )
                    }
                }
            }
        }

        // 2. Format
        if (caps.supportedFormats.isNotEmpty()) {
            FilterSection("Format") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    caps.supportedFormats.forEach { format ->
                        FilterChip(
                            selected = state.selectedFormat == format,
                            onClick = {
                                val newFormat = if (state.selectedFormat == format) null else format
                                onEvent(SearchContract.Event.SelectFormat(newFormat))
                            },
                            label = { Text(format.name.replace("_", " ")) }
                        )
                    }
                }
            }
        }

        // 3. Status
        if (caps.supportedStatus.isNotEmpty()) {
            FilterSection("Status") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    caps.supportedStatus.forEach { status ->
                        FilterChip(
                            selected = state.selectedStatus == status,
                            onClick = {
                                val newStatus = if (state.selectedStatus == status) null else status
                                onEvent(SearchContract.Event.SelectStatus(newStatus))
                            },
                            label = { Text(status.name.replace("_", " ")) }
                        )
                    }
                }
            }
        }

        // 4. Genres
        AnimatedVisibility(visible = caps.supportsGenres, enter = expandVertically(), exit = shrinkVertically()) {
            FilterSection("Genres") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    caps.availableGenres.forEach { genre ->
                        FilterChip(
                            selected = state.selectedGenres.contains(genre),
                            onClick = { onEvent(SearchContract.Event.ToggleGenre(genre)) },
                            label = { Text(genre) }
                        )
                    }
                }
            }
        }

        // 5. Tags
        AnimatedVisibility(
            visible = caps.supportsTags,
            enter = expandVertically(),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            FilterSection("Tags") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    caps.availableTags.forEach { tag ->
                        FilterChip(
                            selected = state.selectedTags.contains(tag),
                            onClick = { onEvent(SearchContract.Event.ToggleTag(tag)) },
                            label = { Text(tag) }
                        )
                    }
                }
            }
        }
    }
}


// ========================================================================
// UI HELPERS
// ========================================================================

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}


// Helper to count active badges
private fun getActiveFilterCount(state: SearchContract.State): Int {
    var count = 0
    count += state.selectedGenres.size
    count += state.selectedTags.size
    if (state.selectedFormat != null) count++
    if (state.selectedStatus != null) count++
    if (state.selectedSeason != null) count++
    if (state.selectedYear != null) count++
    return count
}
