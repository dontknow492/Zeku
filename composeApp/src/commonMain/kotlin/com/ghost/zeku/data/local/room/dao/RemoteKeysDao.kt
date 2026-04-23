package com.ghost.zeku.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.AnimeRemoteKeys
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
}