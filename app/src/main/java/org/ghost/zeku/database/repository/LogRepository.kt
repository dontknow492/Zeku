package org.ghost.zeku.database.repository

import kotlinx.coroutines.flow.Flow
import org.ghost.zeku.database.dao.LogDao
import org.ghost.zeku.database.models.LogItem
import javax.inject.Inject


class LogRepository @Inject constructor(private val logDao: LogDao) {
    val items: Flow<List<LogItem>> = logDao.getAllLogsFlow()

    fun getAll(): List<LogItem> {
        return logDao.getAllLogs()
    }

    fun getLogFlowByID(id: Long): Flow<LogItem> {
        return logDao.getLogFlowByID(id)
    }

    fun getLogByID(id: Long): LogItem? {
        return logDao.getByID(id)
    }


    suspend fun insert(item: LogItem): Long {
        return logDao.insert(item)
    }

    suspend fun delete(item: LogItem) {
        logDao.delete(item.id)
    }


    suspend fun deleteAll() {
        logDao.deleteAll()
    }

    fun getItem(id: Long): LogItem {
        return logDao.getByID(id)
    }

    suspend fun update(line: String, id: Long, resetLog: Boolean = false) {
        kotlin.runCatching {
            logDao.updateLog(line, id, resetLog)
        }
    }

}