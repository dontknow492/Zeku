package org.ghost.zeku.ui.screen.download

import HistoryCard
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.ghost.zeku.R
import org.ghost.zeku.core.enum.HistoryStatus
import org.ghost.zeku.core.enum.MediaType
import org.ghost.zeku.core.enum.SORTING
import org.ghost.zeku.database.models.Format
import org.ghost.zeku.database.models.HistoryItem
import org.ghost.zeku.database.repository.HistoryRepository
import org.ghost.zeku.database.repository.HistoryRepository.HistorySortType
import org.ghost.zeku.ui.common.ErrorSnackBar
import org.ghost.zeku.ui.common.SelectedItemsIndicator
import org.ghost.zeku.ui.component.EmptyScreen
import org.ghost.zeku.ui.component.SelectionBottomNavigation


sealed interface HistoryPageEvent {
    data class OnMediaTypeChange(val MediaType: MediaType) : HistoryPageEvent
    data class OnQueryChange(val query: String) : HistoryPageEvent
    data class OnHistorySelectionChange(val id: Long) : HistoryPageEvent
    data class OnDeleteHistory(val id: Long) : HistoryPageEvent

    object OnSelectAll : HistoryPageEvent
    object OnClearAll : HistoryPageEvent
    object OnDeleteSelected : HistoryPageEvent
    object OnInverseSelection : HistoryPageEvent
}

