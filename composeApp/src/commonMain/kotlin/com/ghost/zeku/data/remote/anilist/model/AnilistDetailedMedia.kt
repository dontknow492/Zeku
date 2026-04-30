package com.ghost.zeku.data.remote.anilist.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// --- Connections & Edges ---

@Serializable
data class AniListCharacterConnection(val edges: List<AniListCharacterEdge>? = null)

@Serializable
data class AniListCharacterEdge(
    val role: String? = null,
    val node: AniListCharacterNode? = null
)

@Serializable
data class AniListCharacterNode(
    val id: Int? = null,
    val name: AniListCharacterName? = null,
    val image: AniListCharacterImage? = null
)

@Serializable
data class AniListCharacterName(val full: String? = null)

@Serializable
data class AniListCharacterImage(val large: String? = null)

@Serializable
data class AniListRelationConnection(val edges: List<AniListRelationEdge>? = null)

@Serializable
data class AniListRelationEdge(
    val relationType: String? = null,
    val node: AniListMedia? = null // Reuses your existing AniListMedia DTO
)

// --- Lazy Load Responses ---

@Serializable
data class AniListRecommendationsResponse(
    @SerialName("Media") val media: AniListRecommendationConnectionContainer? = null
)

@Serializable
data class AniListRecommendationConnectionContainer(
    val recommendations: AniListRecommendationConnection? = null
)

@Serializable
data class AniListRecommendationConnection(
    val pageInfo: AniListPageInfo? = null,
    val nodes: List<AniListRecommendationNode>? = null
)

@Serializable
data class AniListRecommendationNode(
    val mediaRecommendation: AniListMedia? = null
)

@Serializable
data class AniListReviewsResponse(
    @SerialName("Media") val media: AniListReviewConnectionContainer? = null
)

@Serializable
data class AniListReviewConnectionContainer(
    val reviews: AniListReviewConnection? = null
)

@Serializable
data class AniListReviewConnection(
    val pageInfo: AniListPageInfo? = null,
    val nodes: List<AniListReviewNode>? = null
)

@Serializable
data class AniListReviewNode(
    val id: Int? = null,
    val summary: String? = null,
    val body: String? = null,
    val score: Int? = null,
    val rating: Int? = null,
    val ratingAmount: Int? = null,
    val user: AniListUser? = null,
    val createdAt: Long? = null
)

@Serializable
data class AniListUser(
    val name: String? = null,
    val avatar: AniListAvatar? = null
)

@Serializable
data class AniListAvatar(val large: String? = null)