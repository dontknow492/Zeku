package com.ghost.zeku.data.local.room.entities

import androidx.room.*
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.ProviderType

@Entity(
    tableName = "library",
    indices = [
        Index(value = ["mediaId", "mediaType", "provider"], unique = true),
        Index(value = ["categoryId"]),
        Index(value = ["favorite", "addedAt"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = LibraryCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LibraryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val mediaId: Int,
    val mediaType: MediaType,
    val provider: ProviderType,

    val categoryId: Long,

    val favorite: Boolean = false,
    val pinned: Boolean = false,
    val hidden: Boolean = false,
    val downloaded: Boolean = false,

    val downloadPath: String? = null,
    val fileSize: Long? = null,

    val customTitle: String? = null,
    val customDescription: String? = null,

    val notes: String? = null,
    val rating: Float? = null,

    val watchCount: Int = 0,
    val lastWatchedAt: Long? = null,

    val tags: List<String> = emptyList(),

    val customThumbnailPath: String? = null,

    val addedAt: Long,
    val updatedAt: Long,

    val syncedAt: Long? = null,
    val isSynced: Boolean = true
)




