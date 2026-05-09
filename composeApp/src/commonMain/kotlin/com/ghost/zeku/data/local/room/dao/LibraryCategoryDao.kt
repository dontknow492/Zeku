package com.ghost.zeku.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity
import com.ghost.zeku.data.local.room.item.CategoryWithCount
import com.ghost.zeku.domain.model.media.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryCategoryDao {

    // ────────────────────────────────────────────
    // OBSERVE (Reactive)
    // ────────────────────────────────────────────

    @Query("SELECT * FROM library_categories ORDER BY sort_order ASC, name ASC")
    fun observeAll(): Flow<List<LibraryCategoryEntity>>

    // Categories that match the given type OR are global (type IS NULL)
    @Query(
        """
        SELECT * FROM library_categories
        WHERE type = :type OR type IS NULL
        ORDER BY sort_order ASC, name ASC
    """
    )
    fun observeByType(type: MediaType): Flow<List<LibraryCategoryEntity>>

    @Query(
        """
        SELECT * FROM library_categories
        WHERE is_visible = 1 AND (type = :type OR type IS NULL)
        ORDER BY sort_order ASC, name ASC
    """
    )
    fun observeVisibleByType(type: MediaType): Flow<List<LibraryCategoryEntity>>

    @Query("SELECT * FROM library_categories WHERE is_default = 1")
    fun observeDefault(): Flow<List<LibraryCategoryEntity>>

    // ────────────────────────────────────────────
    // GET (one‑shot)
    // ────────────────────────────────────────────

    @Query("SELECT * FROM library_categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LibraryCategoryEntity?

    @Query(
        """
        SELECT * FROM library_categories
        WHERE name = :name AND (type = :type OR type IS NULL)
        LIMIT 1
    """
    )
    suspend fun getByName(name: String, type: MediaType): LibraryCategoryEntity?

    // Better flexible: prefer type‑specific, fallback to global
    @Query(
        """
        SELECT * FROM library_categories
        WHERE name = :name AND (type = :type OR type IS NULL)
        ORDER BY CASE WHEN type = :type THEN 0 ELSE 1 END
        LIMIT 1
    """
    )
    suspend fun findBestMatch(name: String, type: MediaType): LibraryCategoryEntity?

    // Category with item count (join with library table)
    @Query(
        """
        SELECT c.*, COUNT(l.mediaId) AS item_count
        FROM library_categories c
        LEFT JOIN library l ON c.id = l.categoryId
        GROUP BY c.id
        ORDER BY c.sort_order ASC, c.name ASC
    """
    )
    fun observeCategoriesWithCount(): Flow<List<CategoryWithCount>>

    // ────────────────────────────────────────────
    // WRITE
    // ────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: LibraryCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<LibraryCategoryEntity>)

    @Update
    suspend fun update(category: LibraryCategoryEntity)

    @Delete
    suspend fun delete(category: LibraryCategoryEntity)

    @Query("DELETE FROM library_categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        DELETE FROM library_categories
        WHERE name = :name AND (type = :type OR type IS NULL)
    """
    )
    suspend fun deleteByName(name: String, type: MediaType)

    // ────────────────────────────────────────────
    // HELPERS
    // ────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM library_categories")
    suspend fun count(): Int

    @Query("UPDATE library_categories SET is_visible = :visible WHERE id = :id")
    suspend fun setVisibility(id: Long, visible: Boolean)

    @Query("UPDATE library_categories SET sort_order = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Long, order: Int)
}