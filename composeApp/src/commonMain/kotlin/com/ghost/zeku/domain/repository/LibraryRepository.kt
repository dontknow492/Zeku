package com.ghost.zeku.domain.repository

import com.ghost.zeku.data.local.room.entities.LibraryEntity
import com.ghost.zeku.domain.model.enum.MediaType
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    // ------------------------------------------------------------------------
    // Observers
    // ------------------------------------------------------------------------

    fun observeLibrary(): Flow<List<LibraryEntity>>

    fun observeLibraryByType(
        mediaType: MediaType
    ): Flow<List<LibraryEntity>>

    fun observeLibraryByCategory(
        category: String
    ): Flow<List<LibraryEntity>>

    fun observeFavorites(): Flow<List<LibraryEntity>>

    fun observeVisibleLibrary(): Flow<List<LibraryEntity>>

    // ------------------------------------------------------------------------
    // Single Entry
    // ------------------------------------------------------------------------

    suspend fun getLibraryEntry(
        mediaId: Int,
        mediaType: MediaType
    ): LibraryEntity?

    suspend fun exists(
        mediaId: Int,
        mediaType: MediaType
    ): Boolean

    // ------------------------------------------------------------------------
    // Mutations
    // ------------------------------------------------------------------------

    suspend fun addToLibrary(
        mediaId: Int,
        mediaType: MediaType,
        category: String = DEFAULT_CATEGORY
    )

    suspend fun removeFromLibrary(
        mediaId: Int,
        mediaType: MediaType
    )

    suspend fun updateCategory(
        mediaId: Int,
        mediaType: MediaType,
        category: String
    )

    suspend fun setFavorite(
        mediaId: Int,
        mediaType: MediaType,
        favorite: Boolean
    )

    suspend fun setPinned(
        mediaId: Int,
        mediaType: MediaType,
        pinned: Boolean
    )

    suspend fun setHidden(
        mediaId: Int,
        mediaType: MediaType,
        hidden: Boolean
    )

    suspend fun setDownloaded(
        mediaId: Int,
        mediaType: MediaType,
        downloaded: Boolean
    )

    suspend fun updateNotes(
        mediaId: Int,
        mediaType: MediaType,
        notes: String?
    )

    suspend fun updateCustomTitle(
        mediaId: Int,
        mediaType: MediaType,
        title: String?
    )

    suspend fun clear()

    companion object {
        const val DEFAULT_CATEGORY = "default"
    }
}