data class HistoryPageUiState(
    val historyFlow: Flow<PagingData<HistoryItem>> = emptyFlow(),
    val selectedIds: Set<Long> = emptySet(),

    //filters
    val mediaType: MediaType = MediaType.AUTO,
    val query: String,
    val site: String,
    val sortType: HistorySortType,
    val sort: SORTING,
    val statusFilter: HistoryStatus,

    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(
    modifier: Modifier = Modifier,
    event: (HistoryPageEvent) -> Unit,
    state: HistoryPageUiState,
    onDownloadingClick: () -> Unit
) {
    var selectedHistoryItem by remember { mutableStateOf<HistoryItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val historyLazyPagingItem = state.historyFlow.collectAsLazyPagingItems()
    val isSelectionMenu = remember(state.selectedIds){
        state.selectedIds.isNotEmpty()
    }


    val seletectedAnnotatedString = remember(state.selectedIds) {
        buildAnnotatedString {
            append("Selected: ")
        }
    }


    LaunchedEffect(state.error) {
        if (state.error != null){
            snackbarHostState.showSnackbar(
                message = state.error,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (isSelectionMenu){
                SelectedItemsIndicator(
                    selectedIds = state.selectedIds,
                )
            }
            else{
                TopAppBar(
                    title = { Text("History", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(R.drawable.rounded_history_24),
                            contentDescription = stringResource(R.string.history_title),
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onDownloadingClick
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_downloading_24),
                                contentDescription = stringResource(R.string.downloads_title),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                )
            }

        },
        snackbarHost = {
            SnackbarHost(snackbarHostState){snackbarData ->
                ErrorSnackBar(snackbarData = snackbarData)
            }
        },
        bottomBar = {
            if (isSelectionMenu){
                SelectionBottomNavigation(
                    onDeleteClick = { event(HistoryPageEvent.OnDeleteSelected) },
                    onInvertSelectionClick = { event(HistoryPageEvent.OnInverseSelection) },
                    onClearSelectionClick = { event(HistoryPageEvent.OnClearAll) },
                    onSelectAllClick = { event(HistoryPageEvent.OnSelectAll) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            HistorySearchBar(
                Modifier.fillMaxWidth(),
                value = state.query,
                onValueChange = { event(HistoryPageEvent.OnQueryChange(it)) },
                onFilterClick = { }
            )
            HistoryFilterBar(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                selectedValue = state.mediaType,
                onValueChange = { event(HistoryPageEvent.OnMediaTypeChange(it)) }
            )

            if (historyLazyPagingItem.itemCount <= 0) {
                EmptyScreen(
                    modifier = Modifier.padding(innerPadding),
                    text = stringResource(R.string.empty_history_text),
                    model = R.drawable.broken_pie_chart,
                    contentDescription = stringResource(R.string.empty_history),
                    button = null
                )
            }
            else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(300.dp),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(count = historyLazyPagingItem.itemCount, key = { it }) { index ->
                        val historyItem = historyLazyPagingItem[index]
                        historyItem?.let {
                            val selected = state.selectedIds.contains(it.id)
                            HistoryCard(
                                historyItem = it,
                                selected = selected,
                                onClick = {
                                    if (selected || isSelectionMenu) {
                                        event(HistoryPageEvent.OnHistorySelectionChange(it.id))
                                    }else {
                                        selectedHistoryItem = it
                                    }
                                },
                                onLongPress = { event(HistoryPageEvent.OnHistorySelectionChange(it.id)) },
                                onDeleteClick = { event(HistoryPageEvent.OnDeleteHistory(it.id)) }
                            )
                        }

                    }

                }
            }


        }
    }

    if (selectedHistoryItem != null){
        // TODO: Implement ui
    }
}

@Composable
private fun HistorySearchBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onFilterClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Add padding
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            value = value, // <-- FIX: Use the parameter
            onValueChange = onValueChange, // <-- FIX: Use the parameter
            placeholder = { Text("Search") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear),
                        )
                    }
                }
            },
            // Style improvements
            singleLine = true,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent, // Remove underline
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
        IconButton(onClick = onFilterClick ) {
            Icon(
                painter = painterResource(R.drawable.rounded_filter_list_24),
                contentDescription = stringResource(R.string.filter),
                modifier = Modifier.size(48.dp)
            )
        }
    }

}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryFilterBar(
    modifier: Modifier = Modifier,
    selectedValue: MediaType,
    onValueChange: (MediaType) -> Unit,
) {
    data class MediaTypeOption(
        val type: MediaType,
        val label: String,
        val iconRes: Int
    )

    val options = listOf(
        MediaTypeOption(MediaType.AUTO, "All", R.drawable.rounded_background_dot_small_24),
        MediaTypeOption(MediaType.AUDIO, "Audio", R.drawable.rounded_music_note_24),
        MediaTypeOption(MediaType.VIDEO, "Video", R.drawable.rounded_movie_24),
        MediaTypeOption(MediaType.COMMAND, "Command", R.drawable.rounded_terminal_24)
    )
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between chips
    ) {
        // Create a FilterChip for each option in the list.
        options.forEach { option ->
            val isSelected = selectedValue == option.type
            FilterChip(
                selected = isSelected,
                onClick = { onValueChange(option.type) },
                label = { Text(option.label) },
                // Show a checkmark when selected
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    // Show the media type icon when not selected
                    {
                        Icon(
                            painter = painterResource(id = option.iconRes),
                            contentDescription = option.label,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun HistoryDetailBottomSheet(
    modifier: Modifier = Modifier,
    item: HistoryItem,
    event: (HistoryPageEvent) -> Unit,
    onDismiss: () -> Unit
) {
    
}









@Preview()
@Composable
fun HistoryPagePreview() {
    val sampleItem = HistoryItem(
        id = 1,
        url = "https://example.com",
        title = "A Great Video About Jetpack Compose Layouts and Modifiers",
        author = "Awesome Android Dev",
        duration = "12:34",
        thumb = "https://picsum.photos/seed/picsum/400/300",
        type = MediaType.VIDEO,
        time = System.currentTimeMillis() - 3600000 * 5, // 5 hours ago
        filesize = 1024L * 1024 * 58 + 300,
        downloadPath = listOf("downloads", "videos", "sample.mp4"),
        downloadId = 123456789L,
        website = "Youtube",
        format = Format()
    )
    val data: Flow<PagingData<HistoryItem>> = flowOf(PagingData.from(
        listOf(
            sampleItem,
            sampleItem.copy(id = 2),
            sampleItem.copy(id = 3),
            sampleItem.copy(id = 4),
            sampleItem.copy(id = 5),
            sampleItem.copy(id = 6),
            sampleItem.copy(id = 7),
            sampleItem.copy(id = 8)
        )
    ))
    HistoryPage(
        event = {},
        state = HistoryPageUiState(
            historyFlow = data,
            selectedIds = setOf(1L, 2L),
            mediaType = MediaType.AUTO,
            query = "A Great",
            site = "",
            sortType = HistoryRepository.HistorySortType.DATE,
            sort = SORTING.DESC,
            statusFilter = HistoryStatus.ALL
        ),
        onDownloadingClick = {}
    )
}