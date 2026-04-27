package com.ghost.zeku.presentation.components.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Any> PagedMediaSection(
    title: String,
    items: LazyPagingItems<T>,
    config: MediaSectionConfig = MediaSectionConfig(),
    modifier: Modifier = Modifier,
    key: ((item: T) -> Any)? = null,
    onViewAllClick: (() -> Unit)? = null,
    itemContent: @Composable (item: T, modifier: Modifier) -> Unit
) {
    var isVisible by remember { mutableStateOf(!config.enableEntranceAnimation) }

    LaunchedEffect(Unit) {
        if (config.enableEntranceAnimation) isVisible = true
    }

    // Determine loading and empty states natively through Paging 3
    val isRefreshing = items.loadState.refresh is LoadState.Loading
    val isListEmpty = items.itemCount == 0 && !isRefreshing

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessVeryLow)) +
                slideInVertically { 50 }
    ) {
        Column(modifier = modifier.fillMaxWidth()) {

            // Header
            if (config.showHeader) {
                SectionHeader(
                    title = title,
                    onViewAllClick = if (config.showViewAll) onViewAllClick else null,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            when {
                // Initial load
                isRefreshing && items.itemCount == 0 -> {
                    SectionLoadingShimmer(config)
                }

                // Empty state
                isListEmpty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(config.emptyHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No media found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    when (val layout = config.layout) {

                        is SectionLayout.HorizontalRow -> {
                            LazyRow(
                                contentPadding = layout.contentPadding,
                                horizontalArrangement = Arrangement.spacedBy(layout.itemSpacing)
                            ) {
                                items(
                                    count = items.itemCount,
                                    key = if (key != null) items.itemKey(key) else null
                                ) { index ->
                                    // Paging items can be null if placeholders are enabled
                                    items[index]?.let { item ->
                                        itemContent(item, Modifier.animateItem())
                                    }
                                }
                            }
                        }

                        is SectionLayout.VerticalList -> {
                            // Switched to LazyColumn: Paging standard Columns triggers all page fetches
                            LazyColumn(
                                modifier = Modifier.padding(layout.contentPadding),
                                verticalArrangement = Arrangement.spacedBy(layout.itemSpacing)
                            ) {
                                items(
                                    count = items.itemCount,
                                    key = if (key != null) items.itemKey(key) else null
                                ) { index ->
                                    items[index]?.let { item ->
                                        itemContent(
                                            item,
                                            Modifier
                                        ) // Modifier.animateItem() not supported inside LazyColumn globally yet in all Compose versions
                                    }
                                }
                            }
                        }

                        is SectionLayout.Grid -> {
                            val columns = when (layout.columns) {
                                is GridType.Fixed -> GridCells.Fixed(layout.columns.count)
                                is GridType.Adaptive -> GridCells.Adaptive(layout.columns.minSize)
                            }

                            LazyVerticalGrid(
                                columns = columns,
                                contentPadding = layout.contentPadding,
                                horizontalArrangement = Arrangement.spacedBy(layout.horizontalSpacing),
                                verticalArrangement = Arrangement.spacedBy(layout.verticalSpacing)
                            ) {
                                items(
                                    count = items.itemCount,
                                    key = if (key != null) items.itemKey(key) else null
                                ) { index ->
                                    items[index]?.let { item ->
                                        itemContent(item, Modifier.animateItem())
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


// Notice: It is NO LONGER a @Composable function.
// It is an extension on LazyListScope.
fun <T : Any> LazyListScope.pagedMediaSection(
    modifier: Modifier = Modifier,
    title: String,
    items: LazyPagingItems<T>,
    config: MediaSectionConfig = MediaSectionConfig(),
    key: ((item: T) -> Any)? = null,
    onViewAllClick: (() -> Unit)? = null,
    itemContent: @Composable (item: T, modifier: Modifier) -> Unit
) {
    val isRefreshing = items.loadState.refresh is LoadState.Loading
    val isListEmpty = items.itemCount == 0 && !isRefreshing

    // 1. Header (Wrapped in a single item block)
    if (config.showHeader) {
        item {
            Column(modifier = modifier.fillMaxWidth()) {
                SectionHeader(
                    title = title,
                    onViewAllClick = if (config.showViewAll) onViewAllClick else null,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    when {
        // 2. Loading State
        isRefreshing && items.itemCount == 0 -> {
            item {
                SectionLoadingShimmer(config)
            }
        }

        // 3. Empty State
        isListEmpty -> {
            item {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(config.emptyHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No media found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. Data Layouts
        else -> {
            when (val layout = config.layout) {
                is SectionLayout.HorizontalRow -> {
                    // HORIZONTAL: We can wrap a LazyRow inside a single item{} safely!
                    item {
                        LazyRow(
                            modifier = modifier,
                            contentPadding = layout.contentPadding,
                            horizontalArrangement = Arrangement.spacedBy(layout.itemSpacing)
                        ) {
                            items(
                                count = items.itemCount,
                                key = if (key != null) items.itemKey(key) else null
                            ) { index ->
                                items[index]?.let { item ->
                                    itemContent(item, Modifier.animateItem())
                                }
                            }
                        }
                    }
                }

                is SectionLayout.VerticalList -> {
                    // VERTICAL: We MUST use the parent's scope directly.
                    // No item{} wrapper, we inject directly into the root LazyColumn.
                    items(
                        count = items.itemCount,
                        key = if (key != null) items.itemKey(key) else null
                    ) { index ->
                        items[index]?.let { item ->
                            // Padding has to be applied to the individual items here
                            Box(modifier = modifier.padding(horizontal = 16.dp, vertical = layout.itemSpacing / 2)) {
                                itemContent(item, Modifier)
                            }
                        }
                    }
                }

                is SectionLayout.Grid -> {
                    // GRID: LazyVerticalGrid will crash inside a LazyColumn.
                    // You have two options here:
                    // Option A: Use a FlowRow inside a single item { }
                    // Option B: Manually chunk the Paging items into rows.

                    /* Example Option A (requires Compose 1.5+): */
                    item {
                        FlowRow(
                            modifier = modifier.padding(layout.contentPadding),
                            horizontalArrangement = Arrangement.spacedBy(layout.horizontalSpacing),
                            verticalArrangement = Arrangement.spacedBy(layout.verticalSpacing),
                            maxItemsInEachRow = 3 // or calculate based on GridType
                        ) {
                            // Note: FlowRow isn't "lazy", so it will compose all fetched items.
                            // If your paging loads chunks of 20, it's usually fine.
                            for (index in 0 until items.itemCount) {
                                items[index]?.let { item ->
                                    itemContent(item, Modifier)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}