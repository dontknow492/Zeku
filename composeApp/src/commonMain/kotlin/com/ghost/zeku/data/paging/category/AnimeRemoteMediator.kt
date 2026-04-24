package com.ghost.zeku.data.paging.category

import androidx.paging.ExperimentalPagingApi
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.AnimeEntity
import com.ghost.zeku.data.local.room.entities.AnimeRemoteKeys
import com.ghost.zeku.data.local.room.toEntity
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.provider.AnimeListProvider
import io.github.aakira.napier.Napier

/**
 * RemoteMediator for anime category lists (Popular, Trending, Upcoming, etc.).
 * Handles pagination with sort order tracking for proper ranked display.
 */
@OptIn(ExperimentalPagingApi::class)
class AnimeRemoteMediator(
    private val category: AnimeCategory,
    currentProviderType: ProviderType,
    private val provider: AnimeListProvider,
    database: AppDatabase
) : BaseCategoryRemoteMediator<Anime, AnimeEntity, AnimeRemoteKeys>(
    category.name, currentProviderType, database
) {
    private val animeDao = database.animeDao()
    private val remoteKeysDao = database.remoteKeysDao()

    init {
        Napier.v {
            "AnimeRemoteMediator initialized: category=$category, " +
                    "provider=$currentProviderType"
        }
    }

    override suspend fun fetchFromNetwork(page: Int, pageSize: Int) =
        provider.getAnimeList(category, page, pageSize).also { result ->
            when (result) {
                is com.ghost.zeku.domain.model.api.ApiResult.Success -> {
                    val items = result.data.items
                    Napier.v {
                        "Anime list fetched: category=$category, page=$page, " +
                                "count=${items.size}, totalPages=${result.data.totalPages}, " +
                                "hasNextPage=${result.data.hasNextPage}"
                    }
                    // Log first and last anime for pagination boundary verification
                    if (items.isNotEmpty()) {
                        Napier.d {
                            "Anime page boundaries: category=$category, page=$page, " +
                                    "range=#${items.first().id}..${items.last().id}, " +
                                    "first='${items.first().title.english ?: items.first().title.romaji}', " +
                                    "last='${items.last().title.english ?: items.last().title.romaji}'"
                        }
                    }
                }

                is com.ghost.zeku.domain.model.api.ApiResult.Error -> {
                    Napier.w {
                        "Anime list fetch failed: category=$category, page=$page, " +
                                "error=${result.error.message}"
                    }
                }

                else -> {
                    Napier.v {
                        "Anime list fetch status: category=$category, page=$page, " +
                                "result=${result::class.simpleName}"
                    }
                }
            }
        }

    override suspend fun getRemoteKey(id: Int): AnimeRemoteKeys? {
        val key = remoteKeysDao.getAnimeRemoteKey(id, currentProviderType, categoryName)
        Napier.v {
            "Anime remote key lookup: id=$id, category=$category, " +
                    "provider=$currentProviderType, found=${key != null}, " +
                    "nextPage=${key?.nextPage}, sortOrder=${key?.sortOrder}"
        }
        return key
    }

    override fun getRemoteKeyNextPage(key: AnimeRemoteKeys): Int? {
        Napier.v {
            "Anime next page: id=${key.id}, category=$category, " +
                    "nextPage=${key.nextPage}, sortOrder=${key.sortOrder}"
        }
        return key.nextPage
    }

    override fun getEntityId(entity: AnimeEntity): Int {
        return entity.id
    }

    override suspend fun clearRemoteKeys() {
        Napier.v {
            "Clearing anime remote keys: category=$category, " +
                    "provider=$currentProviderType"
        }
        remoteKeysDao.clearAnimeKeys(currentProviderType, categoryName)
        Napier.d {
            "Anime remote keys cleared: category=$category, " +
                    "provider=$currentProviderType"
        }
    }

    override suspend fun saveToDb(
        items: List<Anime>,
        startingIndex: Int,
        prevKey: Int?,
        nextKey: Int?
    ) {
        Napier.v {
            "Saving anime to database: category=$category, " +
                    "count=${items.size}, startingIndex=$startingIndex, " +
                    "prevKey=$prevKey, nextKey=$nextKey"
        }

        val currentTime = System.currentTimeMillis()

        // Create remote keys with sort order for proper ranking
        val keys = items.mapIndexed { index, anime ->
            val sortOrder = startingIndex + index
            Napier.v {
                "Anime key mapping: id=${anime.id}, " +
                        "index=$index, sortOrder=$sortOrder, " +
                        "title='${anime.title.english ?: anime.title.romaji}'"
            }

            AnimeRemoteKeys(
                id = anime.id,
                source = currentProviderType,
                category = categoryName,
                sortOrder = sortOrder,
                prevPage = prevKey,
                nextPage = nextKey,
                lastUpdated = currentTime
            )
        }

        // Convert domain models to entities
        val entities = items.map { anime ->
            val entity = anime.toEntity()
            Napier.v {
                "Anime entity created: id=${entity.id}, " +
                        "title='${anime.title.english ?: anime.title.romaji}', " +
                        "format=${anime.format}, status=${anime.status}, " +
                        "episodes=${anime.episodes}, " +
                        "averageScore=${anime.score}"
            }
            entity
        }

        // Perform database operations
        remoteKeysDao.upsertAnimeKeys(keys)
        animeDao.upsertAll(entities)

        // Calculate statistics for the saved batch
        val genres = items.flatMap { it.genres }.distinct().take(10)
        val averageScore = items.mapNotNull { it.score }.average().takeIf { !it.isNaN() }
        val totalEpisodes = items.sumOf { it.episodes ?: 0 }
        val currentlyAiring = items.count { it.status?.name?.contains("RELEASING", ignoreCase = true) == true }

        Napier.i {
            "Anime saved to database: category=$category, " +
                    "count=${entities.size}, " +
                    "startingIndex=$startingIndex..${startingIndex + items.size - 1}, " +
                    "prevPage=$prevKey, nextPage=$nextKey, " +
                    "avgScore=${averageScore?.let { String.format("%.1f", it) } ?: "N/A"}, " +
                    "totalEpisodes=$totalEpisodes, " +
                    "currentlyAiring=$currentlyAiring, " +
                    "topGenres=${genres.joinToString(", ")}"
        }

        // Log first and last anime for boundary verification
        if (items.isNotEmpty()) {
            val first = items.first()
            val last = items.last()
            Napier.d {
                "Anime save boundaries: category=$category, " +
                        "first=#${first.id} '${first.title.english ?: first.title.romaji}' " +
                        "(${first.format}, ${first.score}%), " +
                        "last=#${last.id} '${last.title.english ?: last.title.romaji}' " +
                        "(${last.format}, ${last.score}%)"
            }
        } else {
            Napier.w {
                "Empty anime list saved: category=$category"
            }
        }
    }
}