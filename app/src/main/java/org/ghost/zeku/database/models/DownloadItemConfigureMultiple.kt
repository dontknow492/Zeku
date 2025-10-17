package org.ghost.zeku.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ghost.zeku.core.enum.MediaType

@Entity(tableName = "downloads")
data class DownloadItemConfigureMultiple(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var url: String,
    var title: String,
    var thumb: String,
    var duration: String,
    var container: String,
    var format: Format,
    var allFormats: MutableList<Format>,
    var audioPreferences: AudioPreferences,
    var videoPreferences: VideoPreferences,
    @ColumnInfo(defaultValue = "Queued")
    var status: String,
    var type: MediaType,
    var incognito: Boolean = false
)