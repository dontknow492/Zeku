package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import com.ghost.zeku.domain.model.enum.ProviderType

@Entity(
    tableName = "episode_remote_keys",
    primaryKeys = ["id", "provider"]
)
data class EpisodeRemoteKeys(
    val id: String,
    val provider: ProviderType,
    val mediaId: Int, // Helpful for clearing keys for a specific anime
    val prevPage: Int?,
    val nextPage: Int?,
    val lastUpdated: Long = System.currentTimeMillis()
)