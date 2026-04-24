package com.ghost.zeku.data.paging.item

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
import io.github.aakira.napier.Napier

/**
 * A Generic RemoteMediator for detailed media items (Episodes, Chapters).
 * Automatically handles TTL (Time-To-Live) caching logic and pagination.
 *
 * Logging provides full visibility into the mediation lifecycle for debugging
 * pagination issues, cache behavior, and network errors.
 */
@OptIn(ExperimentalPagingApi::class)
abstract class BaseMediaItemRemoteMediator<T : Any, E : Any, K : Any>(
    protected val mediaId: Int,
    protected val currentProviderType: ProviderType,
    protected val database: AppDatabase,
    protected val cacheTimeoutMillis: Long
) : RemoteMediator<Int, E>() {

    abstract suspend fun fetchFromNetwork(page: Int, pageSize: Int): ApiResult<PageResult<T>>
    abstract suspend fun clearRemoteKeys()
    abstract suspend fun getRemoteKey(id: String): K?
    abstract fun getRemoteKeyLastUpdated(key: K): Long
    abstract fun getRemoteKeyNextPage(key: K): Int?
    abstract fun getEntityId(entity: E): String

    // Allows the child class to map entities while explicitly preserving local DB state
    abstract suspend fun saveToDb(items: List<T>, prevKey: Int?, nextKey: Int?)

    override suspend fun load(loadType: LoadType, state: PagingState<Int, E>): MediatorResult {
        val loadTypeName = loadType.name
        val itemCount = state.pages.sumOf { it.data.size }

        Napier.v {
            "RemoteMediator load() called: mediaId=$mediaId, " +
                    "provider=$currentProviderType, loadType=$loadTypeName, " +
                    "loadedItems=$itemCount, pageCount=${state.pages.size}, " +
                    "cacheTimeoutMs=$cacheTimeoutMillis"
        }

        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    // --- TTL CACHE CHECK ---
                    val firstItem = state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()

                    if (firstItem != null) {
                        val entityId = getEntityId(firstItem)
                        val key = getRemoteKey(entityId)

                        if (key != null) {
                            val cacheAge = System.currentTimeMillis() - getRemoteKeyLastUpdated(key)
                            val isCacheValid = cacheAge < cacheTimeoutMillis

                            Napier.v {
                                "Cache check: mediaId=$mediaId, entityId=$entityId, " +
                                        "cacheAgeMs=$cacheAge, cacheTimeoutMs=$cacheTimeoutMillis, " +
                                        "isCacheValid=$isCacheValid"
                            }

                            if (isCacheValid) {
                                Napier.d {
                                    "Cache still fresh: mediaId=$mediaId, " +
                                            "cacheAgeMs=$cacheAge, skipping network refresh"
                                }
                                return Success(endOfPaginationReached = false)
                            } else {
                                Napier.d {
                                    "Cache expired: mediaId=$mediaId, " +
                                            "cacheAgeMs=$cacheAge, fetching fresh data"
                                }
                            }
                        } else {
                            Napier.v { "No remote key found for entity: mediaId=$mediaId, entityId=$entityId" }
                        }
                    } else {
                        Napier.v { "No data in pages for REFRESH: mediaId=$mediaId, performing initial load" }
                    }
                    1
                }

                LoadType.PREPEND -> {
                    Napier.v { "PREPEND not supported: mediaId=$mediaId, returning Success" }
                    return Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    val remoteKey = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKey?.let { getRemoteKeyNextPage(it) }

                    if (nextPage == null) {
                        Napier.v {
                            "APPEND: No next page available: mediaId=$mediaId, " +
                                    "hasRemoteKey=${remoteKey != null}, endOfPagination=true"
                        }
                        return Success(endOfPaginationReached = remoteKey != null)
                    }

                    Napier.v {
                        "APPEND: Loading next page: mediaId=$mediaId, " +
                                "page=$nextPage, currentPages=${state.pages.size}"
                    }
                    nextPage
                }
            }

            Napier.d {
                "Fetching from network: mediaId=$mediaId, provider=$currentProviderType, " +
                        "loadType=$loadTypeName, page=$page, pageSize=${state.config.pageSize}"
            }

            when (val response = fetchFromNetwork(page, state.config.pageSize)) {
                is ApiResult.Success -> {
                    val endOfPaginationReached = !response.data.hasNextPage
                    val itemCount = response.data.items.size

                    Napier.i {
                        "Network fetch successful: mediaId=$mediaId, " +
                                "page=$page, items=$itemCount, " +
                                "hasNextPage=${response.data.hasNextPage}, " +
                                "endOfPagination=$endOfPaginationReached"
                    }

                    database.useWriterConnection {
                        if (loadType == LoadType.REFRESH) {
                            Napier.v {
                                "REFRESH: Clearing remote keys for mediaId=$mediaId (entities preserved)"
                            }
                            clearRemoteKeys()
                        }

                        val prevKey = if (page == 1) null else page - 1
                        val nextKey = if (endOfPaginationReached) null else page + 1

                        Napier.v {
                            "Saving to database: mediaId=$mediaId, page=$page, " +
                                    "prevKey=$prevKey, nextKey=$nextKey, itemCount=$itemCount"
                        }
                        saveToDb(response.data.items, prevKey, nextKey)
                    }

                    Napier.d {
                        "Database save complete: mediaId=$mediaId, page=$page, " +
                                "endOfPagination=$endOfPaginationReached"
                    }
                    Success(endOfPaginationReached = endOfPaginationReached)
                }

                is ApiResult.Error -> {
                    val errorMessage = response.getErrorMessage()
                    val errorType = response.error.type
                    val errorCode = response.error.code

                    Napier.e(response.error.cause) {
                        "Network fetch failed: mediaId=$mediaId, provider=$currentProviderType, " +
                                "page=$page, loadType=$loadTypeName, " +
                                "errorType=$errorType, errorCode=$errorCode, " +
                                "message=$errorMessage, " +
                                "recoverable=${response.error.recoverable}"
                    }

                    Error(response.error.cause ?: Exception(errorMessage))
                }

                is ApiResult.Empty -> {
                    Napier.w {
                        "Network fetch returned empty: mediaId=$mediaId, page=$page, " +
                                "loadType=$loadTypeName, treating as end of pagination"
                    }
                    Success(endOfPaginationReached = true)
                }

                is ApiResult.Loading -> {
                    Napier.v {
                        "Network fetch returned Loading state: mediaId=$mediaId, page=$page, " +
                                "progress=${response.progress}"
                    }
                    Success(endOfPaginationReached = false)
                }
            }
        } catch (e: Exception) {
            Napier.e(e) {
                "Unexpected exception in RemoteMediator: mediaId=$mediaId, " +
                        "provider=$currentProviderType, loadType=$loadTypeName, " +
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
            Napier.v { "No non-empty pages found for remote key lookup: mediaId=$mediaId" }
            return null
        }

        val lastEntity = lastPage.data.lastOrNull()

        if (lastEntity == null) {
            Napier.v { "Last page has no entities for remote key lookup: mediaId=$mediaId" }
            return null
        }

        val entityId = getEntityId(lastEntity)
        val key = getRemoteKey(entityId)

        Napier.v {
            "Remote key lookup: mediaId=$mediaId, entityId=$entityId, " +
                    "keyFound=${key != null}, " +
                    "nextPage=${key?.let { getRemoteKeyNextPage(it) }}"
        }

        return key
    }
}