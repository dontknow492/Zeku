package com.ghost.zeku.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ghost.zeku.domain.model.media.MediaType

@Entity(
    tableName = "library_categories",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["type", "name"], unique = true)
    ]
)
data class LibraryCategoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val type: MediaType?,

    val displayName: String? = null,

    val description: String? = null,

    val color: String? = null,

    val icon: String? = null,

    @ColumnInfo(name = "sort_order", defaultValue = "0")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "is_default", defaultValue = "0")
    val isDefault: Boolean = false,

    @ColumnInfo(name = "is_visible", defaultValue = "1")
    val isVisible: Boolean = true,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)