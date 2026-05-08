package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ghost.zeku.domain.model.enum.MediaType

@Entity(
    tableName = "library",
    indices = [
        Index(
            value = ["mediaId", "mediaType"],
            unique = true
        )
    ]
)
data class LibraryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val mediaId: Int,

    val mediaType: MediaType,

    /**
     * Local/custom categories
     */
    val category: String = "default",

    val favorite: Boolean = false,

    val pinned: Boolean = false,

    val hidden: Boolean = false,

    val downloaded: Boolean = false,

    val customTitle: String? = null,

    val notes: String? = null,

    val addedAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)