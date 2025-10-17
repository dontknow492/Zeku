package org.ghost.zeku.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ghost.zeku.core.enum.MediaType

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    val url: String,
    val title: String,
    val author: String,
    val duration: String,
    val thumb: String,
    val type: MediaType,
    val time: Long,
    val downloadPath: List<String>,
    val website: String,
    val format: Format,
    @ColumnInfo(defaultValue = "0")
    val filesize: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val downloadId: Long,
    @ColumnInfo(defaultValue = "")
    val command: String = ""
)