package com.ghost.zeku.data.paging.item

import androidx.paging.ExperimentalPagingApi
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.entities.ChapterEntity
import com.ghost.zeku.data.local.room.entities.ChapterRemoteKeys
import com.ghost.zeku.domain.model.enum.DownloadState
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.Chapter
import com.ghost.zeku.domain.provider.MangaDetailsProvider
import io.github.aakira.napier.Napier

/**
 * RemoteMediator for manga chapters.
 * Handles pagination, local state preservation (read status, downloads),
 * and caching for chapter lists.
 */
@OptIn(ExperimentalPagingApi::class)
class ChapterRemoteMediator(
    mediaId: Int,
    currentProviderType: ProviderType,
    private val provider: MangaDetailsProvider,
    database: AppDatabase,
    cacheTimeoutMillis: Long
) : BaseMediaItemRemoteMediator<Chapter, ChapterEntity, ChapterRemoteKeys>(
    mediaId, currentProviderType, database, cacheTimeoutMillis
) {
    private val chapterDao = database.chapterDao()
    private val remoteKeysDao = database.remoteKeysDao()

    init {
        Napier.v {
            "ChapterRemoteMediator initialized: mediaId=$mediaId, " +
                    "provider=$currentProviderType, cacheTimeoutMs=$cacheTimeoutMillis"
        }
    }

    override suspend fun fetchFromNetwork(page: Int, pageSize: Int) =
        provider.getMangaChapters(mediaId, page, pageSize).also { result ->
            when (result) {
                is com.ghost.zeku.domain.model.api.ApiResult.Success -> {
                    Napier.v {
                        "Manga chapters fetched: mediaId=$mediaId, page=$page, " +
                                "count=${result.data.items.size}, hasNextPage=${result.data.hasNextPage}"
                    }
                }

                is com.ghost.zeku.domain.model.api.ApiResult.Error -> {
                    Napier.w {
                        "Manga chapters fetch failed: mediaId=$mediaId, page=$page, " +
                                "error=${result.error.message}"
                    }
                }

                else -> {
                    Napier.v { "Manga chapters fetch status: mediaId=$mediaId, page=$page, result=${result::class.simpleName}" }
                }
            }
        }

    override suspend fun getRemoteKey(id: String): ChapterRemoteKeys? {
        val key = remoteKeysDao.getChapterRemoteKey(id, currentProviderType)
        Napier.v {
            "Chapter remote key lookup: id=$id, provider=$currentProviderType, " +
                    "found=${key != null}, " +
                    "nextPage=${key?.nextPage}, prevPage=${key?.prevPage}"
        }
        return key
    }

    override fun getRemoteKeyLastUpdated(key: ChapterRemoteKeys): Long {
        val age = System.currentTimeMillis() - key.lastUpdated
        Napier.v { "Chapter key age: id=${key.id}, ageMs=$age, updated=${key.lastUpdated}" }
        return key.lastUpdated
    }

    override fun getRemoteKeyNextPage(key: ChapterRemoteKeys): Int? {
        Napier.v { "Chapter next page: id=${key.id}, nextPage=${key.nextPage}" }
        return key.nextPage
    }

    override fun getEntityId(entity: ChapterEntity): String {
        return entity.id
    }

    override suspend fun clearRemoteKeys() {
        Napier.v {
            "Clearing chapter remote keys: mediaId=$mediaId, provider=$currentProviderType"
        }
        remoteKeysDao.clearChapterKeysByMedia(mediaId, currentProviderType)
        Napier.d {
            "Chapter remote keys cleared: mediaId=$mediaId, provider=$currentProviderType"
        }
    }

    override suspend fun saveToDb(items: List<Chapter>, prevKey: Int?, nextKey: Int?) {
        Napier.v {
            "Saving chapters to database: mediaId=$mediaId, " +
                    "count=${items.size}, prevKey=$prevKey, nextKey=$nextKey"
        }

        val currentTime = System.currentTimeMillis()

        // Fetch existing state to preserve user data (read progress, downloads)
        val existingStateMap = chapterDao.getChaptersForMediaSync(mediaId, currentProviderType)
            .associateBy { it.id }

        Napier.v {
            "Existing chapters in DB: mediaId=$mediaId, " +
                    "count=${existingStateMap.size}, " +
                    "ids=${existingStateMap.keys.take(5).joinToString()}..." // First 5 IDs for debug
        }

        // Track state preservation for logging
        var preservedCount = 0
        var newCount = 0
        var updatedCount = 0

        val chapterEntities = items.map { ch ->
            val existing = existingStateMap[ch.id]

            when {
                existing == null -> {
                    newCount++
                    Napier.v { "New chapter: id=${ch.id}, number=${ch.number}, title='${ch.title}'" }
                }

                existing.title != ch.title || existing.number != ch.number -> {
                    updatedCount++
                    Napier.v {
                        "Chapter updated from network: id=${ch.id}, " +
                                "oldNumber=${existing.number}, newNumber=${ch.number}"
                    }
                }

                existing.isRead || existing.downloadStatus != DownloadState.NONE -> {
                    preservedCount++
                    Napier.v {
                        "Chapter state preserved: id=${ch.id}, " +
                                "isRead=${existing.isRead}, download=${existing.downloadStatus}, " +
                                "lastReadPage=${existing.lastReadPage}"
                    }
                }
            }

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

        // Create remote keys for pagination tracking
        val keys = items.map { chapter ->
            ChapterRemoteKeys(
                id = chapter.id,
                source = currentProviderType,
                mediaId = mediaId,
                prevPage = prevKey,
                nextPage = nextKey,
                lastUpdated = currentTime
            )
        }

        // Perform database operations
        remoteKeysDao.upsertChapterKeys(keys)
        chapterDao.upsertAll(chapterEntities)

        Napier.i {
            "Chapters saved to database: mediaId=$mediaId, " +
                    "totalSaved=${chapterEntities.size}, " +
                    "new=$newCount, updated=$updatedCount, " +
                    "preservedState=$preservedCount, " +
                    "prevPage=$prevKey, nextPage=$nextKey"
        }

        // Log first and last chapter for pagination boundary visibility
        if (items.isNotEmpty()) {
            Napier.d {
                "Chapter page boundaries: mediaId=$mediaId, " +
                        "first=#${items.first().number} (${items.first().title}), " +
                        "last=#${items.last().number} (${items.last().title}), " +
                        "hasPrevPage=${prevKey != null}, hasNextPage=${nextKey != null}"
            }
        } else {
            Napier.w { "Empty chapter list saved: mediaId=$mediaId" }
        }
    }
}