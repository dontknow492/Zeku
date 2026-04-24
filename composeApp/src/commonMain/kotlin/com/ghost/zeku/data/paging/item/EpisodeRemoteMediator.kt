package com.ghost.zeku.data.paging.item

import androidx.paging.ExperimentalPagingApi
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.EpisodeEntity
import com.ghost.zeku.data.local.room.entities.EpisodeRemoteKeys
import com.ghost.zeku.domain.model.enum.DownloadState
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.Episode
import com.ghost.zeku.domain.provider.AnimeDetailsProvider
import io.github.aakira.napier.Napier

/**
 * RemoteMediator for anime episodes.
 * Handles pagination, local state preservation (watch progress, downloads, filler tracking),
 * and caching for episode lists.
 */
@OptIn(ExperimentalPagingApi::class)
class EpisodeRemoteMediator(
    mediaId: Int,
    currentProviderType: ProviderType,
    private val provider: AnimeDetailsProvider,
    database: AppDatabase,
    cacheTimeoutMillis: Long
) : BaseMediaItemRemoteMediator<Episode, EpisodeEntity, EpisodeRemoteKeys>(
    mediaId, currentProviderType, database, cacheTimeoutMillis
) {
    private val episodeDao = database.episodeDao()
    private val remoteKeysDao = database.remoteKeysDao()

    init {
        Napier.v {
            "EpisodeRemoteMediator initialized: mediaId=$mediaId, " +
                    "provider=$currentProviderType, cacheTimeoutMs=$cacheTimeoutMillis"
        }
    }

    override suspend fun fetchFromNetwork(page: Int, pageSize: Int) =
        provider.getAnimeEpisodes(mediaId, page, pageSize).also { result ->
            when (result) {
                is com.ghost.zeku.domain.model.api.ApiResult.Success -> {
                    val episodes = result.data.items
                    val fillerCount = episodes.count { it.isFiller }
                    Napier.v {
                        "Anime episodes fetched: mediaId=$mediaId, page=$page, " +
                                "count=${episodes.size}, fillerEpisodes=$fillerCount, " +
                                "hasNextPage=${result.data.hasNextPage}"
                    }
                    // Log episode range for pagination verification
                    if (episodes.isNotEmpty()) {
                        Napier.d {
                            "Episode page boundaries: mediaId=$mediaId, page=$page, " +
                                    "range=Ep${episodes.first().number}..Ep${episodes.last().number}, " +
                                    "first='${episodes.first().title}', last='${episodes.last().title}'"
                        }
                    }
                }

                is com.ghost.zeku.domain.model.api.ApiResult.Error -> {
                    Napier.w {
                        "Anime episodes fetch failed: mediaId=$mediaId, page=$page, " +
                                "error=${result.error.message}"
                    }
                }

                else -> {
                    Napier.v {
                        "Anime episodes fetch status: mediaId=$mediaId, page=$page, " +
                                "result=${result::class.simpleName}"
                    }
                }
            }
        }

    override suspend fun getRemoteKey(id: String): EpisodeRemoteKeys? {
        val key = remoteKeysDao.getEpisodeRemoteKey(id, currentProviderType)
        Napier.v {
            "Episode remote key lookup: id=$id, provider=$currentProviderType, " +
                    "found=${key != null}, " +
                    "nextPage=${key?.nextPage}, prevPage=${key?.prevPage}"
        }
        return key
    }

    override fun getRemoteKeyLastUpdated(key: EpisodeRemoteKeys): Long {
        val age = System.currentTimeMillis() - key.lastUpdated
        Napier.v { "Episode key age: id=${key.id}, ageMs=$age, lastUpdated=${key.lastUpdated}" }
        return key.lastUpdated
    }

    override fun getRemoteKeyNextPage(key: EpisodeRemoteKeys): Int? {
        Napier.v { "Episode next page: id=${key.id}, nextPage=${key.nextPage}" }
        return key.nextPage
    }

    override fun getEntityId(entity: EpisodeEntity): String {
        return entity.id
    }

    override suspend fun clearRemoteKeys() {
        Napier.v {
            "Clearing episode remote keys: mediaId=$mediaId, provider=$currentProviderType"
        }
        remoteKeysDao.clearEpisodeKeysByMedia(mediaId, currentProviderType)
        Napier.d {
            "Episode remote keys cleared: mediaId=$mediaId, provider=$currentProviderType"
        }
    }

    override suspend fun saveToDb(items: List<Episode>, prevKey: Int?, nextKey: Int?) {
        Napier.v {
            "Saving episodes to database: mediaId=$mediaId, " +
                    "count=${items.size}, prevKey=$prevKey, nextKey=$nextKey"
        }

        val currentTime = System.currentTimeMillis()

        // 1. Fetch existing episodes to preserve download & watch states
        val existingStateMap = episodeDao.getEpisodesForMediaSync(mediaId, currentProviderType)
            .associateBy { it.id }

        Napier.v {
            "Existing episodes in DB: mediaId=$mediaId, " +
                    "count=${existingStateMap.size}, " +
                    "ids=${existingStateMap.keys.take(5).joinToString()}..." // First 5 IDs for debug
        }

        // Track state preservation for logging
        var newEpisodes = 0
        var updatedEpisodes = 0
        var preservedWatchState = 0
        var preservedDownloadState = 0
        var fillerEpisodes = 0

        // 2. Map Network Models to Entities, injecting the preserved state
        val episodeEntities = items.map { ep ->
            val existing = existingStateMap[ep.id]

            when {
                existing == null -> {
                    newEpisodes++
                    Napier.v {
                        "New episode: id=${ep.id}, number=${ep.number}, " +
                                "title='${ep.title}', isFiller=${ep.isFiller}"
                    }
                }

                existing.title != ep.title ||
                        existing.number != ep.number.toFloat() ||
                        existing.isFiller != ep.isFiller -> {
                    updatedEpisodes++
                    Napier.v {
                        "Episode updated from network: id=${ep.id}, " +
                                "oldNumber=${existing.number}, newNumber=${ep.number}, " +
                                "fillerChanged=${existing.isFiller != ep.isFiller}"
                    }
                }

                else -> {
                    // Episode data unchanged, but check preserved state
                    if (existing.isWatched) {
                        preservedWatchState++
                        Napier.v {
                            "Watch state preserved: id=${ep.id}, ep=${existing.number}, " +
                                    "progress=${existing.watchProgressMillis}ms"
                        }
                    }
                    if (existing.downloadStatus != DownloadState.NONE) {
                        preservedDownloadState++
                        Napier.v {
                            "Download state preserved: id=${ep.id}, ep=${existing.number}, " +
                                    "status=${existing.downloadStatus}, path=${existing.localFilePath}"
                        }
                    }
                }
            }

            if (ep.isFiller) fillerEpisodes++

            EpisodeEntity(
                id = ep.id,
                mediaId = mediaId,
                source = currentProviderType,
                number = ep.number.toFloat(),
                title = ep.title,
                description = ep.description,
                thumbnail = ep.thumbnail,
                isFiller = ep.isFiller,
                isWatched = existing?.isWatched ?: false,
                watchProgressMillis = existing?.watchProgressMillis ?: 0L,
                downloadStatus = existing?.downloadStatus ?: DownloadState.NONE,
                localFilePath = existing?.localFilePath
            )
        }

        // Create remote keys for pagination tracking
        val keys = items.map { episode ->
            EpisodeRemoteKeys(
                id = episode.id,
                source = currentProviderType,
                mediaId = mediaId,
                prevPage = prevKey,
                nextPage = nextKey,
                lastUpdated = currentTime
            )
        }

        // Perform database operations
        remoteKeysDao.upsertEpisodeKeys(keys)
        episodeDao.upsertAll(episodeEntities)

        Napier.i {
            "Episodes saved to database: mediaId=$mediaId, " +
                    "totalSaved=${episodeEntities.size}, " +
                    "new=$newEpisodes, updated=$updatedEpisodes, " +
                    "preservedWatchState=$preservedWatchState, " +
                    "preservedDownloadState=$preservedDownloadState, " +
                    "fillerEpisodes=$fillerEpisodes, " +
                    "prevPage=$prevKey, nextPage=$nextKey"
        }

        // Log pagination boundaries for debugging
        if (items.isNotEmpty()) {
            val firstEp = items.first()
            val lastEp = items.last()
            Napier.d {
                "Episode page boundaries: mediaId=$mediaId, " +
                        "range=Ep${firstEp.number}..Ep${lastEp.number}, " +
                        "first='${firstEp.title}' (filler=${firstEp.isFiller}), " +
                        "last='${lastEp.title}' (filler=${lastEp.isFiller}), " +
                        "hasPrevPage=${prevKey != null}, hasNextPage=${nextKey != null}"
            }
        } else {
            Napier.w { "Empty episode list saved: mediaId=$mediaId" }
        }

        // Log state preservation summary
        val totalWatched = episodeEntities.count { it.isWatched }
        val totalDownloaded = episodeEntities.count { it.downloadStatus == DownloadState.COMPLETED }
        val totalDownloading = episodeEntities.count { it.downloadStatus == DownloadState.DOWNLOADING }

        if (totalWatched > 0 || totalDownloaded > 0 || totalDownloading > 0) {
            Napier.i {
                "User state summary after save: mediaId=$mediaId, " +
                        "totalWatched=$totalWatched, " +
                        "completedDownloads=$totalDownloaded, " +
                        "activeDownloads=$totalDownloading"
            }
        }
    }
}