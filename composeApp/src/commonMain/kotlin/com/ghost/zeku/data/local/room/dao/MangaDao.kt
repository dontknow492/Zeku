package com.ghost.zeku.data.local.room.dao


import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.MangaDetailsEntity
import com.ghost.zeku.data.local.room.entities.MangaEntity
import com.ghost.zeku.domain.model.enum.MangaCategory
import com.ghost.zeku.domain.model.enum.ProviderType
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {

    /**
     * Upsert = Update or Insert.
     * If the manga doesn't exist, it inserts it.
     * If it DOES exist, it replaces it (perfect for adding 'Details' to a 'List' item).
     */
    @Upsert
    suspend fun upsertAll(mangaList: List<MangaEntity>)

    @Upsert
    suspend fun upsert(manga: MangaEntity)

    /**
     * Used for the Details Screen. Returns a Flow so the UI automatically
     * updates when the API finishes fetching the detailed data.
     */
    @Query("SELECT * FROM manga WHERE id = :id AND source = :source")
    fun observeManga(id: Int, source: ProviderType): Flow<MangaEntity?>

    /**
     * Used for Paging 3. Room handles the limit/offset behind the scenes!
     */
    @Query("SELECT * FROM manga WHERE source = :source ORDER BY id ASC")
    fun getMangaPagingSource(source: ProviderType): PagingSource<Int, MangaEntity>

    /**
     * Clears cached data for a specific provider.
     * Useful if the user switches from MAL to AniList and you want to clean up.
     */
    @Query("DELETE FROM manga WHERE source = :source")
    suspend fun clearAllBySource(source: ProviderType)


    // --- DETAILS CACHING ---
    @Upsert
    suspend fun upsertMangaDetails(details: MangaDetailsEntity)

    @Query("SELECT * FROM manga_details WHERE id = :id AND source = :source")
    fun observeMangaDetails(id: Int, source: ProviderType): Flow<MangaDetailsEntity?>

    @Query("DELETE FROM manga_details WHERE source = :source")
    suspend fun clearDetailsBySource(source: ProviderType)


    @Query(
        """
        SELECT manga.* FROM manga 
        INNER JOIN manga_remote_keys 
            ON manga.id = manga_remote_keys.id AND manga.source = manga_remote_keys.source
        WHERE manga_remote_keys.category = :category AND manga_remote_keys.source = :source
        ORDER BY manga_remote_keys.sortOrder ASC
    """
    )
    fun getMangaByCategory(category: MangaCategory, source: ProviderType): PagingSource<Int, MangaEntity>


    @Query(
        """
        SELECT manga.* FROM manga 
        INNER JOIN manga_remote_keys 
            ON manga.id = manga_remote_keys.id AND manga.source = manga_remote_keys.source
        WHERE manga_remote_keys.category = :category AND manga_remote_keys.source = :source
        ORDER BY manga_remote_keys.sortOrder ASC
        LIMIT :limit
    """
    )
    fun observeMangaByCategory(category: String, source: ProviderType, limit: Int): Flow<List<MangaEntity>>
}