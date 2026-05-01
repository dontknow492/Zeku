package com.ghost.zeku.data.remote.jikan


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JikanResponse<T>(
    val data: T,
    val pagination: JikanPagination? = null
)

@Serializable
data class JikanPagination(
    val last_visible_page: Int? = null,
    val has_next_page: Boolean = false,
    val current_page: Int? = null
)


@Serializable
data class JikanAnime(
    val mal_id: Int? = null,
    val url: String? = null,
    val title: String? = null,
    @SerialName("title_english") val titleEnglish: String? = null,
    @SerialName("title_japanese") val titleJapanese: String? = null,
    @SerialName("main_picture") val mainPicture: JikanImages? = null,
    val synopsis: String? = null,
    val background: String? = null,
    val episodes: Int? = null,
    val status: String? = null,
    val airing: Boolean = false,
    @SerialName("season") val season: JikanSeason? = null,
    val year: Int? = null,
    val genres: List<JikanGenre>? = null,
    val num_episodes: Int? = null,
    val duration: String? = null,
    val rating: String? = null,
    val source: String? = null,
    val average_episodes: Int? = null,
    val rank: Int? = null,
    val popularity: Int? = null,
    val score: Double? = null,
    val voted_on: Int? = null,
    val num_reviews: Int? = null,
    val inserted: String? = null,
    val updated: String? = null,
    val studios: List<JikanStudio>? = null,
    val pictures: List<JikanImageFormat>? = null,
    val trailers: List<JikanTrailer>? = null,
    val relations: List<JikanRelation>? = null,
    val recommendations: List<JikanRecommendationEdge>? = null,
    val characters: List<JikanCharacterEdge>? = null
)

@Serializable
data class JikanManga(
    val mal_id: Int? = null,
    val url: String? = null,
    val title: String? = null,
    @SerialName("title_english") val titleEnglish: String? = null,
    @SerialName("title_japanese") val titleJapanese: String? = null,
    @SerialName("main_picture") val mainPicture: JikanImages? = null,
    val synopsis: String? = null,
    val background: String? = null,
    val chapters: Int? = null,
    val status: String? = null,
    val publishing_status: String? = null,
    val year: Int? = null,
    val genres: List<JikanGenre>? = null,
    val volumes: Int? = null,
    val rating: String? = null,
    val score: Double? = null,
    val voted_on: Int? = null,
    val num_reviews: Int? = null,
    val inserted: String? = null,
    val updated: String? = null,
    val authors: List<JikanAuthor>? = null,
    val artists: List<JikanAuthor>? = null,
    val relations: List<JikanRelation>? = null,
    val recommendations: List<JikanRecommendationEdge>? = null
)

@Serializable
data class JikanSeason(val name: String? = null)

@Serializable
data class JikanGenre(val mal_id: Int? = null, val name: String? = null)

@Serializable
data class JikanStudio(val mal_id: Int? = null, val name: String? = null)

@Serializable
data class JikanAuthor(val mal_id: Int? = null, val name: String? = null)

@Serializable
data class JikanTrailer(
    val mal_id: Int? = null,
    val url: String? = null,
    val images: JikanImages? = null,
    val title: String? = null
)

@Serializable
data class JikanRelation(
    val related: JikanRecommendationEntry? = null, // Reuse Entry as they share id/title/image
    @SerialName("relation_type") val relationType: String? = null
)


// --- Shared Models ---

@Serializable
data class JikanImages(
    val jpg: JikanImageFormat? = null,
    val webp: JikanImageFormat? = null
)

@Serializable
data class JikanImageFormat(
    val image_url: String? = null,
    val large_image_url: String? = null
)

// --- Characters ---

@Serializable
data class JikanCharacterEdge(
    val character: JikanCharacter? = null,
    val role: String? = null
)

@Serializable
data class JikanCharacter(
    val mal_id: Int? = null,
    val name: String? = null,
    val images: JikanImages? = null
)

// --- Episodes ---

@Serializable
data class JikanEpisode(
    val mal_id: Int? = null,
    val title: String? = null,
    val synopsis: String? = null,
    val filler: Boolean = false
)

// --- Reviews ---

@Serializable
data class JikanReview(
    val mal_id: Int? = null,
    val score: Int? = null,
    val review: String? = null,
    val tags: List<String>? = null, // Jikan uses tags to denote spoilers
    val is_spoiler: Boolean = false,
    val is_preliminary: Boolean = false,
    val date: String? = null,
    val user: JikanUser? = null
)

@Serializable
data class JikanUser(
    val username: String? = null,
    val images: JikanImages? = null
)

// --- Recommendations ---

@Serializable
data class JikanRecommendationEdge(
    val entry: JikanRecommendationEntry? = null,
    val votes: Int? = null
)

@Serializable
data class JikanRecommendationEntry(
    val mal_id: Int? = null,
    val title: String? = null,
    val images: JikanImages? = null
)