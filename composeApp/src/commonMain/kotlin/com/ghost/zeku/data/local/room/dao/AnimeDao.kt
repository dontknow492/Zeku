package com.ghost.zeku.data.local.room.dao


import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.AnimeDetailsEntity
import com.ghost.zeku.data.local.room.entities.AnimeEntity
import com.ghost.zeku.domain.model.enum.AnimeCategory
import com.ghost.zeku.domain.model.enum.ProviderType
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    /**
     * Upsert = Update or Insert.
     * If the anime doesn't exist, it inserts it.
     * If it DOES exist, it replaces it (perfect for adding 'Details' to a 'List' item).
     */
    @Upsert
    suspend fun upsertAll(animeList: List<AnimeEntity>)

    @Upsert
    suspend fun upsert(anime: AnimeEntity)

    /**
     * Used for the Details Screen. Returns a Flow so the UI automatically
     * updates when the API finishes fetching the detailed data.
     */
    @Query("SELECT * FROM anime WHERE id = :id AND source = :source")
    fun observeAnime(id: Int, source: ProviderType): Flow<AnimeEntity?>

    /**
     * Used for Paging 3. Room handles the limit/offset behind the scenes!
     */
    @Query("SELECT * FROM anime WHERE source = :source ORDER BY id ASC")
    fun getAnimePagingSource(source: ProviderType): PagingSource<Int, AnimeEntity>

    /**
     * Clears cached data for a specific provider.
     * Useful if the user switches from MAL to AniList and you want to clean up.
     */
    @Query("DELETE FROM anime WHERE source = :source")
    suspend fun clearAllBySource(source: ProviderType)


    // --- DETAILS CACHING ---
    @Upsert
    suspend fun upsertAnimeDetails(details: AnimeDetailsEntity)

    @Query("SELECT * FROM anime_details WHERE id = :id AND source = :source")
    fun observeAnimeDetails(id: Int, source: ProviderType): Flow<AnimeDetailsEntity?>

    @Query("DELETE FROM anime_details WHERE source = :source")
    suspend fun clearDetailsBySource(source: ProviderType)


    @Query(
        """
        SELECT anime.* FROM anime 
        INNER JOIN anime_remote_keys 
            ON anime.id = anime_remote_keys.id AND anime.source = anime_remote_keys.source
            WHERE anime_remote_keys.category = :category AND anime_remote_keys.source = :source
            ORDER BY anime_remote_keys.sortOrder ASC
            """
    )
    fun getAnimeByCategory(category: AnimeCategory, source: ProviderType): PagingSource<Int, AnimeEntity>
}



