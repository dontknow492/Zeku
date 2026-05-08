package com.ghost.zeku.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.LibraryEntity
import com.ghost.zeku.domain.model.enum.MediaType
import kotlinx.coroutines.flow.Flow


@Dao
interface LibraryDao {

    // ------------------------------------------------------------------------
    // Observers
    // ------------------------------------------------------------------------

    @Query(
        """
        SELECT * FROM library
        ORDER BY pinned DESC, updatedAt DESC
    """
    )
    fun observeLibrary(): Flow<List<LibraryEntity>>

    @Query(
        """
        SELECT * FROM library
        WHERE mediaType = :mediaType
        ORDER BY pinned DESC, updatedAt DESC
    """
    )
    fun observeLibraryByType(
        mediaType: MediaType
    ): Flow<List<LibraryEntity>>

    @Query(
        """
        SELECT * FROM library
        WHERE category = :category
        ORDER BY pinned DESC, updatedAt DESC
    """
    )
    fun observeLibraryByCategory(
        category: String
    ): Flow<List<LibraryEntity>>

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
    fun observeVisibleLibrary(): Flow<List<LibraryEntity>>

    // ------------------------------------------------------------------------
    // Single Entry
    // ------------------------------------------------------------------------

    @Query(
        """
        SELECT * FROM library
        WHERE mediaId = :mediaId
        AND mediaType = :mediaType
        LIMIT 1
    """
    )
    suspend fun getLibraryEntry(
        mediaId: Int,
        mediaType: MediaType
    ): LibraryEntity?

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM library
            WHERE mediaId = :mediaId
            AND mediaType = :mediaType
        )
    """
    )
    suspend fun exists(
        mediaId: Int,
        mediaType: MediaType
    ): Boolean

    // ------------------------------------------------------------------------
    // Writes
    // ------------------------------------------------------------------------

    @Upsert
    suspend fun upsert(entry: LibraryEntity)

    @Delete
    suspend fun delete(entry: LibraryEntity)

    @Query(
        """
        DELETE FROM library
        WHERE mediaId = :mediaId
        AND mediaType = :mediaType
    """
    )
    suspend fun deleteByMedia(
        mediaId: Int,
        mediaType: MediaType
    )

    @Query("DELETE FROM library")
    suspend fun clear()
}








