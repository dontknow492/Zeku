package com.ghost.zeku.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity
import com.ghost.zeku.data.local.room.entities.LibraryEntity
import com.ghost.zeku.data.local.room.entities.MediaDetailsEntity
import com.ghost.zeku.data.local.room.entities.MediaEntity
import com.ghost.zeku.data.local.room.entities.MediaSearchEntity
import com.ghost.zeku.data.local.room.view.MediaLibraryView
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.model.media.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    // =========================================================
    // BASE MEDIA
    // =========================================================

    @Upsert
    suspend fun upsertAll(media: List<MediaEntity>)

    @Upsert
    suspend fun upsert(media: MediaEntity)


    @Upsert
    suspend fun upsertMediaInternal(media: MediaEntity)

    // 2. FTS Tables duplicate data if you just insert, so we must delete the old one first
    @Query("DELETE FROM media_search WHERE mediaId = :mediaId AND provider = :provider")
    suspend fun deleteSearchInternal(mediaId: Int, provider: String)

    @Insert
    suspend fun insertMediaSearchInternal(search: MediaSearchEntity)

    /**
     * 3. The public upsert method used by your Repository.
     * @Transaction ensures that if one fails, the other rolls back.
     */
    @Transaction
    suspend fun upsertMediaWithSearch(
        media: MediaEntity,
    ) {
        // Step 1: Upsert the actual media data
        upsertMediaInternal(media)

        // Step 2: Delete the old search index (prevents duplicate search results!)
        deleteSearchInternal(media.id, media.provider.name)

        // Step 3: Mash all possible names together for the Search Index
        val allSearchableTitles = listOfNotNull(
            media.title.romaji,
            media.title.english,
            media.title.native,
            media.title.userPreferred
        ) + media.synonyms

        // Join them with a space
        val combinedSynonyms = allSearchableTitles.joinToString(" ")

        // Step 4: Create and save the fresh FTS search entity
        val searchEntity = MediaSearchEntity(
            mediaId = media.id,
            provider = media.provider.name,
            title = media.title.userPreferred ?: media.title.romaji ?: "",
            synonyms = combinedSynonyms,
            description = media.description ?: ""
        )

        insertMediaSearchInternal(searchEntity)
    }


    /**
     * Helper for upserting a whole list from a network fetch.
     */
    @Transaction
    suspend fun upsertMediaListWithSearch(
        medias: List<MediaEntity>
    ) {
        medias.forEach { media ->
            upsertMediaWithSearch(media)
        }
    }





    @Query(
        """
        SELECT * FROM media
        WHERE id = :id
        AND provider = :source
        """
    )
    fun observeMedia(
        id: Int,
        source: ProviderType
    ): Flow<MediaEntity?>

    @Query(
        """
        SELECT * FROM media
        WHERE provider = :source
        AND mediaType = :mediaType
        ORDER BY id ASC
        """
    )
    fun getPagingSource(
        source: ProviderType,
        mediaType: MediaType
    ): PagingSource<Int, MediaEntity>

    @Query(
        """
        DELETE FROM media
        WHERE provider = :source
        """
    )
    suspend fun clearAllBySource(
        source: ProviderType
    )

    // =========================================================
    // DETAILS
    // =========================================================

    @Upsert
    suspend fun upsertDetails(
        details: MediaDetailsEntity
    )

    @Query(
        """
        SELECT * FROM media_details
        WHERE id = :id
        AND provider = :provider
        """
    )
    fun observeDetails(
        id: Int,
        provider: ProviderType
    ): Flow<MediaDetailsEntity?>

    @Query(
        """
        DELETE FROM media_details
        WHERE provider = :source
        """
    )
    suspend fun clearDetailsBySource(
        source: ProviderType
    )

    // =========================================================
    // CATEGORY PAGING
    // =========================================================

    @Query(
        """
        SELECT media.* 
        FROM media

        INNER JOIN media_remote_keys
        ON media.id = media_remote_keys.mediaId
        AND media.provider = media_remote_keys.provider
        AND media.mediaType = media_remote_keys.mediaType

        WHERE media_remote_keys.category = :category
        AND media_remote_keys.provider = :provider
        AND media_remote_keys.mediaType = :mediaType

        ORDER BY media_remote_keys.sortOrder ASC
        """
    )
    fun getMediaByCategory(
        category: String,
        provider: ProviderType,
        mediaType: MediaType
    ): PagingSource<Int, MediaEntity>

    @Query(
        """
        SELECT media.* 
        FROM media

        INNER JOIN media_remote_keys
        ON media.id = media_remote_keys.mediaId
        AND media.provider = media_remote_keys.provider
        AND media.mediaType = media_remote_keys.mediaType

        WHERE media_remote_keys.category = :category
        AND media_remote_keys.provider = :provider
        AND media_remote_keys.mediaType = :mediaType

        ORDER BY media_remote_keys.sortOrder ASC

        LIMIT :limit
        """
    )
    fun observeMediaByCategory(
        category: String,
        provider: ProviderType,
        mediaType: MediaType,
        limit: Int
    ): Flow<List<MediaEntity>>

    // =========================================================
    // CLEANUP
    // =========================================================

    @Query(
        """
        DELETE FROM media
        WHERE provider = :provider
        AND mediaType = :mediaType
        """
    )
    suspend fun clearByType(
        provider: ProviderType,
        mediaType: MediaType
    )

    @Query(
        """
        DELETE FROM media_remote_keys
        WHERE provider = :provider
        AND mediaType = :mediaType
        """
    )
    suspend fun clearRemoteKeys(
        provider: ProviderType,
        mediaType: MediaType
    )


    @RawQuery(
        observedEntities = [
            MediaEntity::class,
            LibraryEntity::class,
            LibraryCategoryEntity::class  // Add this
        ]
    )
    fun getMediaPagingSource(query: RoomRawQuery): PagingSource<Int, MediaLibraryView>
}