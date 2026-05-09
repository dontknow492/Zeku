package com.ghost.zeku.presentation.screen.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.map
import com.ghost.zeku.data.local.room.toDomain
import com.ghost.zeku.domain.model.filter.GroupOption
import com.ghost.zeku.domain.model.filter.MediaFilterState
import com.ghost.zeku.domain.model.filter.SortDirection
import com.ghost.zeku.domain.model.filter.SortOption
import com.ghost.zeku.domain.model.media.MediaReleaseStatus
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference
import com.ghost.zeku.presentation.components.SearchTopBar
import com.ghost.zeku.presentation.components.media.MediaGrid
import com.ghost.zeku.presentation.components.media.settings.DisplaySettingsPanel
import com.ghost.zeku.presentation.components.section.EmptyMediaState
import com.ghost.zeku.presentation.viewmodel.library.LibraryContract
import com.ghost.zeku.presentation.viewmodel.library.LibraryViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                is LibraryContract.Effect.NavigateToDetail -> {
                    // TODO: Navigation Logic
                }

                LibraryContract.Effect.OpenFilterBottomSheet -> {
                    // TODO: Trigger ModalBottomSheet
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SearchTopBar(
                query = state.filterState.searchQuery,
                placeholder = "Search your library...",
                onQueryChange = { viewModel.onEvent(LibraryContract.Event.SearchQueryChanged(it)) },
                onFilterClick = { viewModel.onEvent(LibraryContract.Event.OnFilterClicked) },
                badgeCount = 0,
                isFilterPanelOpen = state.filterSheetVisible
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Open search/add screen */ },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Media")
            }
        }
    ) { padding ->
        LibraryScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = state,
            onEvent = viewModel::onEvent,
            viewModel = viewModel
        )
        if (state.filterSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(LibraryContract.Event.ToggleFilterSheet(false)) }
            ) {
                LibraryBottomSheet(
                    filter = state.filterState,
                    displayPreferences = state.displayPreference,
                    onFilterChange = { viewModel.onEvent(LibraryContract.Event.UpdateFilter(it)) },
                    onDisplayPreferenceChange = { viewModel.onEvent(LibraryContract.Event.UpdateDisplayPref(it)) },
                    modifier = Modifier
                )
            }
        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryScreenContent(
    modifier: Modifier = Modifier,
    state: LibraryContract.State,
    onEvent: (LibraryContract.Event) -> Unit,
    viewModel: LibraryViewModel // Passed to fetch the specific Paging flow per tab
) {
    Column(modifier = modifier.fillMaxSize()) {
        // 2. Dynamic Tabs & Pager
        if (state.tabs.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { state.tabs.size })
            val coroutineScope = rememberCoroutineScope()

            // Sync ViewModel selected tab with manual user swipes
            LaunchedEffect(pagerState.currentPage) {
                val currentTab = state.tabs[pagerState.currentPage]
                if (state.selectedTabId != currentTab.id) {
                    onEvent(LibraryContract.Event.OnTabSelected(currentTab.id))
                }
            }

            // Sync Pager position if ViewModel changes tab programmatically
            LaunchedEffect(state.selectedTabId) {
                val index = state.tabs.indexOfFirst { it.id == state.selectedTabId }
                if (index != -1 && pagerState.currentPage != index) {
                    pagerState.animateScrollToPage(index)
                }
            }

            SecondaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            )
            {
                state.tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp) // Slight peek effect
            ) { pageIndex ->
                val currentTab = state.tabs[pageIndex]

                // Fetch the specific PagingData Flow for this tab and collect it
                val pagingItems = remember(currentTab.id) {
                    viewModel.getPagingFlowForTab(currentTab).map { pagingData ->
                        // This inner .map is the PagingData map!
                        pagingData.map { mediaLibraryView ->
                            // Call your extension function here.
                            // Depending on your mapper, it might be:
                            // mediaLibraryView.toDomain()
                            // OR
                            mediaLibraryView.media.toDomain()
                        }
                    }
                }.collectAsLazyPagingItems()

                MediaGrid(
                    displayPreferences = state.displayPreference,
                    pagingItems = pagingItems,
                    onMediaAction = { TODO() },
                    modifier = Modifier,
                )

            }
        } else {
            // Empty State (No tabs generated)
            EmptyMediaState(
                modifier = Modifier.fillMaxSize(),
                title = "Empty Library",
                description = "No library available, try to add some media",
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryBottomSheet(
    filter: MediaFilterState,
    displayPreferences: MediaDisplayPreference,
    onFilterChange: (MediaFilterState) -> Unit,
    onDisplayPreferenceChange: (MediaDisplayPreference) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Filter", "Sort & Group", "Display")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f) // Take up 85% of screen height max
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Bottom Sheet Handle & Title
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }

        Text(
            text = "Library Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = { HorizontalDivider() },
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> FilterPanel(
                    filter = filter,
                    onFilterChange = onFilterChange,
                    modifier = Modifier.fillMaxSize()
                )

                1 -> SortGroupPanel(
                    filter = filter,
                    onFilterChange = onFilterChange,
                    modifier = Modifier.fillMaxSize()
                )

                2 -> DisplaySettingsPanel(
                    displayPreferences = displayPreferences,
                    onMediaDisplayPreferenceChange = onDisplayPreferenceChange,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FilterPanel(
    filter: MediaFilterState,
    onFilterChange: (MediaFilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Media Type Section ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Media Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MediaType.entries.forEach { type ->
                    val isSelected = filter.mediaTypes.contains(type)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newTypes = if (isSelected) filter.mediaTypes - type else filter.mediaTypes + type
                            onFilterChange(filter.copy(mediaTypes = newTypes))
                        },
                        label = { Text(type.name) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        // --- Status Section ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Release Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MediaReleaseStatus.entries.forEach { status ->
                    val isSelected = filter.statuses.contains(status)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newStatuses = if (isSelected) filter.statuses - status else filter.statuses + status
                            onFilterChange(filter.copy(statuses = newStatuses))
                        },
                        label = { Text(status.name.replace("_", " ")) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        // TODO: Future Implementation -> Genres and Tags Expandable Lists
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SortGroupPanel(
    filter: MediaFilterState,
    onFilterChange: (MediaFilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Grouping Section ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Group Library By", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GroupOption.entries.forEach { option ->
                    val isSelected = filter.groupBy == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter.copy(groupBy = option)) },
                        label = { Text(option.name) }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // --- Sorting Section ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sort Library By", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            // Direction Toggle (Ascending / Descending)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = filter.sortDirection == SortDirection.ASCENDING,
                    onClick = { onFilterChange(filter.copy(sortDirection = SortDirection.ASCENDING)) },
                    shape = RoundedCornerShape(topStart = 50f, bottomStart = 50f)
                ) { Text("Ascending (A-Z)") }

                SegmentedButton(
                    selected = filter.sortDirection == SortDirection.DESCENDING,
                    onClick = { onFilterChange(filter.copy(sortDirection = SortDirection.DESCENDING)) },
                    shape = RoundedCornerShape(topEnd = 50f, bottomEnd = 50f)
                ) { Text("Descending (Z-A)") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sort Options
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.entries.forEach { option ->
                    val isSelected = filter.sortBy == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter.copy(sortBy = option)) },
                        label = { Text(option.name.replace("_", " ")) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}
