package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.AnimeDetails

/**
 * Isolated Cache for Anime Details.
 * By storing the full complex object as a JSON string (handled by RoomConverters),
 * we achieve lightning-fast reads and avoid complex SQL @Relation queries.
 */
@Entity(
    tableName = "anime_details",
    primaryKeys = ["id", "source"]
)
data class AnimeDetailsEntity(
    val id: Int,
    val source: ProviderType,

    // The RoomConverter will automatically serialize/deserialize this complex object!
    val details: AnimeDetails,

    val updatedAt: Long = System.currentTimeMillis()
)



