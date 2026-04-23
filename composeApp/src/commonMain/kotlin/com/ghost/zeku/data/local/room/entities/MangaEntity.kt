package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.ProviderType

@Entity(
    tableName = "manga",
    primaryKeys = ["id", "source"],
    indices = [
        Index(value = ["source"]),
        Index(value = ["status"]),
        Index(value = ["score"]),
        Index(value = ["author"]),
        Index(value = ["title"]),
        Index(value = ["updatedAt"])
    ]
)
data class MangaEntity(
    val id: Int,
    val source: ProviderType,

    // Core Media Fields
    val title: MediaTitle,
    val coverImage: String,
    val bannerImage: String?,
    val description: String?,
    val genres: List<String>,
    val status: MediaReleaseStatus?,
    val score: Float?,
    val startDate: MediaDate?,
    val trackEntry: TrackEntry?,

    // Manga Specific Fields
    val chapters: Int?,
    val volumes: Int?,
    val author: String?,

    // Metadata for Maintenance
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


