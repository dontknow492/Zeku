package com.ghost.zeku.data.paging.category

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.RemoteMediator.MediatorResult.Error
import androidx.paging.RemoteMediator.MediatorResult.Success
import androidx.room.useWriterConnection
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.getErrorMessage
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.PageResult
import com.ghost.zeku.utils.formatTimestamp
import io.github.aakira.napier.Napier

/**
 * A Generic RemoteMediator for top-level category lists (Anime, Manga, etc.).
 * Handles all the complex Paging 3 LoadType logic and API Result wrapping.
 *
 * Unlike item-level mediators, this handles full category pages where
 * position/index within the list is important (for sorting/ranking).
 */
@OptIn(ExperimentalPagingApi::class)
abstract class BaseCategoryRemoteMediator<T : Any, E : Any, K : Any>(
    protected val categoryName: String,
    protected val currentProviderType: ProviderType,
    protected val database: AppDatabase,
    protected val cacheTimeoutMillis: Long,
) : RemoteMediator<Int, E>() {

    // NEW: The Paging 3 Initialize block
    override suspend fun initialize(): InitializeAction {
        val lastUpdated = getLastUpdatedTime() ?: 0L
        val isCacheValid = (System.currentTimeMillis() - lastUpdated) < cacheTimeoutMillis

        return if (isCacheValid) {
            Napier.d {
                "Cache for $categoryName is fresh (under ${formatTimestamp(cacheTimeoutMillis)}. " +
                        "Skipping network REFRESH and loading from DB."
            }
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            Napier.d {
                "Cache for $categoryName is expired or empty. " +
                        "Triggering network REFRESH."
            }
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    abstract suspend fun fetchFromNetwork(page: Int, pageSize: Int): ApiResult<PageResult<T>>
    abstract suspend fun clearRemoteKeys()
    abstract suspend fun getRemoteKey(id: Int): K?
    abstract fun getRemoteKeyNextPage(key: K): Int?
    abstract fun getEntityId(entity: E): Int

    // NEW: Abstract function to get the latest timestamp for this specific category
    abstract suspend fun getLastUpdatedTime(): Long?

    // Delegates the actual mapping and saving to the child class
    abstract suspend fun saveToDb(items: List<T>, startingIndex: Int, prevKey: Int?, nextKey: Int?)

    override suspend fun load(loadType: LoadType, state: PagingState<Int, E>): MediatorResult {
        val loadTypeName = loadType.name
        val itemCount = state.pages.sumOf { it.data.size }

        Napier.v {
            "Category RemoteMediator load() called: " +
                    "category=$categoryName, provider=$currentProviderType, " +
                    "loadType=$loadTypeName, loadedItems=$itemCount, " +
                    "pageCount=${state.pages.size}"
        }

        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    Napier.d {
                        "REFRESH triggered: category=$categoryName, " +
                                "resetting to page 1"
                    }
                    1
                }

                LoadType.PREPEND -> {
                    Napier.v {
                        "PREPEND not supported for categories: " +
                                "category=$categoryName, returning Success"
                    }
                    return Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val remoteKey = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKey?.let { getRemoteKeyNextPage(it) }

                    if (nextPage == null) {
                        val reason = if (remoteKey == null) "no remote key found" else "no next page"
                        Napier.v {
                            "APPEND: Cannot proceed for category=$categoryName: $reason, " +
                                    "endOfPagination=${remoteKey != null}"
                        }
                        return Success(endOfPaginationReached = remoteKey != null)
                    }

                    Napier.v {
                        "APPEND: Loading next page: category=$categoryName, " +
                                "page=$nextPage, currentPages=${state.pages.size}, " +
                                "loadedItems=$itemCount"
                    }
                    nextPage
                }
            }

            val pageSize = state.config.pageSize
            Napier.d {
                "Fetching category from network: category=$categoryName, " +
                        "provider=$currentProviderType, loadType=$loadTypeName, " +
                        "page=$page, pageSize=$pageSize"
            }

            when (val response = fetchFromNetwork(page, pageSize)) {
                is ApiResult.Success -> {
                    val endOfPaginationReached = !response.data.hasNextPage
                    val fetchedItemCount = response.data.items.size
                    val totalItems = response.data.totalPages

                    Napier.i {
                        "Category fetch successful: category=$categoryName, " +
                                "page=$page, items=$fetchedItemCount, " +
                                "total=$totalItems, hasNextPage=${response.data.hasNextPage}, " +
                                "endOfPagination=$endOfPaginationReached"
                    }

                    database.useWriterConnection {
                        if (loadType == LoadType.REFRESH) {
                            Napier.v {
                                "REFRESH: Clearing remote keys for category=$categoryName, " +
                                        "provider=$currentProviderType"
                            }
                            clearRemoteKeys()
                        }

                        val prevKey = if (page == 1) null else page - 1
                        val nextKey = if (endOfPaginationReached) null else page + 1
                        val startingIndex = (page - 1) * pageSize

                        Napier.v {
                            "Saving to database: category=$categoryName, page=$page, " +
                                    "startingIndex=$startingIndex, prevKey=$prevKey, " +
                                    "nextKey=$nextKey, itemCount=$fetchedItemCount"
                        }
                        saveToDb(response.data.items, startingIndex, prevKey, nextKey)
                    }

                    Napier.d {
                        "Category save complete: category=$categoryName, page=$page, " +
                                "endOfPagination=$endOfPaginationReached"
                    }
                    Success(endOfPaginationReached = endOfPaginationReached)
                }

                is ApiResult.Error -> {
                    val error = response.error
                    val errorMessage = response.getErrorMessage()

                    Napier.e(error.cause) {
                        "Category fetch failed: category=$categoryName, " +
                                "provider=$currentProviderType, page=$page, " +
                                "loadType=$loadTypeName, " +
                                "errorType=${error.type}, errorCode=${error.code}, " +
                                "message=$errorMessage, " +
                                "recoverable=${error.recoverable}"
                    }

                    Error(error.cause ?: Exception(errorMessage))
                }

                is ApiResult.Empty -> {
                    Napier.w {
                        "Category fetch returned empty: category=$categoryName, " +
                                "page=$page, loadType=$loadTypeName, " +
                                "treating as end of pagination"
                    }
                    Success(endOfPaginationReached = true)
                }

                is ApiResult.Loading -> {
                    Napier.e {
                        "Category fetch returned unexpected Loading state: " +
                                "category=$categoryName, page=$page. " +
                                "This should not happen - API returned Loading instead of data."
                    }
                    Error(
                        IllegalStateException(
                            "API unexpectedly returned a Loading state for category: $categoryName"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Napier.e(e) {
                "Unexpected exception in Category RemoteMediator: " +
                        "category=$categoryName, provider=$currentProviderType, " +
                        "loadType=$loadTypeName, " +
                        "exception=${e::class.simpleName}, message=${e.message}"
            }
            Error(e)
        }
    }

    /**
     * Retrieves the remote key for the last item in the current paging state.
     * Used during APPEND to determine the next page to fetch.
     */
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, E>): K? {
        val lastPage = state.pages.lastOrNull { it.data.isNotEmpty() }

        if (lastPage == null) {
            Napier.v {
                "No non-empty pages for remote key lookup: " +
                        "category=$categoryName"
            }
            return null
        }

        val lastEntity = lastPage.data.lastOrNull()

        if (lastEntity == null) {
            Napier.v {
                "Last page has no entities for remote key lookup: " +
                        "category=$categoryName"
            }
            return null
        }

        val entityId = getEntityId(lastEntity)
        val key = getRemoteKey(entityId)

        Napier.v {
            "Remote key lookup for category: category=$categoryName, " +
                    "entityId=$entityId, keyFound=${key != null}, " +
                    "nextPage=${key?.let { getRemoteKeyNextPage(it) }}"
        }

        return key
    }
}