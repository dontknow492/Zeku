package org.ghost.zeku.database.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.ghost.zeku.core.enum.HistoryStatus
import org.ghost.zeku.core.enum.MediaType
import org.ghost.zeku.core.enum.SORTING
import org.ghost.zeku.core.utils.FileUtil
import org.ghost.zeku.database.dao.HistoryDao
import org.ghost.zeku.database.models.HistoryItem
import java.io.File
import javax.inject.Inject

class HistoryRepository @Inject constructor(private val historyDao: HistoryDao) {
    val items: Flow<List<HistoryItem>> = historyDao.getAllHistory()
    val websites: Flow<List<String>> = historyDao.getWebsites()
    val count: Flow<Int> = historyDao.getCount()

    enum class HistorySortType {
        DATE, TITLE, AUTHOR, FILESIZE
    }

    fun getItem(id: Long): HistoryItem {
        return historyDao.getHistoryItem(id)
    }

    fun getAll(): List<HistoryItem> {
        return historyDao.getAllHistoryList()
    }

    fun getAllByURL(url: String): List<HistoryItem> {
        return historyDao.getAllHistoryByURL(url)
    }

    fun getAllByURLAndType(url: String, type: MediaType): List<HistoryItem> {
        return historyDao.getAllHistoryByURLAndType(url, type)
    }

    fun getAllByIDs(ids: List<Long>): List<HistoryItem> {
        return historyDao.getAllHistoryByIDs(ids)
    }

    data class HistoryIDsAndPaths(
        val id: Long,
        val downloadPath: List<String>
    )

    fun getFilteredIDs(
        query: String,
        type: String,
        site: String,
        sortType: HistorySortType,
        sort: SORTING,
        statusFilter: HistoryStatus
    ): List<Long> {
        var filtered = when (sortType) {
            HistorySortType.DATE -> historyDao.getHistoryIDsSortedByID(
                query,
                type,
                site,
                sort.toString()
            )

            HistorySortType.TITLE -> historyDao.getHistoryIDsSortedByTitle(
                query,
                type,
                site,
                sort.toString()
            )

            HistorySortType.AUTHOR -> historyDao.getHistoryIDsSortedByAuthor(
                query,
                type,
                site,
                sort.toString()
            )

            HistorySortType.FILESIZE -> historyDao.getHistoryIDsSortedByFilesize(
                query,
                type,
                site,
                sort.toString()
            )
        }

        when (statusFilter) {
            HistoryStatus.DELETED -> {
                filtered = filtered.filter { it.downloadPath.any { it2 -> !FileUtil.exists(it2) } }
            }

            HistoryStatus.NOT_DELETED -> {
                filtered = filtered.filter { it.downloadPath.any { it2 -> FileUtil.exists(it2) } }
            }

            else -> {}
        }
        return filtered.map { it.id }
    }

    fun getPaginatedSource(
        query: String,
        type: String,
        site: String,
        sortType: HistorySortType,
        sort: SORTING
    ): PagingSource<Int, HistoryItem> {
        val source = when (sortType) {
            HistorySortType.DATE -> historyDao.getHistorySortedByIDPaginated(
                query,
                type,
                site,
                sort.toString()
            )

            HistorySortType.TITLE -> historyDao.getHistorySortedByTitlePaginated(
                query,
                type,
                site,
                sort.toString()
            )

            HistorySortType.AUTHOR -> historyDao.getHistorySortedByAuthorPaginated(
                query,
                type,
                site,
                sort.toString()
            )

            HistorySortType.FILESIZE -> {
                historyDao.getHistorySortedByFilesizePaginated(query, type, site, sort.toString())
            }
        }

        return source
    }


    suspend fun insert(item: HistoryItem) {
        historyDao.insert(item)
    }

    suspend fun delete(item: HistoryItem, deleteFile: Boolean) {
        historyDao.delete(item.id)
        if (deleteFile) {
            item.downloadPath.forEach {
                FileUtil.deleteFile(it)
            }
        }
    }

    suspend fun deleteAll(deleteFile: Boolean = false) {
        if (deleteFile) {
            historyDao.getAllHistoryList().forEach { item ->
                item.downloadPath.forEach {
                    FileUtil.deleteFile(it)
                }
            }
        }
        historyDao.deleteAll()
    }

    suspend fun deleteAllWithIDs(ids: List<Long>, deleteFile: Boolean = false) {
        if (deleteFile) {
            ids.chunked(500).forEach { chunks ->
                historyDao.getAllHistoryByIDs(chunks).forEach { item ->
                    item.downloadPath.forEach {
                        FileUtil.deleteFile(it)
                    }
                }
            }

        }
        ids.chunked(500).forEach { chunks ->
            historyDao.deleteAllByIDs(chunks)
        }

    }

    suspend fun deleteAllWithIDsCheckFiles(ids: List<Long>) {
        val idsToDelete = mutableListOf<Long>()
        ids.chunked(500).forEach { chunks ->
            historyDao.getAllHistoryByIDs(chunks).forEach { item ->
                val filesNotPresent =
                    item.downloadPath.all { !File(it).exists() && it.isNotBlank() }
                if (filesNotPresent) {
                    idsToDelete.add(item.id)
                }
            }
        }

        if (idsToDelete.isNotEmpty()) {
            idsToDelete.chunked(500).forEach { chunked ->
                historyDao.deleteAllByIDs(chunked)
            }

        }
    }

    data class HistoryItemDownloadPaths(
        val downloadPath: List<String>
    )

    fun getDownloadPathsFromIDs(ids: List<Long>): List<List<String>> {
        val res: MutableList<List<String>> = mutableListOf()
        ids.chunked(500).forEach { chunks ->
            val tmp = historyDao.getDownloadPathsFromIDs(chunks)
            res.addAll(tmp.map { it.downloadPath })
        }
        return res
    }

    suspend fun deleteDuplicates() {
        historyDao.deleteDuplicates()
    }

    suspend fun update(item: HistoryItem) {
        historyDao.update(item)
    }

    suspend fun clearDeletedHistory() {
        items.collectLatest {
            it.forEach { item ->
                if (item.downloadPath.all { path -> !FileUtil.exists(path) }) {
                    historyDao.delete(item.id)
                }
            }
        }
    }

}