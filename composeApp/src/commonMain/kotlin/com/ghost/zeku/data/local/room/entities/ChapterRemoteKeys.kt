package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import com.ghost.zeku.domain.model.enum.ProviderType

@Entity(
    tableName = "chapter_remote_keys",
    primaryKeys = ["id", "provider"]
)
data class ChapterRemoteKeys(
    val id: String,
    val provider: ProviderType,
    val mediaId: Int,
    val prevPage: Int?,
    val nextPage: Int?,
    val lastUpdated: Long = System.currentTimeMillis()
)