package com.ghost.zeku.data.remote.jikan


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