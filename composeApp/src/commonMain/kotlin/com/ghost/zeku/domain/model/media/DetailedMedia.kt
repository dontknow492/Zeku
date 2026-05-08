package com.ghost.zeku.domain.model.media


import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.enum.*
import kotlinx.serialization.Serializable

@Serializable
data class MediaDetails(

    // ------------------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------------------

    val id: Int,

    val source: ProviderType,

    val mediaType: MediaType,

    // ------------------------------------------------------------------------
    // Core
    // ------------------------------------------------------------------------

    val title: MediaTitle,

    val synonyms: List<String> = emptyList(),

    val countryOfOrigin: String? = null,

    // ------------------------------------------------------------------------
    // Visuals
    // ------------------------------------------------------------------------

    val coverImage: String,

    val bannerImage: String? = null,

    val extraPictures: List<String> = emptyList(),

    val description: String? = null,

    val background: String? = null,

    // ------------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------------

    val status: MediaReleaseStatus? = null,

    val format: MediaFormat? = null,

    val sourceMaterial: MediaSourceMaterial? = null,

    val isAdult: Boolean = false,

    // ------------------------------------------------------------------------
    // Dates
    // ------------------------------------------------------------------------

    val startDate: MediaDate? = null,

    val endDate: MediaDate? = null,

    val createdAt: MediaDate? = null,

    val updatedAt: MediaDate? = null,

    // ------------------------------------------------------------------------
    // Seasonal
    // ------------------------------------------------------------------------

    val season: MediaSeason? = null,

    val seasonYear: Int? = null,

    val broadcastString: String? = null,

    // ------------------------------------------------------------------------
    // Categorization
    // ------------------------------------------------------------------------

    val genres: List<String> = emptyList(),

    val tags: List<MediaTag> = emptyList(),

    // ------------------------------------------------------------------------
    // Statistics
    // ------------------------------------------------------------------------

    val averageScore: Double? = null,

    val meanScore: Double? = null,

    val popularity: Int? = null,

    val favourites: Int? = null,

    val rank: Int? = null,

    // ------------------------------------------------------------------------
    // Anime Fields
    // ------------------------------------------------------------------------

    val totalEpisodes: Int? = null,

    val durationPerEpisode: Int? = null,

    val contentRating: String? = null,

    val nextAiringEpisode: AiringSchedule? = null,

    val studios: List<MediaStudio> = emptyList(),

    // ------------------------------------------------------------------------
    // Manga Fields
    // ------------------------------------------------------------------------

    val totalChapters: Int? = null,

    val totalVolumes: Int? = null,

    val serializations: List<String> = emptyList(),

    val authors: List<MediaStaff> = emptyList(),

    // ------------------------------------------------------------------------
    // Relations
    // ------------------------------------------------------------------------

    val trailer: MediaTrailer? = null,

    val externalLinks: List<ExternalLink> = emptyList(),

    val characters: List<MediaCharacter> = emptyList(),

    val relations: List<MediaRelation> = emptyList(),

    val staff: List<MediaStaff> = emptyList(),

    // ------------------------------------------------------------------------
    // Stats
    // ------------------------------------------------------------------------

    val watching: String? = null,

    val completed: String? = null,

    val onHold: String? = null,

    val dropped: String? = null,

    val planToWatch: String? = null
)


// ========================================================================
// SUB-COMPONENTS
// ========================================================================

@Serializable
data class MediaTag(
    val name: String,
    val description: String? = null,
    val rank: Int? = null, // Relevance percentage (AniList)
    val isSpoiler: Boolean = false,
    val category: String? = null // e.g., "Cast-Main", "Theme"
)

@Serializable
data class MediaStudio(
    val id: Int,
    val name: String,
    val isAnimationStudio: Boolean // True for Kyoto Animation, False for Aniplex (producer)
)

@Serializable
data class MediaStaff(
    val id: Int,
    val name: String,
    val role: String, // e.g., "Original Creator", "Story & Art", "Director"
    val imageUrl: String? = null
)

@Serializable
data class MediaTrailer(
    val title: String?,
    val id: String,     // e.g., YouTube video ID
    val site: String,   // "youtube", "dailymotion"
    val thumbnail: String?
)

@Serializable
data class ExternalLink(
    val url: String,
    val site: String,   // "Crunchyroll", "Twitter", "Official Site"
    val iconUrl: String? = null
)

@Serializable
data class MediaCharacter(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val role: CharacterRole // "MAIN", "SUPPORTING"
)

@Serializable
data class MediaRelation(
    val id: Int,
    val relationType: RelationType, // Replaced String with strict Enum
    val title: MediaTitle,
    val coverImage: String?,
    val mediaType: MediaType,   // "ANIME" or "MANGA"
    val format: MediaFormat     // Extra detail useful for the UI
)

@Serializable
data class AiringSchedule(
    val episode: Int,
    val timeUntilAiring: Long // In seconds
)

// ========================================================================
// LAZY-LOADED COMPONENTS (Loaded via separate API calls/Paging)
// ========================================================================

@Serializable
data class Episode(
    val id: String, // Could be string if it's from a streaming provider
    val number: Int,
    val title: String?,
    val description: String?,
    val thumbnail: String?,
    val isFiller: Boolean = false
)

@Serializable
data class Chapter(
    val id: String,
    val number: Float, // Chapters can be 10.5
    val title: String?,
    val volume: Int?
)


@Serializable
data class Review(
    val id: Int,
    val author: String,
    val authorAvatar: String?, // URL to the reviewer's profile picture
    val score: Int?,           // Score (usually out of 100 for AniList, or 10 for MAL)
    val summary: String?,      // A short tagline or summary (very common in AniList)
    val body: String,          // The full text content of the review
    val upvotes: Int = 0,      // Number of people who found the review helpful
    val isSpoiler: Boolean = false,
    val createdAt: Long? = null // Timestamp for when the review was written
)