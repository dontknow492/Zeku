package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import com.ghost.zeku.domain.model.enum.ProviderType

/**
 * Similar to AnimeRemoteKeys, this maps Manga to their respective categories
 * while remembering the exact sorting order from the API.
 */
@Entity(
    tableName = "manga_remote_keys",
    primaryKeys = ["id", "source", "category"]
)
data class MangaRemoteKeys(
    val id: Int,
    val source: ProviderType,

    val category: String,
    val sortOrder: Int,

    val prevPage: Int?,
    val nextPage: Int?
)