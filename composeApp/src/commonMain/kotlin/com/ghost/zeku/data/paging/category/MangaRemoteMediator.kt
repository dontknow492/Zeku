package com.ghost.zeku.data.paging.category

import androidx.paging.ExperimentalPagingApi
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.MangaEntity
import com.ghost.zeku.data.local.room.entities.MangaRemoteKeys
import com.ghost.zeku.data.local.room.toEntity
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.provider.MangaListProvider
import io.github.aakira.napier.Napier

/**
 * RemoteMediator for manga category lists (Popular, Trending, Top Rated, etc.).
 * Handles pagination with sort order tracking for proper ranked display.
 */
@OptIn(ExperimentalPagingApi::class)
class MangaRemoteMediator(
    private val category: MangaCategory,
    currentProviderType: ProviderType,
    private val provider: MangaListProvider,
    database: AppDatabase
) : BaseCategoryRemoteMediator<Manga, MangaEntity, MangaRemoteKeys>(
    category.name, currentProviderType, database
) {
    private val mangaDao = database.mangaDao()
    private val remoteKeysDao = database.remoteKeysDao()

    init {
        Napier.v {
            "MangaRemoteMediator initialized: category=$category, " +
                    "provider=$currentProviderType"
        }
    }

    override suspend fun fetchFromNetwork(page: Int, pageSize: Int) =
        provider.getMangaList(category, page, pageSize).also { result ->
            when (result) {
                is com.ghost.zeku.domain.model.api.ApiResult.Success -> {
                    val items = result.data.items
                    Napier.v {
                        "Manga list fetched: category=$category, page=$page, " +
                                "count=${items.size}, totalPages=${result.data.totalPages}, " +
                                "hasNextPage=${result.data.hasNextPage}"
                    }
                    // Log first and last manga for pagination boundary verification
                    if (items.isNotEmpty()) {
                        Napier.d {
                            "Manga page boundaries: category=$category, page=$page, " +
                                    "range=#${items.first().id}..${items.last().id}, " +
                                    "first='${items.first().title.english ?: items.first().title.romaji}', " +
                                    "last='${items.last().title.english ?: items.last().title.romaji}'"
                        }
                    }
                }

                is com.ghost.zeku.domain.model.api.ApiResult.Error -> {
                    Napier.w {
                        "Manga list fetch failed: category=$category, page=$page, " +
                                "error=${result.error.message}"
                    }
                }

                else -> {
                    Napier.v {
                        "Manga list fetch status: category=$category, page=$page, " +
                                "result=${result::class.simpleName}"
                    }
                }
            }
        }

    override suspend fun getRemoteKey(id: Int): MangaRemoteKeys? {
        val key = remoteKeysDao.getMangaRemoteKey(id, currentProviderType, categoryName)
        Napier.v {
            "Manga remote key lookup: id=$id, category=$category, " +
                    "provider=$currentProviderType, found=${key != null}, " +
                    "nextPage=${key?.nextPage}, sortOrder=${key?.sortOrder}"
        }
        return key
    }

    override fun getRemoteKeyNextPage(key: MangaRemoteKeys): Int? {
        Napier.v {
            "Manga next page: id=${key.id}, category=$category, " +
                    "nextPage=${key.nextPage}, sortOrder=${key.sortOrder}"
        }
        return key.nextPage
    }

    override fun getEntityId(entity: MangaEntity): Int {
        return entity.id
    }

    override suspend fun clearRemoteKeys() {
        Napier.v {
            "Clearing manga remote keys: category=$category, " +
                    "provider=$currentProviderType"
        }
        remoteKeysDao.clearMangaKeys(currentProviderType, categoryName)
        Napier.d {
            "Manga remote keys cleared: category=$category, " +
                    "provider=$currentProviderType"
        }
    }

    override suspend fun saveToDb(
        items: List<Manga>,
        startingIndex: Int,
        prevKey: Int?,
        nextKey: Int?
    ) {
        Napier.v {
            "Saving manga to database: category=$category, " +
                    "count=${items.size}, startingIndex=$startingIndex, " +
                    "prevKey=$prevKey, nextKey=$nextKey"
        }

        val currentTime = System.currentTimeMillis()

        // Create remote keys with sort order for proper ranking
        val keys = items.mapIndexed { index, manga ->
            val sortOrder = startingIndex + index
            Napier.v {
                "Manga key mapping: id=${manga.id}, " +
                        "index=$index, sortOrder=$sortOrder, " +
                        "title='${manga.title.english ?: manga.title.romaji}'"
            }

            MangaRemoteKeys(
                id = manga.id,
                source = currentProviderType,
                category = categoryName,
                sortOrder = sortOrder,
                prevPage = prevKey,
                nextPage = nextKey,
                lastUpdated = currentTime
            )
        }

        // Convert domain models to entities
        val entities = items.map { manga ->
            val entity = manga.toEntity()
            Napier.v {
                "Manga entity created: id=${entity.id}, " +
                        "title='${manga.title.english ?: manga.title.romaji}', " +
                        "format=${manga.format}, status=${manga.status}, " +
                        "chapters=${manga.chapters}, volumes=${manga.volumes}, " +
                        "averageScore=${manga.score}"
            }
            entity
        }

        // Perform database operations
        remoteKeysDao.upsertMangaKeys(keys)
        mangaDao.upsertAll(entities)

        // Calculate statistics for the saved batch
        val genres = items.flatMap { it.genres }.distinct().take(10)
        val averageScore = items.mapNotNull { it.score }.average().takeIf { !it.isNaN() }
        val totalChapters = items.sumOf { it.chapters ?: 0 }
        val totalVolumes = items.sumOf { it.volumes ?: 0 }
        val currentlyPublishing = items.count {
            it.status?.name?.contains("RELEASING", ignoreCase = true) == true
        }
        val completedSeries = items.count {
            it.status?.name?.contains("FINISHED", ignoreCase = true) == true
        }

        Napier.i {
            "Manga saved to database: category=$category, " +
                    "count=${entities.size}, " +
                    "startingIndex=$startingIndex..${startingIndex + items.size - 1}, " +
                    "prevPage=$prevKey, nextPage=$nextKey, " +
                    "avgScore=${averageScore?.let { String.format("%.1f", it) } ?: "N/A"}, " +
                    "totalChapters=$totalChapters, " +
                    "totalVolumes=$totalVolumes, " +
                    "currentlyPublishing=$currentlyPublishing, " +
                    "completed=$completedSeries, " +
                    "topGenres=${genres.joinToString(", ")}"
        }

        // Log first and last manga for boundary verification
        if (items.isNotEmpty()) {
            val first = items.first()
            val last = items.last()
            Napier.d {
                "Manga save boundaries: category=$category, " +
                        "first=#${first.id} '${first.title.english ?: first.title.romaji}' " +
                        "(${first.format}, ${first.score}%), " +
                        "last=#${last.id} '${last.title.english ?: last.title.romaji}' " +
                        "(${last.format}, ${last.score}%)"
            }
        } else {
            Napier.w {
                "Empty manga list saved: category=$category"
            }
        }
    }
}