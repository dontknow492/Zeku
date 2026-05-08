package com.ghost.zeku.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.MediaDetailsEntity
import com.ghost.zeku.data.local.room.entities.MediaEntity
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
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
}