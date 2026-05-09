package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import com.ghost.zeku.domain.model.media.DownloadState
import com.ghost.zeku.domain.model.ProviderType

@Entity(
    tableName = "chapters",
    primaryKeys = ["id", "provider"],
    indices = [
        Index(value = ["mediaId", "provider"]),
        Index(value = ["number"])
    ]
)
data class ChapterEntity(
    val id: String,
    val mediaId: Int,
    val provider: ProviderType,

    val number: Float,
    val title: String?,
    val volume: Int?,

    // LOCAL APP STATE
    val isRead: Boolean = false,
    val lastReadPage: Int = 0,

    val downloadStatus: DownloadState = DownloadState.NONE,
    val localFolderPath: String? = null // Path to the folder containing the downloaded chapter images
)

