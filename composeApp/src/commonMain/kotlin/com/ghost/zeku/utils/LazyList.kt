package com.ghost.zeku.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf

@Composable
fun <T : Any> List<T>.toPagingItems(): LazyPagingItems<T> {
    val flow = remember(this) {
        flowOf(PagingData.from(this))
    }
    return flow.collectAsLazyPagingItems()
}


fun <T : Any> LazyListScope.pagedVerticalMediaSection(
    title: String,
    items: LazyPagingItems<T>,
    onViewAllClick: (() -> Unit)? = null,
    sectionHeader: @Composable () -> Unit = {},
    itemContent: @Composable (item: T, modifier: Modifier) -> Unit
) {
    // 1. Header
    item {
        sectionHeader.invoke()

    }

    val isRefreshing = items.loadState.refresh is LoadState.Loading
    val isListEmpty = items.itemCount == 0 && !isRefreshing

    when {
        // 2. Loading State
        isRefreshing && items.itemCount == 0 -> {
            item {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
        // 3. Empty State
        isListEmpty -> {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No media found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        // 4. The Paged Items
        else -> {
            items(count = items.itemCount) { index ->
                items[index]?.let { item ->
                    itemContent(item, Modifier.animateItem())
                }
            }
        }
    }
}