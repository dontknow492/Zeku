package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import com.ghost.zeku.domain.model.enum.ProviderType

/**
 * This table acts as the Link between a specific Anime and a Category (e.g. Trending).
 * The composite primary key allows the SAME anime to exist in multiple
 * categories without duplicating the actual anime data.
 */
@Entity(
    tableName = "anime_remote_keys",
    primaryKeys = ["id", "source", "category"]
)
data class AnimeRemoteKeys(
    val id: Int,
    val source: ProviderType,

    // The specific list this key belongs to (e.g., "TRENDING", "POPULAR", "FAVORITES")
    val category: String,

    // Stores the exact index/position from the API to maintain sorting in the local database
    val sortOrder: Int,

    // Pagination pointers
    val prevPage: Int?,
    val nextPage: Int?,
    val lastUpdated: Long = System.currentTimeMillis()
)


