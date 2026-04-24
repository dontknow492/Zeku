package com.ghost.zeku.data.paging


import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.RemoteMediator.MediatorResult.Error
import androidx.paging.RemoteMediator.MediatorResult.Success
import androidx.room.useWriterConnection
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.MangaEntity
import com.ghost.zeku.data.local.room.entities.MangaRemoteKeys
import com.ghost.zeku.data.local.room.toEntity
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.getErrorMessage
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.provider.MangaListProvider

@OptIn(ExperimentalPagingApi::class)
class MangaRemoteMediatorV1(
    private val category: MangaCategory,
    private val currentProviderType: ProviderType,
    private val provider: MangaListProvider,
    private val database: AppDatabase
) : RemoteMediator<Int, MangaEntity>() {

    private val mangaDao = database.mangaDao()
    private val remoteKeysDao = database.remoteKeysDao()

    // Convert the enum to a string for database linkage
    private val categoryName = category.name

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MangaEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return Success(endOfPaginationReached = remoteKeys != null)
                    nextPage
                }
            }

            // Fetch data from the API
            val response = provider.getMangaList(
                category = category,
                page = page,
                perPage = state.config.pageSize
            )

            when (response) {
                is ApiResult.Success -> {
                    val mangas = response.data.items
                    val endOfPaginationReached = !response.data.hasNextPage

                    database.useWriterConnection {
                        // 1. Clear keys ONLY for this category and provider on refresh
                        if (loadType == LoadType.REFRESH) {
                            remoteKeysDao.clearMangaKeys(source = currentProviderType, category = categoryName)
                            // The actual manga table is untouched so shared items aren't dropped
                        }

                        // 2. Pagination pointers
                        val prevKey = if (page == 1) null else page - 1
                        val nextKey = if (endOfPaginationReached) null else page + 1

                        // 3. Maintain exact API sort order in the database
                        val startingIndex = (page - 1) * state.config.pageSize

                        // 4. Map the category link keys
                        val keys = mangas.mapIndexed { index, manga ->
                            MangaRemoteKeys(
                                id = manga.id,
                                source = manga.source,
                                category = categoryName,
                                sortOrder = startingIndex + index,
                                prevPage = prevKey,
                                nextPage = nextKey
                            )
                        }

                        // 5. Save the raw Manga data and the Category Keys
                        mangaDao.upsertAll(mangas.map { it.toEntity() })
                        remoteKeysDao.upsertMangaKeys(keys)
                    }

                    Success(endOfPaginationReached = endOfPaginationReached)
                }

                is ApiResult.Error -> Error(response.error.cause ?: Exception(response.getErrorMessage()))

                is ApiResult.Empty -> {
                    // If the API returns an explicitly empty result, there is no more data to load.
                    // We tell Paging 3 that we have successfully reached the end of the list.
                    Success(endOfPaginationReached = true)

                }

                is ApiResult.Loading -> {
                    // A single-shot suspend API call should not return a "Loading" state as its final result.
                    // If it does somehow slip through, treat it as an error so Paging 3 doesn't freeze
                    // and can attempt to retry the request later.
                    Error(IllegalStateException("API unexpectedly returned a Loading state"))
                }
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, MangaEntity>): MangaRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { manga ->
                remoteKeysDao.getMangaRemoteKey(
                    id = manga.id,
                    source = manga.source,
                    category = categoryName
                )
            }
    }
}






