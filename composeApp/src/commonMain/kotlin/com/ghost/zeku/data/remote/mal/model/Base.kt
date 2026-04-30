package com.ghost.zeku.data.remote.mal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Paging & Nodes ---
@Serializable
data class MalPagedResponse<T>(
    val data: List<MalNode<T>>? = null,
    val paging: MalPaging? = null
)

@Serializable
data class MalNode<T>(
    val node: T? = null,
    @SerialName("list_status")
    val listStatus: MalListStatus? = null // This is how MAL returns the user's tracking info!
)

@Serializable
data class MalPaging(
    @SerialName("next") val nextUrl: String? = null
)

// --- MAL Specific Models ---
@Serializable
data class MalAnimeDto(
    val id: Int? = null,
    val title: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("alternative_titles") val alternativeTitles: MalAlternativeTitles? = null,
    @SerialName("main_picture") val mainPicture: MalPicture? = null,
    val synopsis: String? = null,
    @SerialName("num_episodes") val numEpisodes: Int? = null,
    @SerialName("average_episode_duration") val averageEpisodeDuration: Int? = null, // ADDED: For duration mapping
    val status: String? = null, // Airing status (e.g., "currently_airing")
    val mean: Double? = null,
    val genres: List<MalGenre>? = null,
    @SerialName("start_date") val startDate: String? = null, // ADDED: For date parsing

    // Allows mapping user list data directly from a search or details request
    @SerialName("my_list_status") val myListStatus: MalListStatus? = null,
    // Relational Data
    @SerialName("related_anime") val relatedAnime: List<MalRelatedEdge>? = null,
    @SerialName("related_manga") val relatedManga: List<MalRelatedEdge>? = null,
    val recommendations: List<MalRecommendationEdge>? = null,


    )

@Serializable
data class MalMangaDto(
    val id: Int? = null,
    val title: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("alternative_titles") val alternativeTitles: MalAlternativeTitles? = null,
    @SerialName("main_picture") val mainPicture: MalPicture? = null,
    val synopsis: String? = null,
    val mean: Double? = null,
    val status: String? = null, // Publishing status
    val genres: List<MalGenre>? = null,
    @SerialName("num_chapters") val numChapters: Int? = null,
    @SerialName("num_volumes") val numVolumes: Int? = null,
    @SerialName("start_date") val startDate: String? = null, // ADDED: For date parsing

    // Relational Data
    @SerialName("related_anime") val relatedAnime: List<MalRelatedEdge>? = null,
    @SerialName("related_manga") val relatedManga: List<MalRelatedEdge>? = null,
    val recommendations: List<MalRecommendationEdge>? = null,

    @SerialName("my_list_status") val myListStatus: MalListStatus? = null
)

@Serializable
data class MalAlternativeTitles(
    val en: String? = null,
    val ja: String? = null
)

@Serializable
data class MalPicture(
    val medium: String? = null,
    val large: String? = null
)

@Serializable
data class MalListStatus(
    val status: String? = null,
    @SerialName("num_episodes_watched") val numEpisodesWatched: Int? = null,
    @SerialName("num_chapters_read") val numChaptersRead: Int? = null,
    val score: Int? = null
)

@Serializable
data class MalGenre(
    val id: Int? = null,
    val name: String? = null
)

@Serializable
data class MalUserDto(
    val id: Int,
    val name: String,
    @SerialName("picture") val pictureUrl: String? = null,
    val gender: String? = null,
    val birthday: String? = null,
    val location: String? = null,
    @SerialName("anime_statistics") val animeStatistics: MalAnimeStatistics? = null
)

@Serializable
data class MalAnimeStatistics(
    @SerialName("num_items_watching") val numItemsWatching: Int? = null,
    @SerialName("num_items_completed") val numItemsCompleted: Int? = null,
    @SerialName("num_items_on_hold") val numItemsOnHold: Int? = null,
    @SerialName("num_items_dropped") val numItemsDropped: Int? = null,
    @SerialName("num_items_plan_to_watch") val numItemsPlanToWatch: Int? = null,
    @SerialName("num_items") val numTotalItems: Int? = null,
    @SerialName("num_days_watched") val numDaysWatched: Double? = null,
    @SerialName("num_episodes") val numEpisodes: Int? = null,
    @SerialName("mean_score") val meanScore: Double? = null
)


@Serializable
data class MalRelatedEdge(
    val node: MalRelatedNode? = null,
    @SerialName("relation_type") val relationType: String? = null,
    @SerialName("relation_type_formatted") val relationTypeFormatted: String? = null
)

@Serializable
data class MalRelatedNode(
    val id: Int? = null,
    val title: String? = null,
    @SerialName("main_picture") val mainPicture: MalPicture? = null
)

@Serializable
data class MalRecommendationEdge(
    val node: MalRelatedNode? = null,
    @SerialName("num_recommendations") val numRecommendations: Int? = null
)

