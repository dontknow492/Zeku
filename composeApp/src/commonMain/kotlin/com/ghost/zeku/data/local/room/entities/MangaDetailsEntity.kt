package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.MangaDetails

@Entity(
    tableName = "manga_details",
    primaryKeys = ["id", "source"]
)
data class MangaDetailsEntity(
    val id: Int,
    val source: ProviderType,

    val details: MangaDetails,

    val updatedAt: Long = System.currentTimeMillis()
)