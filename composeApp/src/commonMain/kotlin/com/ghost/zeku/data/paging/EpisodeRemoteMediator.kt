package com.ghost.zeku.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.useWriterConnection
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.EpisodeEntity
import com.ghost.zeku.data.local.room.entities.EpisodeRemoteKeys
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.DownloadState
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.provider.AnimeDetailsProvider

@OptIn(ExperimentalPagingApi::class)
class EpisodeRemoteMediator(
    private val mediaId: Int,
    private val currentProviderType: ProviderType,
    private val provider: AnimeDetailsProvider,
    private val database: AppDatabase,
    private val cacheTimeoutMillis: Long
) : RemoteMediator<Int, EpisodeEntity>() {

    private val episodeDao = database.episodeDao()
    private val remoteKeysDao = database.remoteKeysDao()

    // Set your TTL (Time To Live). 12 hours is a great balance for episodes.

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EpisodeEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    // --- TTL CACHE CHECK ---
                    // Grab the first visible item to find its remote key
                    val firstItem = state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
                    if (firstItem != null) {
                        val key = remoteKeysDao.getEpisodeRemoteKey(firstItem.id, currentProviderType)

                        // If the key exists and the cache is younger than 12 hours, SKIP the network!
                        if (key != null && (System.currentTimeMillis() - key.lastUpdated < cacheTimeoutMillis)) {
                            return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }
                    // Cache is expired (or doesn't exist), so fetch page 1
                    1
                }

                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextPage
                }
            }

            val response = provider.getAnimeEpisodes(id = mediaId, page = page, perPage = state.config.pageSize)

            when (response) {
                is ApiResult.Success -> {
                    val newEpisodes = response.data.items
                    val endOfPaginationReached = !response.data.hasNextPage

                    database.useWriterConnection {
                        if (loadType == LoadType.REFRESH) {
                            remoteKeysDao.clearEpisodeKeysByMedia(mediaId, currentProviderType)
                            // We DO NOT clear the actual episodes here! If we did, we'd lose offline downloads.
                        }

                        // 1. Fetch existing episodes to preserve download & watch states
                        val existingStateMap = episodeDao.getEpisodesForMediaSync(mediaId, currentProviderType)
                            .associateBy { it.id }

                        // 2. Map Network Models to Entities, injecting the preserved state!
                        val episodeEntities = newEpisodes.map { ep ->
                            val existing = existingStateMap[ep.id]
                            EpisodeEntity(
                                id = ep.id,
                                mediaId = mediaId,
                                source = currentProviderType,
                                number = ep.number.toFloat(),
                                title = ep.title,
                                description = ep.description,
                                thumbnail = ep.thumbnail,
                                isFiller = ep.isFiller,

                                // CRITICAL: Preserve the local state if it exists!
                                isWatched = existing?.isWatched ?: false,
                                watchProgressMillis = existing?.watchProgressMillis ?: 0L,
                                downloadStatus = existing?.downloadStatus ?: DownloadState.NONE,
                                localFilePath = existing?.localFilePath
                            )
                        }

                        val prevKey = if (page == 1) null else page - 1
                        val nextKey = if (endOfPaginationReached) null else page + 1

                        val currentTime = System.currentTimeMillis()

                        val keys = newEpisodes.map {
                            EpisodeRemoteKeys(
                                id = it.id,
                                source = currentProviderType,
                                mediaId = mediaId,
                                prevPage = prevKey,
                                nextPage = nextKey,
                                lastUpdated = currentTime // Explicitly save the fresh timestamp
                            )
                        }

                        remoteKeysDao.upsertEpisodeKeys(keys)
                        episodeDao.upsertAll(episodeEntities)
                    }
                    MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                }

                is ApiResult.Error -> MediatorResult.Error(Exception(response.error.message))
                is ApiResult.Empty -> MediatorResult.Success(endOfPaginationReached = true)
                is ApiResult.Loading -> MediatorResult.Success(endOfPaginationReached = false)
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, EpisodeEntity>): EpisodeRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { ep ->
            remoteKeysDao.getEpisodeRemoteKey(id = ep.id, source = currentProviderType)
        }
    }
}