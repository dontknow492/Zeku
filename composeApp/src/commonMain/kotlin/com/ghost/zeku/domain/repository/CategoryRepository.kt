package com.ghost.zeku.domain.repository

import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity
import com.ghost.zeku.data.local.room.item.CategoryWithCount
import com.ghost.zeku.domain.model.media.MediaType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    // ── Reactive streams ──────────────────────────
    fun observeAll(): Flow<List<LibraryCategoryEntity>>
    fun observeVisible(mediaType: MediaType): Flow<List<LibraryCategoryEntity>>
    fun observeDefault(): Flow<List<LibraryCategoryEntity>>
    fun observeWithItemCount(): Flow<List<CategoryWithCount>>   // category + item count

    // ── One‑shot lookups ──────────────────────────
    suspend fun getById(id: Long): LibraryCategoryEntity?
    suspend fun getByName(name: String, type: MediaType): LibraryCategoryEntity?
    suspend fun findBestMatch(
        name: String,
        type: MediaType
    ): LibraryCategoryEntity?  // type‑specific first, then global

    // ── Write ─────────────────────────────────────
    suspend fun create(category: LibraryCategoryEntity): Long   // returns id
    suspend fun update(category: LibraryCategoryEntity)
    suspend fun delete(categoryId: Long)

    // ── System helpers ────────────────────────────
    suspend fun ensureDefaultsExist()   // inserts the default categories if table is empty
    suspend fun setVisibility(id: Long, visible: Boolean)
    suspend fun updateSortOrder(id: Long, order: Int)
    suspend fun count(): Int
}