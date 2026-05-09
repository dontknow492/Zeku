package com.ghost.zeku.data.local.room.item

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity

data class CategoryWithCount(
    @Embedded val category: LibraryCategoryEntity,

    @ColumnInfo(name = "item_count")
    val itemCount: Int
)