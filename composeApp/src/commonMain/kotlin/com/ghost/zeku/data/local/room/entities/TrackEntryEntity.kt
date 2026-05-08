package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.enum.TrackStatus

@Entity(
    tableName = "track_entries",
    indices = [
        Index(
            value = ["mediaId", "mediaType", "provider"],
            unique = true
        )
    ]
)
data class TrackEntryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val mediaId: Int,

    val mediaType: MediaType,

    /**
     * AniList / MAL / etc
     */
    val provider: ProviderType,

    /**
     * Remote tracking entry ID
     */
    val remoteId: String? = null,

    val status: TrackStatus = TrackStatus.PLANNING,

    val progress: Int = 0,

    val score: Float? = null,

    val repeatCount: Int = 0,

    val startedAt: Long? = null,

    val completedAt: Long? = null,

    val notes: String? = null,

    val updatedAt: Long = System.currentTimeMillis(),

    val createdAt: Long = System.currentTimeMillis()
)