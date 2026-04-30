package com.ghost.zeku.domain.model.media


import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.*
import kotlinx.serialization.Serializable


@Serializable
data class AnimeDetails(
    // -------------------------
    // CORE IDENTITY
    // -------------------------
    val id: Int,
    val source: ProviderType,
    val title: MediaTitle,
    val synonyms: List<String> = emptyList(),
    val countryOfOrigin: String?, // e.g., "JP", "KR", "CN"

    // -------------------------
    // VISUALS & TEXT
    // -------------------------
    val coverImage: String,
    val bannerImage: String?,
    val extraPictures: List<String> = emptyList(), // MAL 'pictures' array
    val description: String?,
    val background: String?, // MAL production notes / AniList raw background

    // -------------------------
    // METADATA
    // -------------------------
    val status: MediaReleaseStatus,
    val format: MediaFormat?,
    val sourceMaterial: MediaSourceMaterial?, // e.g., MANGA, ORIGINAL, LIGHT_NOVEL
    val isAdult: Boolean = false,

    // -------------------------
    // DATES & SCHEDULES
    // -------------------------
    val startDate: MediaDate?,
    val endDate: MediaDate?,
    val season: MediaSeason?, // WINTER, SPRING, SUMMER, FALL
    val seasonYear: Int?,
    val broadcastString: String?, // e.g., "Tuesdays at 22:30 (JST)"

    // -------------------------
    // CATEGORIZATION
    // -------------------------
    val genres: List<String>,
    val tags: List<MediaTag>, // AniList Tags / MAL Themes & Demographics

    // -------------------------
    // STATISTICS & RANKINGS
    // -------------------------
    val averageScore: Double?,
    val meanScore: Double?,
    val popularity: Int?, // Number of users with this in their list
    val favourites: Int?,
    val rank: Int?,

    // -------------------------
    // ANIME SPECIFICS
    // -------------------------
    val totalEpisodes: Int?,
    val durationPerEpisode: Int?, // In minutes
    val contentRating: String?, // e.g., "R - 17+ (violence & profanity)", "PG-13"
    val nextAiringEpisode: AiringSchedule?,
    val studios: List<MediaStudio>,

    // -------------------------
    // RELATIONAL DATA (Eagerly Loaded)
    // -------------------------
    val trailer: MediaTrailer?,
    val externalLinks: List<ExternalLink>,
    val characters: List<MediaCharacter>,
    val relations: List<MediaRelation>,
    val staff: List<MediaStaff>, // Directors, Composers, etc.

    // -------------------------
    // USER TRACKING
    // -------------------------
    val trackEntry: TrackEntry? = null
)

@Serializable
data class MangaDetails(
    // -------------------------
    // CORE IDENTITY
    // -------------------------
    val id: Int,
    val source: ProviderType,
    val title: MediaTitle,
    val synonyms: List<String> = emptyList(),
    val countryOfOrigin: String?,

    // -------------------------
    // VISUALS & TEXT
    // -------------------------
    val coverImage: String,
    val bannerImage: String?,
    val extraPictures: List<String> = emptyList(),
    val description: String?,
    val background: String?,

    // -------------------------
    // METADATA
    // -------------------------
    val status: MediaReleaseStatus,
    val format: MediaFormat?,
    val sourceMaterial: MediaSourceMaterial?,
    val isAdult: Boolean = false,

    // -------------------------
    // DATES
    // -------------------------
    val startDate: MediaDate?,
    val endDate: MediaDate?,

    // -------------------------
    // CATEGORIZATION
    // -------------------------
    val genres: List<String>,
    val tags: List<MediaTag>,

    // -------------------------
    // STATISTICS & RANKINGS
    // -------------------------
    val averageScore: Double?,
    val meanScore: Double?,
    val popularity: Int?,
    val favourites: Int?,
    val rank: Int?,

    // -------------------------
    // MANGA SPECIFICS
    // -------------------------
    val totalChapters: Int?,
    val totalVolumes: Int?,
    val serializations: List<String>, // Magazines e.g., "Weekly Shonen Jump"

    // -------------------------
    // RELATIONAL DATA (Eagerly Loaded)
    // -------------------------
    val authors: List<MediaStaff>, // Writers & Artists
    val externalLinks: List<ExternalLink>,
    val characters: List<MediaCharacter>,
    val relations: List<MediaRelation>,

    // -------------------------
    // USER TRACKING
    // -------------------------
    val trackEntry: TrackEntry? = null
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