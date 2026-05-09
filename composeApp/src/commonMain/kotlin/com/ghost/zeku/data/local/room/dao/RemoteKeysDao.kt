package com.ghost.zeku.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.ChapterRemoteKeys
import com.ghost.zeku.data.local.room.entities.EpisodeRemoteKeys
import com.ghost.zeku.data.local.room.entities.MediaRemoteKeys
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.ProviderType

/**
 * A unified DAO for managing the pagination and category links.
 * Keeping them in one DAO reduces boilerplate in your AppDatabase.
 */
@Dao
interface RemoteKeysDao {

    // =========================================================
    // MEDIA KEYS
    // =========================================================

    @Upsert
    suspend fun upsertMediaKeys(
        keys: List<MediaRemoteKeys>
    )

    @Query(
        """
        SELECT * FROM media_remote_keys
        
        WHERE mediaId = :id
        AND provider = :provider
        AND mediaType = :mediaType
        AND category = :category
        """
    )
    suspend fun getMediaRemoteKey(
        id: Int,
        provider: ProviderType,
        mediaType: MediaType,
        category: String
    ): MediaRemoteKeys?

    @Query(
        """
        DELETE FROM media_remote_keys
        
        WHERE provider = :provider
        AND mediaType = :mediaType
        AND category = :category
        """
    )
    suspend fun clearMediaKeys(
        provider: ProviderType,
        mediaType: MediaType,
        category: String
    )

    @Query(
        """
        SELECT lastUpdated
        
        FROM media_remote_keys
        
        WHERE provider = :provider
        AND mediaType = :mediaType
        AND category = :category
        
        LIMIT 1
        """
    )
    suspend fun getMediaLastUpdated(
        provider: ProviderType,
        mediaType: MediaType,
        category: String
    ): Long?

    // =========================================================
    // EPISODE KEYS
    // =========================================================

    @Upsert
    suspend fun upsertEpisodeKeys(
        keys: List<EpisodeRemoteKeys>
    )

    @Query(
        """
        SELECT * FROM episode_remote_keys
        
        WHERE id = :id
        AND provider = :source
        """
    )
    suspend fun getEpisodeRemoteKey(
        id: String,
        source: ProviderType
    ): EpisodeRemoteKeys?

    @Query(
        """
        DELETE FROM episode_remote_keys
        
        WHERE mediaId = :mediaId
        AND provider = :source
        """
    )
    suspend fun clearEpisodeKeysByMedia(
        mediaId: Int,
        source: ProviderType
    )

    // =========================================================
    // CHAPTER KEYS
    // =========================================================

    @Upsert
    suspend fun upsertChapterKeys(
        keys: List<ChapterRemoteKeys>
    )

    @Query(
        """
        SELECT * FROM chapter_remote_keys
        
        WHERE id = :id
        AND provider = :source
        """
    )
    suspend fun getChapterRemoteKey(
        id: String,
        source: ProviderType
    ): ChapterRemoteKeys?

    @Query(
        """
        DELETE FROM chapter_remote_keys
        
        WHERE mediaId = :mediaId
        AND provider = :source
        """
    )
    suspend fun clearChapterKeysByMedia(
        mediaId: Int,
        source: ProviderType
    )

    // =========================================================
    // GLOBAL CLEANUP
    // =========================================================

    @Query(
        """
        DELETE FROM media_remote_keys
        WHERE provider = :provider
        """
    )
    suspend fun clearAllMediaKeysByProvider(
        provider: ProviderType
    )

    @Query(
        """
        DELETE FROM media_remote_keys
        
        WHERE provider = :provider
        AND mediaType = :mediaType
        """
    )
    suspend fun clearMediaKeysByType(
        provider: ProviderType,
        mediaType: MediaType
    )
}