package com.ghost.zeku.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.useWriterConnection
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.ChapterEntity
import com.ghost.zeku.data.local.room.entities.ChapterRemoteKeys
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.enum.DownloadState
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.provider.MangaDetailsProvider

@OptIn(ExperimentalPagingApi::class)
class ChapterRemoteMediatorV1(
    private val mediaId: Int,
    private val currentProviderType: ProviderType,
    private val provider: MangaDetailsProvider,
    private val database: AppDatabase,
    private val cacheTimeoutMillis: Long
) : RemoteMediator<Int, ChapterEntity>() {

    private val chapterDao = database.chapterDao()
    private val remoteKeysDao = database.remoteKeysDao()


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ChapterEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    // --- TTL CACHE CHECK ---
                    val firstItem = state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
                    if (firstItem != null) {
                        val key = remoteKeysDao.getChapterRemoteKey(firstItem.id, currentProviderType)

                        if (key != null && (System.currentTimeMillis() - key.lastUpdated < cacheTimeoutMillis)) {
                            return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }
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

            val response = provider.getMangaChapters(id = mediaId, page = page, perPage = state.config.pageSize)

            when (response) {
                is ApiResult.Success -> {
                    val newChapters = response.data.items
                    val endOfPaginationReached = !response.data.hasNextPage

                    database.useWriterConnection {
                        if (loadType == LoadType.REFRESH) {
                            remoteKeysDao.clearChapterKeysByMedia(mediaId, currentProviderType)
                        }

                        val existingStateMap = chapterDao.getChaptersForMediaSync(mediaId, currentProviderType)
                            .associateBy { it.id }

                        val chapterEntities = newChapters.map { ch ->
                            val existing = existingStateMap[ch.id]
                            ChapterEntity(
                                id = ch.id,
                                mediaId = mediaId,
                                source = currentProviderType,
                                number = ch.number,
                                title = ch.title,
                                volume = ch.volume,

                                isRead = existing?.isRead ?: false,
                                lastReadPage = existing?.lastReadPage ?: 0,
                                downloadStatus = existing?.downloadStatus ?: DownloadState.NONE,
                                localFolderPath = existing?.localFolderPath
                            )
                        }

                        val prevKey = if (page == 1) null else page - 1
                        val nextKey = if (endOfPaginationReached) null else page + 1

                        val currentTime = System.currentTimeMillis()

                        val keys = newChapters.map {
                            ChapterRemoteKeys(
                                id = it.id,
                                source = currentProviderType,
                                mediaId = mediaId,
                                prevPage = prevKey,
                                nextPage = nextKey,
                                lastUpdated = currentTime // Explicitly save the fresh timestamp
                            )
                        }

                        remoteKeysDao.upsertChapterKeys(keys)
                        chapterDao.upsertAll(chapterEntities)
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

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ChapterEntity>): ChapterRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { ch ->
            remoteKeysDao.getChapterRemoteKey(id = ch.id, source = currentProviderType)
        }
    }
}