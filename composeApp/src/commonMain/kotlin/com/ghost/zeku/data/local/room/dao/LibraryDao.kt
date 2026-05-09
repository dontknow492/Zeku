package com.ghost.zeku.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.LibraryEntity
import com.ghost.zeku.domain.model.media.MediaType
import kotlinx.coroutines.flow.Flow


@Dao
interface LibraryDao {

    // ────────────────────────────────────────────
    // OBSERVE
    // ────────────────────────────────────────────

    @Query(
        """
        SELECT * FROM library
        ORDER BY pinned DESC, updatedAt DESC
    """
    )
    fun observeAll(): Flow<List<LibraryEntity>>

    @Query(
        """
        SELECT * FROM library
        WHERE mediaType = :mediaType
        ORDER BY pinned DESC, updatedAt DESC
    """
    )
    fun observeByMediaType(mediaType: MediaType): Flow<List<LibraryEntity>>

    // By categoryId
    @Query(
        """
        SELECT * FROM library
        WHERE categoryId = :categoryId
        ORDER BY pinned DESC, updatedAt DESC
    """
    )
    fun observeByCategory(categoryId: Long): Flow<List<LibraryEntity>>

    @Query(
        """
        SELECT * FROM library
        WHERE favorite = 1
        ORDER BY updatedAt DESC
    """
    )
    fun observeFavorites(): Flow<List<LibraryEntity>>

    @Query(
        """
        SELECT * FROM library
        WHERE hidden = 0
        ORDER BY pinned DESC, updatedAt DESC
    """
    )
    fun observeVisible(): Flow<List<LibraryEntity>>

    @Query(
        """
        SELECT * FROM library
        WHERE downloaded = 1
        ORDER BY updatedAt DESC
    """
    )
    fun observeDownloaded(): Flow<List<LibraryEntity>>

    @Query(
        """
        SELECT * FROM library
        WHERE pinned = 1
        ORDER BY updatedAt DESC
    """
    )
    fun observePinned(): Flow<List<LibraryEntity>>

    // Combined filters (example)
    @Query(
        """
        SELECT * FROM library
        WHERE (categoryId = :categoryId OR :categoryId IS NULL)
        AND (favorite = :favorite OR :favorite IS NULL)
        ORDER BY pinned DESC, updatedAt DESC
    """
    )
    fun observeFiltered(
        categoryId: Long?,
        favorite: Boolean?
    ): Flow<List<LibraryEntity>>

    // ────────────────────────────────────────────
    // SINGLE ENTRY
    // ────────────────────────────────────────────

    @Query(
        """
        SELECT * FROM library
        WHERE mediaId = :mediaId AND mediaType = :mediaType
        LIMIT 1
    """
    )
    suspend fun getEntry(mediaId: Int, mediaType: MediaType): LibraryEntity?

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM library
            WHERE mediaId = :mediaId AND mediaType = :mediaType
        )
    """
    )
    suspend fun exists(mediaId: Int, mediaType: MediaType): Boolean

    // ────────────────────────────────────────────
    // WRITE
    // ────────────────────────────────────────────

    @Upsert
    suspend fun upsert(entry: LibraryEntity)

    @Delete
    suspend fun delete(entry: LibraryEntity)

    @Query(
        """
        DELETE FROM library
        WHERE mediaId = :mediaId AND mediaType = :mediaType
    """
    )
    suspend fun deleteByMedia(mediaId: Int, mediaType: MediaType)

    @Query("DELETE FROM library")
    suspend fun clear()

    // ────────────────────────────────────────────
    // PARTIAL UPDATES (convenience)
    // ────────────────────────────────────────────

    @Query(
        """
        UPDATE library SET favorite = :favorite, updatedAt = :now
        WHERE mediaId = :mediaId AND mediaType = :mediaType
    """
    )
    suspend fun setFavorite(
        mediaId: Int,
        mediaType: MediaType,
        favorite: Boolean,
        now: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE library SET pinned = :pinned, updatedAt = :now
        WHERE mediaId = :mediaId AND mediaType = :mediaType
    """
    )
    suspend fun setPinned(mediaId: Int, mediaType: MediaType, pinned: Boolean, now: Long = System.currentTimeMillis())

    @Query(
        """
        UPDATE library SET categoryId = :categoryId, updatedAt = :now
        WHERE mediaId = :mediaId AND mediaType = :mediaType
    """
    )
    suspend fun setCategory(
        mediaId: Int,
        mediaType: MediaType,
        categoryId: Long,
        now: Long = System.currentTimeMillis()
    )


    // Inside LibraryDao
    @Query(
        """
    UPDATE library 
    SET downloaded = 1, downloadPath = :path, updatedAt = :now
    WHERE mediaId = :mediaId AND mediaType = :mediaType
"""
    )
    suspend fun markDownloaded(
        mediaId: Int,
        mediaType: MediaType,
        path: String?,
        now: Long = System.currentTimeMillis()
    )
}








