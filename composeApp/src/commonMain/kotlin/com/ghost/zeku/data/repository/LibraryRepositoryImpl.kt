package com.ghost.zeku.data.repository

import com.ghost.zeku.data.local.room.dao.LibraryDao
import com.ghost.zeku.data.local.room.entities.LibraryEntity
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow


class LibraryRepositoryImpl(
    private val libraryDao: LibraryDao
) : LibraryRepository {

    // ------------------------------------------------------------------------
    // Observers
    // ------------------------------------------------------------------------

    override fun observeLibrary(): Flow<List<LibraryEntity>> {
        return libraryDao.observeLibrary()
    }

    override fun observeLibraryByType(
        mediaType: MediaType
    ): Flow<List<LibraryEntity>> {
        return libraryDao.observeLibraryByType(mediaType)
    }

    override fun observeLibraryByCategory(
        category: String
    ): Flow<List<LibraryEntity>> {
        return libraryDao.observeLibraryByCategory(category)
    }

    override fun observeFavorites(): Flow<List<LibraryEntity>> {
        return libraryDao.observeFavorites()
    }

    override fun observeVisibleLibrary(): Flow<List<LibraryEntity>> {
        return libraryDao.observeVisibleLibrary()
    }

    // ------------------------------------------------------------------------
    // Single Entry
    // ------------------------------------------------------------------------

    override suspend fun getLibraryEntry(
        mediaId: Int,
        mediaType: MediaType
    ): LibraryEntity? {
        return libraryDao.getLibraryEntry(
            mediaId = mediaId,
            mediaType = mediaType
        )
    }

    override suspend fun exists(
        mediaId: Int,
        mediaType: MediaType
    ): Boolean {
        return libraryDao.exists(
            mediaId = mediaId,
            mediaType = mediaType
        )
    }

    // ------------------------------------------------------------------------
    // Mutations
    // ------------------------------------------------------------------------

    override suspend fun addToLibrary(
        mediaId: Int,
        mediaType: MediaType,
        category: String
    ) {
        val existing = libraryDao.getLibraryEntry(
            mediaId = mediaId,
            mediaType = mediaType
        )

        if (existing != null) {
            libraryDao.upsert(
                existing.copy(
                    category = category,
                    updatedAt = System.currentTimeMillis()
                )
            )
            return
        }

        libraryDao.upsert(
            LibraryEntity(
                mediaId = mediaId,
                mediaType = mediaType,
                category = category
            )
        )
    }

    override suspend fun removeFromLibrary(
        mediaId: Int,
        mediaType: MediaType
    ) {
        libraryDao.deleteByMedia(
            mediaId = mediaId,
            mediaType = mediaType
        )
    }

    override suspend fun updateCategory(
        mediaId: Int,
        mediaType: MediaType,
        category: String
    ) {
        updateEntry(mediaId, mediaType) {
            LibraryEntity(
                mediaId = mediaId,
                mediaType = mediaType,
                category = category,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun setFavorite(
        mediaId: Int,
        mediaType: MediaType,
        favorite: Boolean
    ) {
        updateEntry(mediaId, mediaType) {
            LibraryEntity(
                mediaId = mediaId,
                mediaType = mediaType,
                favorite = favorite,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun setPinned(
        mediaId: Int,
        mediaType: MediaType,
        pinned: Boolean
    ) {
        updateEntry(mediaId, mediaType) {
            LibraryEntity(
                mediaId = mediaId,
                mediaType = mediaType,
                pinned = pinned,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun setHidden(
        mediaId: Int,
        mediaType: MediaType,
        hidden: Boolean
    ) {
        updateEntry(mediaId, mediaType) {
            LibraryEntity(
                mediaId = mediaId,
                mediaType = mediaType,
                hidden = hidden,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun setDownloaded(
        mediaId: Int,
        mediaType: MediaType,
        downloaded: Boolean
    ) {
        updateEntry(mediaId, mediaType) {
            LibraryEntity(
                mediaId = mediaId,
                mediaType = mediaType,
                downloaded = downloaded,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun updateNotes(
        mediaId: Int,
        mediaType: MediaType,
        notes: String?
    ) {
        updateEntry(mediaId, mediaType) {
            LibraryEntity(
                mediaId = mediaId,
                mediaType = mediaType,
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun updateCustomTitle(
        mediaId: Int,
        mediaType: MediaType,
        title: String?
    ) {
        updateEntry(mediaId, mediaType) {
            LibraryEntity(
                mediaId = mediaId,
                mediaType = mediaType,
                customTitle = title,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun clear() {
        libraryDao.clear()
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private suspend inline fun updateEntry(
        mediaId: Int,
        mediaType: MediaType,
        update: LibraryEntity.() -> LibraryEntity
    ) {
        val existing = libraryDao.getLibraryEntry(
            mediaId = mediaId,
            mediaType = mediaType
        ) ?: return

        libraryDao.upsert(existing.update())
    }
}