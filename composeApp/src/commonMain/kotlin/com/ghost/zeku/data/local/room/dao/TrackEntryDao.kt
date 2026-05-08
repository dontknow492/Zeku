package com.ghost.zeku.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.TrackEntryEntity
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.enum.TrackStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackEntryDao {

    // ------------------------------------------------------------------------
    // Observers
    // ------------------------------------------------------------------------

    @Query(
        """
        SELECT * FROM track_entries
        WHERE provider = :provider
        ORDER BY updatedAt DESC
    """
    )
    fun observeEntries(
        provider: ProviderType
    ): Flow<List<TrackEntryEntity>>

    @Query(
        """
        SELECT * FROM track_entries
        WHERE provider = :provider
        AND mediaType = :mediaType
        ORDER BY updatedAt DESC
    """
    )
    fun observeEntriesByType(
        provider: ProviderType,
        mediaType: MediaType
    ): Flow<List<TrackEntryEntity>>

    @Query(
        """
        SELECT * FROM track_entries
        WHERE provider = :provider
        AND status = :status
        ORDER BY updatedAt DESC
    """
    )
    fun observeEntriesByStatus(
        provider: ProviderType,
        status: TrackStatus
    ): Flow<List<TrackEntryEntity>>

    // ------------------------------------------------------------------------
    // Single Entry
    // ------------------------------------------------------------------------

    @Query(
        """
        SELECT * FROM track_entries
        WHERE mediaId = :mediaId
        AND mediaType = :mediaType
        AND provider = :provider
        LIMIT 1
    """
    )
    suspend fun getEntry(
        mediaId: Int,
        mediaType: MediaType,
        provider: ProviderType
    ): TrackEntryEntity?

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM track_entries
            WHERE mediaId = :mediaId
            AND mediaType = :mediaType
            AND provider = :provider
        )
    """
    )
    suspend fun exists(
        mediaId: Int,
        mediaType: MediaType,
        provider: ProviderType
    ): Boolean

    // ------------------------------------------------------------------------
    // Writes
    // ------------------------------------------------------------------------

    @Upsert
    suspend fun upsert(entry: TrackEntryEntity)

    @Delete
    suspend fun delete(entry: TrackEntryEntity)

    @Query(
        """
        DELETE FROM track_entries
        WHERE mediaId = :mediaId
        AND mediaType = :mediaType
        AND provider = :provider
    """
    )
    suspend fun deleteByMedia(
        mediaId: Int,
        mediaType: MediaType,
        provider: ProviderType
    )

    @Query(
        """
        DELETE FROM track_entries
        WHERE provider = :provider
    """
    )
    suspend fun clearProvider(
        provider: ProviderType
    )
}