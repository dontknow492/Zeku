package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.MediaDetails

@Entity(
    tableName = "media_details",
    primaryKeys = ["id", "provider", "mediaType"]
)
data class MediaDetailsEntity(
    val id: Int,
    val provider: ProviderType,
    val mediaType: MediaType,
    val details: MediaDetails,
    val updatedAt: Long = System.currentTimeMillis()
)