package com.ghost.zeku.data.local.room

import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity

object DefaultCategories {
    val SYSTEM_CATEGORIES = listOf(
        LibraryCategoryEntity(
            name = "default",
            type = null,                // global – applies to all media types
            displayName = "Default",
            description = "Default category for all items",
            color = "#607D8B",
            icon = "ic_folder",
            sortOrder = 0,
            isDefault = true,
            isVisible = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        LibraryCategoryEntity(
            name = "favorites",
            type = null,
            displayName = "Favorites",
            description = "Your favorite items",
            color = "#FF4081",
            icon = "ic_favorite",
            sortOrder = 1,
            isDefault = true,
            isVisible = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        LibraryCategoryEntity(
            name = "watch_later",
            type = null,
            displayName = "Watch Later",
            description = "Items to watch later",
            color = "#FF9800",
            icon = "ic_schedule",
            sortOrder = 2,
            isDefault = true,
            isVisible = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
    )
}