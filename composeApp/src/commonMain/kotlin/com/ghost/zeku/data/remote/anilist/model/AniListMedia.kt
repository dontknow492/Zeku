package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AniListSingleMediaData(
    @SerialName("Media") val media: AniListMedia?
)

@Serializable
data class AniListMedia(
    val id: Int,
    val type: String? = null,
    val title: AniListTitle? = null,
    val coverImage: AniListCoverImage? = null,
    val bannerImage: String? = null,
    val description: String? = null,
    val status: String? = null,
    val genres: List<String>? = null,
    val averageScore: Int? = null,
    val startDate: AniListDate? = null, // Added to match query

    val format: String? = null,

    // Type-specific fields
    val episodes: Int? = null,
    val duration: Int? = null,  // Added to match query
    val chapters: Int? = null,
    val volumes: Int? = null,


    //detailed
    val trailer: AniListTrailer? = null,
    val externalLinks: List<AniListExternalLink>? = null,
    val characters: AniListCharacterConnection? = null,
    val relations: AniListRelationConnection? = null,
    val nextAiringEpisode: AniListAiringSchedule? = null,

    val mediaListEntry: AniListMediaListEntry? = null,
)

@Serializable
data class AniListMediaListEntry(
    val id: Int,
    val mediaId: Int? = null,
    val status: String? = null,
    val progress: Int? = null,
    // AniList scores can be 0-100 or 0.0-10.0. Double is safer than Int.
    val score: Double? = null,
    // This is the actual Anime/Manga info inside the entry
    val media: AniListMedia? = null
)

@Serializable
data class AniListTitle(
    val romaji: String? = null,
    val english: String? = null,
    val native: String? = null
)

@Serializable
data class AniListCoverImage(
    val large: String? = null,
    // Note: If the query only requests "large", medium will always be null.
    val medium: String? = null
)

@Serializable
data class AniListDate(
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null
)