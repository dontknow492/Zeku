package com.ghost.zeku.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.AnimeRemoteKeys
import com.ghost.zeku.data.local.room.entities.ChapterRemoteKeys
import com.ghost.zeku.data.local.room.entities.EpisodeRemoteKeys
import com.ghost.zeku.data.local.room.entities.MangaRemoteKeys
import com.ghost.zeku.domain.model.enum.ProviderType

/**
 * A unified DAO for managing the pagination and category links.
 * Keeping them in one DAO reduces boilerplate in your AppDatabase.
 */
@Dao
interface RemoteKeysDao {

    // --- ANIME KEYS ---
    @Upsert
    suspend fun upsertAnimeKeys(keys: List<AnimeRemoteKeys>)

    @Query("SELECT * FROM anime_remote_keys WHERE id = :id AND source = :source AND category = :category")
    suspend fun getAnimeRemoteKey(id: Int, source: ProviderType, category: String): AnimeRemoteKeys?

    @Query("DELETE FROM anime_remote_keys WHERE source = :source AND category = :category")
    suspend fun clearAnimeKeys(source: ProviderType, category: String)


    // --- MANGA KEYS ---
    @Upsert
    suspend fun upsertMangaKeys(keys: List<MangaRemoteKeys>)

    @Query("SELECT * FROM manga_remote_keys WHERE id = :id AND source = :source AND category = :category")
    suspend fun getMangaRemoteKey(id: Int, source: ProviderType, category: String): MangaRemoteKeys?

    @Query("DELETE FROM manga_remote_keys WHERE source = :source AND category = :category")
    suspend fun clearMangaKeys(source: ProviderType, category: String)


    // --- EPISODE KEYS ---
    @Upsert
    suspend fun upsertEpisodeKeys(keys: List<EpisodeRemoteKeys>)

    @Query("SELECT * FROM episode_remote_keys WHERE id = :id AND source = :source")
    suspend fun getEpisodeRemoteKey(id: String, source: ProviderType): EpisodeRemoteKeys?

    @Query("DELETE FROM episode_remote_keys WHERE mediaId = :mediaId AND source = :source")
    suspend fun clearEpisodeKeysByMedia(mediaId: Int, source: ProviderType)

    // --- CHAPTER KEYS ---
    @Upsert
    suspend fun upsertChapterKeys(keys: List<ChapterRemoteKeys>)

    @Query("SELECT * FROM chapter_remote_keys WHERE id = :id AND source = :source")
    suspend fun getChapterRemoteKey(id: String, source: ProviderType): ChapterRemoteKeys?

    @Query("DELETE FROM chapter_remote_keys WHERE mediaId = :mediaId AND source = :source")
    suspend fun clearChapterKeysByMedia(mediaId: Int, source: ProviderType)
}