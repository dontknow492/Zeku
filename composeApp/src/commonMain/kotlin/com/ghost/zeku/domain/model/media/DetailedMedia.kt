package com.ghost.zeku.domain.model.media


import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.*
import kotlinx.serialization.Serializable


@Serializable
data class AnimeDetails(
    val id: Int,
    val source: ProviderType,
    val title: MediaTitle,
    val coverImage: String,
    val bannerImage: String?,
    val description: String?,
    val status: String?,
    val format: MediaFormat?, // TV, MOVIE, OVA
    val genres: List<String>,
    val averageScore: Double?,

    val trailer: MediaTrailer?,
    val externalLinks: List<ExternalLink>,
    val characters: List<MediaCharacter>,
    val relations: List<MediaRelation>,

    // Summary numbers
    val totalEpisodes: Int?,
    val nextAiringEpisode: AiringSchedule?,

    val trackEntry: TrackEntry? = null
)

@Serializable
data class MangaDetails(
    val id: Int,
    val source: ProviderType,
    val title: MediaTitle,
    val coverImage: String,
    val bannerImage: String?,
    val description: String?,
    val status: String?,
    val format: MediaFormat?,
    val genres: List<String>,
    val averageScore: Double?,

    val externalLinks: List<ExternalLink>,
    val characters: List<MediaCharacter>,
    val relations: List<MediaRelation>,

    val totalChapters: Int?,
    val totalVolumes: Int?,

    val trackEntry: TrackEntry? = null
)


// ========================================================================
// SUB-COMPONENTS
// ========================================================================

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