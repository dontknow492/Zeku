package com.ghost.zeku.data.paging.category

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.useWriterConnection
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.MediaEntity
import com.ghost.zeku.data.local.room.entities.MediaRemoteKeys
import com.ghost.zeku.data.local.room.toEntity
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.media.MediaCategory
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.provider.MediaListProvider

@OptIn(ExperimentalPagingApi::class)
class MediaRemoteMediator(
    private val category: MediaCategory,
    private val mediaType: MediaType,
    private val providerType: ProviderType,
    private val provider: MediaListProvider,
    private val database: AppDatabase,
    private val cacheTimeoutMillis: Long
) : RemoteMediator<Int, MediaEntity>() {

    private val mediaDao = database.mediaDao()

    private val remoteKeysDao = database.remoteKeysDao()

    private val categoryName = category.name

    override suspend fun initialize(): InitializeAction {

        val lastUpdated =
            remoteKeysDao.getMediaLastUpdated(
                provider = providerType,
                mediaType = mediaType,
                category = categoryName
            ) ?: 0L

        val cacheTimeout =
            System.currentTimeMillis() - lastUpdated

        return if (cacheTimeout < cacheTimeoutMillis) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MediaEntity>
    ): MediatorResult {

        return try {

            val page = when (loadType) {

                LoadType.REFRESH -> 1

                LoadType.PREPEND -> {
                    return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                }

                LoadType.APPEND -> {

                    val remoteKeys =
                        getRemoteKeyForLastItem(state)

                    val nextPage =
                        remoteKeys?.nextPage
                            ?: return MediatorResult.Success(
                                endOfPaginationReached = remoteKeys != null
                            )

                    nextPage
                }
            }

            val response =
                provider.getMediaList(
                    category = category,
                    mediaType = mediaType,
                    page = page,
                    perPage = state.config.pageSize
                )

            when (response) {

                is ApiResult.Success -> {

                    val mediaItems = response.data.items

                    val endOfPaginationReached =
                        !response.data.hasNextPage

                    database.useWriterConnection {

                        if (loadType == LoadType.REFRESH) {

                            remoteKeysDao.clearMediaKeys(
                                provider = providerType,
                                mediaType = mediaType,
                                category = categoryName
                            )
                        }

                        val prevPage =
                            if (page == 1) null else page - 1

                        val nextPage =
                            if (endOfPaginationReached) {
                                null
                            } else {
                                page + 1
                            }

                        val startIndex =
                            (page - 1) * state.config.pageSize

                        val keys =
                            mediaItems.mapIndexed { index, media ->

                                MediaRemoteKeys(
                                    mediaId = media.id,

                                    provider = providerType,

                                    mediaType = mediaType,

                                    category = categoryName,

                                    sortOrder = startIndex + index,

                                    prevPage = prevPage,

                                    nextPage = nextPage,

                                    lastUpdated = System.currentTimeMillis()
                                )
                            }

                        mediaDao.upsertMediaListWithSearch(
                            mediaItems.map { it.toEntity() }
                        )

                        remoteKeysDao.upsertMediaKeys(keys)
                    }

                    MediatorResult.Success(
                        endOfPaginationReached = endOfPaginationReached
                    )
                }

                is ApiResult.Empty -> {

                    MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                }

                is ApiResult.Error -> {

                    MediatorResult.Error(
                        response.error.cause
                            ?: Exception(response.error.message)
                    )
                }

                is ApiResult.Loading -> {

                    MediatorResult.Error(
                        IllegalStateException(
                            "API returned Loading state"
                        )
                    )
                }
            }

        } catch (e: Exception) {

            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, MediaEntity>
    ): MediaRemoteKeys? {

        return state.pages
            .lastOrNull { it.data.isNotEmpty() }
            ?.data
            ?.lastOrNull()
            ?.let { media ->

                remoteKeysDao.getMediaRemoteKey(
                    id = media.id,
                    provider = media.provider,
                    mediaType = media.mediaType,
                    category = categoryName
                )
            }
    }
}