package org.ghost.zeku.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ghost.zeku.core.enum.MediaType

@Entity(tableName = "logs")
data class LogItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var title: String,
    var content: String,
    var format: Format,
    var downloadType: MediaType,
    var downloadTime: Long,
)


