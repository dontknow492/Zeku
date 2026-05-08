package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType

/**
 * Stores paging/category mapping for media lists.
 *
 * This table links media items to specific feeds/categories while preserving:
 * - pagination state
 * - exact API ordering
 * - caching metadata
 *
 * Examples:
 * - TRENDING
 * - POPULAR
 * - TOP_RATED
 * - SEASONAL
 * - FAVORITES
 * - SEARCH:<query>
 * - GENRE:Action
 */
@Entity(
    tableName = "media_remote_keys",

    primaryKeys = [
        "mediaId",
        "provider",
        "mediaType",
        "category"
    ],

    indices = [
        Index(value = ["mediaId", "provider", "mediaType", "category"], unique = true),

        // Feed/category lookups
        Index(value = ["category"]),

        // Media lookups
        Index(value = ["mediaId", "provider"]),

        // Filtering
        Index(value = ["mediaType"]),

        // Cache cleanup
        Index(value = ["lastUpdated"])
    ]
)
data class MediaRemoteKeys(

    /**
     * References MediaEntity.id
     */
    val mediaId: Int,

    /**
     * References MediaEntity.source
     */
    val provider: ProviderType,

    /**
     * Anime / Manga / Novel / etc
     */
    val mediaType: MediaType,

    /**
     * Feed/list/category identifier.
     *
     * Examples:
     * - TRENDING
     * - POPULAR
     * - TOP_RATED
     * - SEARCH:naruto
     * - GENRE:Action
     */
    val category: String,

    /**
     * Exact API ordering.
     *
     * Preserves remote sorting locally.
     */
    val sortOrder: Int,

    // ------------------------------------------------------------------------
    // Paging
    // ------------------------------------------------------------------------

    val prevPage: Int?,

    val nextPage: Int?,

    // ------------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------------

    val lastUpdated: Long = System.currentTimeMillis()
)