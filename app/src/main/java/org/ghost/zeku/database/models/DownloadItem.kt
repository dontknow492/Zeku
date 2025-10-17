package org.ghost.zeku.database.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.ghost.zeku.core.enum.MediaType
import org.ghost.zeku.core.enum.Status


@Entity(tableName = "downloads")
@Parcelize
data class DownloadItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var url: String,
    var title: String,
    var author: String,
    var thumb: String,
    var duration: String,
    var type: MediaType,
    var format: Format,
    @ColumnInfo(defaultValue = "Default")
    var container: String,
    @ColumnInfo(defaultValue = "")
    var downloadSections: String,
    var allFormats: MutableList<Format>,
    var downloadPath: String,
    var website: String,
    val downloadSize: String,
    var playlistTitle: String,
    val audioPreferences: AudioPreferences,
    val videoPreferences: VideoPreferences,
    @ColumnInfo(defaultValue = "")
    var extraCommands: String,
    var customFileNameTemplate: String,
    var saveThumb: Boolean,
    @ColumnInfo(defaultValue = "Queued")
    var status: Status,
    @ColumnInfo(defaultValue = "0")
    var downloadStartTime: Long,
    var logID: Long?,
    @ColumnInfo(defaultValue = "")
    var playlistURL: String? = "",
    @ColumnInfo(defaultValue = "")
    var playlistIndex: Int? = null,
    @ColumnInfo(defaultValue = "0")
    var incognito: Boolean = false,
    @ColumnInfo(defaultValue = "[]")
    var availableSubtitles: List<String> = listOf(),
    var rowNumber: Int = 0
) : Parcelable


