package com.ghost.zeku.presentation.screen.category

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.poster.MediaPosterCard
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.toPosterUiData
import com.ghost.zeku.presentation.viewmodel.category.CategoryContract
import com.ghost.zeku.presentation.viewmodel.category.CategoryViewModel
import com.ghost.zeku.presentation.viewmodel.detail.Destination
import com.ghost.zeku.utils.shimmerEffect
import kotlinx.coroutines.flow.Flow
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
        config = CategoryUiConfig(),
        onBack = onBack,
        onEvent = viewModel::onEvent,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryScreenContent(
    state: CategoryContract.State,
    config: CategoryUiConfig,
    onBack: () -> Unit,
    onEvent: (CategoryContract.Event) -> Unit,
) {
    val pagingItems = state.data.collectAsLazyPagingItems()
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
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(
                targetState = pagingItems.loadState.refresh,
                label = "category_loading_crossfade"
            ) { loadState ->
                when (loadState) {
                    is LoadState.Loading -> {
                        // Show Shimmer Grid
                        LoadingGrid()
                    }

                    is LoadState.Error -> {
                        // Show Error State
                        ErrorState(
                            message = loadState.error.localizedMessage ?: "Unknown Error",
                            onRetry = { pagingItems.retry() }
                        )
                    }

                    is LoadState.NotLoading -> {
                        if (pagingItems.itemCount == 0 && state.data != kotlinx.coroutines.flow.emptyFlow<Any>()) {
                            EmptyState()
                        } else {
                            // Main Content Grid
                            LazyVerticalGrid(
                                columns = config.layout,
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    count = pagingItems.itemCount,
                                    key = pagingItems.itemKey { it.id }
                                ) { index ->
                                    val item = pagingItems[index]
                                    if (item != null) {
                                        // Convert Domain Model to UI Model if needed
                                        // Assuming you have a mapping function or your PosterCard takes the domain model directly.
                                        // Here we map it assuming a toUiData() exists.
                                        MediaPosterCard(
                                            data = item.toPosterUiData(),
                                            config = config.posterConfig,
                                            onAction = { onEvent(CategoryContract.Event.OnMediaAction(it)) }
                                        )
                                    }
                                }

                                // Handle Append Loading (Pagination loading spinner at bottom)
                                if (pagingItems.loadState.append is LoadState.Loading) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }

                                // Handle Append Error
                                if (pagingItems.loadState.append is LoadState.Error) {
                                    item {
                                        TextButton(
                                            onClick = { pagingItems.retry() },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Retry Loading More")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingGrid() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(20) {
            Box(
                modifier = Modifier
                    .aspectRatio(2f / 3f)
                    .shimmerEffect() // Assuming this is the modifier we built earlier
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Failed to load category.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No items found in this category.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}