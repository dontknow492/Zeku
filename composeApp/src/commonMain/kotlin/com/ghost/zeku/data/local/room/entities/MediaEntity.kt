package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaSourceMaterial
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType

/**
 * Unified media table for all media types.
 * Type-specific fields are nullable intentionally.
 */
@Entity(
    tableName = "media",
    primaryKeys = ["id", "provider"],
    indices = [
        // Core
        Index(value = ["provider"]),
        Index(value = ["mediaType"]),

        // Search / Sorting
        Index(value = ["title"]),
        Index(value = ["score"]),
        Index(value = ["popularity"]),
        Index(value = ["updatedAt"]),

        // Filtering
        Index(value = ["status"]),
        Index(value = ["format"]),

        // Type-specific filtering
        Index(value = ["studio"]),
        Index(value = ["author"])
    ]
)
data class MediaEntity(

    /**
     * Remote media ID.
     *
     * IMPORTANT:
     * ID uniqueness is scoped by provider.
     *
     * Example:
     * AniList ID 1 != MAL ID 1
     */
    val id: Int,

    /**
     * Source/provider where this media came from.
     *
     * Example:
     * - AniList
     * - MAL
     * - Kitsu
     */
    val provider: ProviderType,

    /**
     * Anime, Manga, Novel, Movie, etc.
     */
    val mediaType: MediaType,

    // ------------------------------------------------------------------------
    // Core Shared Fields
    // ------------------------------------------------------------------------

    val title: MediaTitle,

    val coverImage: String,

    val bannerImage: String? = null,

    val description: String? = null,

    val genres: List<String> = emptyList(),

    val tags: List<String> = emptyList(),

    val status: MediaReleaseStatus? = null,

    val score: Float? = null,

    val popularity: Int? = null,

    val startDate: MediaDate? = null,

    val endDate: MediaDate? = null,

    val format: String? = null,

    val season: String? = null,

    val seasonYear: Int? = null,

    val countryOfOrigin: String? = null,

    val providerMaterial: String? = null,

    val siteUrl: String? = null,

    val sourceMaterial: MediaSourceMaterial? = null,

    // ------------------------------------------------------------------------
    // Anime Fields
    // ------------------------------------------------------------------------

    val episodes: Int? = null,

    val duration: Int? = null,

    val studio: String? = null,

    val nextEpisodeAt: Long? = null,

    // ------------------------------------------------------------------------
    // Manga / Novel Fields
    // ------------------------------------------------------------------------

    val chapters: Int? = null,

    val volumes: Int? = null,

    val author: String? = null,

    // ------------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------------

    /**
     * Timestamp when inserted locally.
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Timestamp when updated locally.
     */
    val updatedAt: Long = System.currentTimeMillis()
)