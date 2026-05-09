package com.ghost.zeku.presentation.screen.category

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference
import com.ghost.zeku.presentation.components.media.MediaGrid
import com.ghost.zeku.presentation.components.media.settings.DisplaySettingsPanel
import com.ghost.zeku.presentation.navigation.Destination
import com.ghost.zeku.presentation.viewmodel.category.CategoryContract
import com.ghost.zeku.presentation.viewmodel.category.CategoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: String,
    title: String,
    mediaType: MediaType,
    viewModel: CategoryViewModel = koinViewModel(),
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState(MediaDisplayPreference())


    // Dispatch Load Event
    LaunchedEffect(categoryId, mediaType, title) {
        viewModel.onEvent(
            CategoryContract.Event.LoadCategory(
                categoryId = categoryId,
                title = title,
                mediaType = mediaType
            )
        )
    }

    // Handle Effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CategoryContract.Effect.Navigate -> onNavigate(effect.destination)
                is CategoryContract.Effect.ShowMessage -> { /* Handle Snackbar */
                }
            }
        }
    }

    CategoryScreenContent(
        state = state,
        displayPreference = displayMode,
        onBack = onBack,
        onEvent = viewModel::onEvent,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryScreenContent(
    state: CategoryContract.State,
    displayPreference: MediaDisplayPreference,
    onBack: () -> Unit,
    onEvent: (CategoryContract.Event) -> Unit,
) {
    val pagingItems = state.data.collectAsLazyPagingItems()
    var showSettingsSheet by remember { mutableStateOf(false) } // 👈 State for sheet
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.title.ifEmpty { state.title },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = { // 👈 Add action button to TopAppBar
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Rounded.Tune, contentDescription = "Display Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MediaGrid(
                displayPreferences = displayPreference,
                pagingItems = pagingItems,
                onMediaAction = { onEvent(CategoryContract.Event.OnMediaAction(it)) }
            )
        }
        // 👈 Bottom Sheet implementation
        if (showSettingsSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
//                windowInsets = BottomSheetDefaults.windowInsets
            ) {
                DisplaySettingsPanel(
                    displayPreferences = displayPreference,
                    onMediaDisplayPreferenceChange = { onEvent(CategoryContract.Event.OnMediaDisplayPreferencesChange(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 32.dp) // Padding for system navigation bars
                )
            }
        }
    }


}
