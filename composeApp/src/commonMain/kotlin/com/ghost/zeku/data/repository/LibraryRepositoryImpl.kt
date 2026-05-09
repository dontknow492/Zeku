package com.ghost.zeku.data.repository

import androidx.paging.PagingSource
import androidx.room.RoomRawQuery
import com.ghost.zeku.data.local.room.dao.LibraryDao
import com.ghost.zeku.data.local.room.dao.MediaDao
import com.ghost.zeku.data.local.room.entities.LibraryEntity
import com.ghost.zeku.data.local.room.view.MediaLibraryView
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.repository.LibraryRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow

class LibraryRepositoryImpl(
    private val libraryDao: LibraryDao,
    private val mediaDao: MediaDao
) : LibraryRepository {

    private companion object {
        const val TAG = "LibraryRepo"
    }

    override fun getPagingSource(query: RoomRawQuery): PagingSource<Int, MediaLibraryView> {
        Napier.d(tag = TAG) { "getPagingSource(query)" }
        return mediaDao.getMediaPagingSource(query)
    }

    override fun observeLibrary(): Flow<List<LibraryEntity>> {
        Napier.d(tag = TAG) { "observeLibrary()" }
        return libraryDao.observeAll()
    }

    override fun observeByMediaType(mediaType: MediaType): Flow<List<LibraryEntity>> {
        Napier.d(tag = TAG) { "observeByMediaType(mediaType=$mediaType)" }
        return libraryDao.observeByMediaType(mediaType)
    }

    override fun observeByCategory(categoryId: Long): Flow<List<LibraryEntity>> {
        Napier.d(tag = TAG) { "observeByCategory(categoryId=$categoryId)" }
        return libraryDao.observeByCategory(categoryId)
    }

    override fun observeFavorites(): Flow<List<LibraryEntity>> {
        Napier.d(tag = TAG) { "observeFavorites()" }
        return libraryDao.observeFavorites()
    }

    override fun observeDownloaded(): Flow<List<LibraryEntity>> {
        Napier.d(tag = TAG) { "observeDownloaded()" }
        return libraryDao.observeDownloaded()
    }

    override fun observePinned(): Flow<List<LibraryEntity>> {
        Napier.d(tag = TAG) { "observePinned()" }
        return libraryDao.observePinned()
    }

    override fun observeFiltered(categoryId: Long?, isFavorite: Boolean?): Flow<List<LibraryEntity>> {
        Napier.d(tag = TAG) { "observeFiltered(categoryId=$categoryId, isFavorite=$isFavorite)" }
        return libraryDao.observeFiltered(categoryId, isFavorite)
    }

    override suspend fun getLibraryEntry(mediaId: Int, mediaType: MediaType): LibraryEntity? {
        Napier.d(tag = TAG) { "getLibraryEntry(mediaId=$mediaId, mediaType=$mediaType)" }
        return try {
            libraryDao.getEntry(mediaId, mediaType).also {
                Napier.d(tag = TAG) { "getLibraryEntry -> $it" }
            }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "getLibraryEntry failed" }
            throw e
        }
    }

    override suspend fun exists(mediaId: Int, mediaType: MediaType): Boolean {
        Napier.d(tag = TAG) { "exists(mediaId=$mediaId, mediaType=$mediaType)" }
        return try {
            libraryDao.exists(mediaId, mediaType).also {
                Napier.d(tag = TAG) { "exists -> $it" }
            }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "exists check failed" }
            throw e
        }
    }

    override suspend fun upsert(entry: LibraryEntity) {
        Napier.d(tag = TAG) { "upsert(mediaId=${entry.mediaId}, mediaType=${entry.mediaType})" }
        try {
            libraryDao.upsert(entry)
            Napier.d(tag = TAG) { "upsert success" }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "upsert failed" }
            throw e
        }
    }

    override suspend fun delete(mediaId: Int, mediaType: MediaType) {
        Napier.d(tag = TAG) { "delete(mediaId=$mediaId, mediaType=$mediaType)" }
        try {
            libraryDao.deleteByMedia(mediaId, mediaType)
            Napier.i(tag = TAG) { "Library entry deleted: $mediaId/$mediaType" }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "delete failed" }
            throw e
        }
    }

    override suspend fun toggleFavorite(mediaId: Int, mediaType: MediaType) {
        Napier.d(tag = TAG) { "toggleFavorite(mediaId=$mediaId, mediaType=$mediaType)" }
        try {
            val current = libraryDao.getEntry(mediaId, mediaType)
            val newValue = current?.favorite != true // if null or false -> true, else false
            libraryDao.setFavorite(mediaId, mediaType, newValue, System.currentTimeMillis())
            Napier.i(tag = TAG) { "Favorite toggled to $newValue for $mediaId/$mediaType" }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "toggleFavorite failed" }
            throw e
        }
    }

    override suspend fun togglePinned(mediaId: Int, mediaType: MediaType) {
        Napier.d(tag = TAG) { "togglePinned(mediaId=$mediaId, mediaType=$mediaType)" }
        try {
            val current = libraryDao.getEntry(mediaId, mediaType)
            val newValue = current?.pinned != true
            libraryDao.setPinned(mediaId, mediaType, newValue, System.currentTimeMillis())
            Napier.i(tag = TAG) { "Pinned toggled to $newValue for $mediaId/$mediaType" }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "togglePinned failed" }
            throw e
        }
    }

    override suspend fun setCategory(mediaId: Int, mediaType: MediaType, categoryId: Long) {
        Napier.d(tag = TAG) { "setCategory(mediaId=$mediaId, mediaType=$mediaType, categoryId=$categoryId)" }
        try {
            libraryDao.setCategory(mediaId, mediaType, categoryId, System.currentTimeMillis())
            Napier.i(tag = TAG) { "Category set to $categoryId for $mediaId/$mediaType" }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "setCategory failed" }
            throw e
        }
    }

    override suspend fun markDownloaded(mediaId: Int, mediaType: MediaType, downloadPath: String?) {
        Napier.d(tag = TAG) { "markDownloaded(mediaId=$mediaId, mediaType=$mediaType, path=$downloadPath)" }
        try {
            libraryDao.markDownloaded(mediaId, mediaType, downloadPath, System.currentTimeMillis())
            Napier.i(tag = TAG) { "Downloaded mark success" }
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "markDownloaded failed" }
            throw e
        }
    }
}






