package com.ghost.zeku.domain.repository


import androidx.paging.PagingSource
import androidx.room.RoomRawQuery
import com.ghost.zeku.data.local.room.entities.LibraryEntity
import com.ghost.zeku.data.local.room.view.MediaLibraryView
import com.ghost.zeku.domain.model.media.MediaType
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    // ── Paging (dynamic filter/sort) ──────────────
    fun getPagingSource(query: RoomRawQuery): PagingSource<Int, MediaLibraryView>

    // ── Reactive streams ──────────────────────────
    fun observeLibrary(): Flow<List<LibraryEntity>>
    fun observeByMediaType(mediaType: MediaType): Flow<List<LibraryEntity>>
    fun observeByCategory(categoryId: Long): Flow<List<LibraryEntity>>
    fun observeFavorites(): Flow<List<LibraryEntity>>
    fun observeDownloaded(): Flow<List<LibraryEntity>>
    fun observePinned(): Flow<List<LibraryEntity>>
    fun observeFiltered(categoryId: Long?, isFavorite: Boolean?): Flow<List<LibraryEntity>>

    // ── One‑shot lookups ──────────────────────────
    suspend fun getLibraryEntry(mediaId: Int, mediaType: MediaType): LibraryEntity?
    suspend fun exists(mediaId: Int, mediaType: MediaType): Boolean

    // ── Write ─────────────────────────────────────
    suspend fun upsert(entry: LibraryEntity)
    suspend fun delete(mediaId: Int, mediaType: MediaType)

    // ── Convenience partial updates ───────────────
    suspend fun toggleFavorite(mediaId: Int, mediaType: MediaType)
    suspend fun togglePinned(mediaId: Int, mediaType: MediaType)
    suspend fun setCategory(mediaId: Int, mediaType: MediaType, categoryId: Long)
    suspend fun markDownloaded(mediaId: Int, mediaType: MediaType, downloadPath: String?)
}









