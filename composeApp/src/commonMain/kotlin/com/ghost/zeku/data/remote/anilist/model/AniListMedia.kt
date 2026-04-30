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
    val synonyms: List<String>? = null,
    val countryOfOrigin: String? = null,
    val coverImage: AniListCoverImage? = null,
    val bannerImage: String? = null,
    val description: String? = null,
    val status: String? = null,
    val format: String? = null,
    val source: String? = null,
    val isAdult: Boolean? = null,

    val startDate: AniListDate? = null,
    val endDate: AniListDate? = null,
    val season: String? = null,
    val seasonYear: Int? = null,

    val genres: List<String>? = null,
    val tags: List<AniListTag>? = null,

    val averageScore: Int? = null,
    val meanScore: Int? = null,
    val popularity: Int? = null,
    val favourites: Int? = null,

    // Type-specific fields
    val episodes: Int? = null,
    val duration: Int? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    val nextAiringEpisode: AniListAiringSchedule? = null,
    val studios: AniListStudioConnection? = null,

    // Relational Data
    val trailer: AniListTrailer? = null,
    val externalLinks: List<AniListExternalLink>? = null,
    val characters: AniListCharacterConnection? = null,
    val relations: AniListRelationConnection? = null,
    val staff: AniListStaffConnection? = null,
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
    val native: String? = null,
    val userPreferred: String? = null
)

@Serializable
data class AniListCoverImage(val large: String? = null, val extraLarge: String? = null, val medium: String? = null)

@Serializable
data class AniListDate(val year: Int? = null, val month: Int? = null, val day: Int? = null)

@Serializable
data class AniListTag(
    val name: String,
    val description: String? = null,
    val rank: Int? = null,
    val isMediaSpoiler: Boolean? = null,
    val category: String? = null
)

@Serializable
data class AniListAiringSchedule(val episode: Int, val timeUntilAiring: Long)

@Serializable
data class AniListTrailer(val id: String? = null, val site: String? = null, val thumbnail: String? = null)

@Serializable
data class AniListExternalLink(val url: String, val site: String, val icon: String? = null)
