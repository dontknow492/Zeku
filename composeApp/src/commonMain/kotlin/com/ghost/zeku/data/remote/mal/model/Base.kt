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
// --- MAL Specific Models ---
@Serializable
data class MalMediaDto(

    val id: Int? = null,

    val title: String? = null,

    @SerialName("media_type")
    val mediaType: String? = null,

    @SerialName("alternative_titles")
    val alternativeTitles: MalAlternativeTitles? = null,

    @SerialName("main_picture")
    val mainPicture: MalPicture? = null,

    val synopsis: String? = null,

    val background: String? = null,

    val pictures: List<MalPicture>? = null,

    val status: String? = null,

    val genres: List<MalGenre>? = null,

    @SerialName("start_date")
    val startDate: String? = null,

    @SerialName("end_date")
    val endDate: String? = null,

    @SerialName("start_season")
    val startSeason: MalSeason? = null,

    val broadcast: MalBroadcast? = null,

    val mean: Double? = null,

    val rank: Int? = null,

    val popularity: Int? = null,

    @SerialName("num_list_users")
    val numListUsers: Int? = null,

    @SerialName("num_scoring_users")
    val numScoringUsers: Int? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    val nsfw: String? = null,

    val rating: String? = null,

    val source: String? = null,

    // =========================
    // UNIFIED COUNTS
    // =========================

    @SerialName("num_episodes")
    val numEpisodes: Int? = null,

    @SerialName("average_episode_duration")
    val averageEpisodeDuration: Int? = null,

    @SerialName("num_chapters")
    val numChapters: Int? = null,

    @SerialName("num_volumes")
    val numVolumes: Int? = null,

    // =========================
    // RELATIONS
    // =========================

    val studios: List<MalStudio>? = null,

    val authors: List<MalAuthorEdge>? = null,

    val serialization: List<MalSerializationEdge>? = null,

    @SerialName("related_anime")
    val relatedAnime: List<MalRelatedEdge>? = null,

    @SerialName("related_manga")
    val relatedManga: List<MalRelatedEdge>? = null,

    val recommendations: List<MalRecommendationEdge>? = null,

    @SerialName("my_list_status")
    val myListStatus: MalListStatus? = null,

    val statistics: MalStatistics? = null
)

@Serializable
data class MalStudio(val id: Int? = null, val name: String? = null)

@Serializable
data class MalAuthorEdge(val node: MalAuthorNode? = null, val role: String? = null)

@Serializable
data class MalAuthorNode(
    val id: Int? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null
)


@Serializable
data class MalAlternativeTitles(val en: String? = null, val ja: String? = null, val synonyms: List<String>? = null)

@Serializable
data class MalSeason(val year: Int? = null, val season: String? = null)

@Serializable
data class MalBroadcast(
    @SerialName("day_of_the_week") val dayOfTheWeek: String? = null,
    @SerialName("start_time") val startTime: String? = null
)

@Serializable
data class MalSerializationEdge(val node: MalSerializationNode? = null)

@Serializable
data class MalSerializationNode(val id: Int? = null, val name: String? = null)


@Serializable
data class MalPicture(
    val medium: String? = null,
    val large: String? = null
)

@Serializable
data class MalListStatus(
    val status: String? = null,
    @SerialName("num_episodes_watched") val numEpisodesWatched: Int? = null,
    @SerialName("num_volumes_read") val numVolumesRead: Int? = null,
    @SerialName("num_chapters_read") val numChaptersRead: Int? = null,
    val score: Double? = null,
    @SerialName("is_rewatching") val isRewatching: Boolean? = null,
    @SerialName("is_rereading") val isRereading: Boolean? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("start_date") val startDate: String? = null,
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
    @SerialName("joined_at") val joinedAt: String? = null,
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

@Serializable
data class MalStatistics(
    val status: MalStatisticsStatus? = null,
    @SerialName("num_list_users")
    val numListUsers: Int? = null
)

@Serializable
data class MalStatisticsStatus(
    val watching: String? = null,
    val completed: String? = null,
    @SerialName("on_hold")
    val onHold: String? = null,
    val dropped: String? = null,
    @SerialName("plan_to_watch")
    val planToWatch: String? = null
)


@Serializable
data class MalLibraryResponse(
    val data: List<MalLibraryNode>,
    val paging: MalPaging
)


@Serializable
data class MalLibraryNode(
    val node: MalMediaNode,
    @SerialName("list_status") val listStatus: MalMediaListEntry
)


@Serializable
data class MalMediaListEntry(
    val status: String,
    val score: Int,
    @SerialName("num_episodes_watched") val episodesWatched: Int? = null,
    @SerialName("num_chapters_read") val chaptersRead: Int? = null,
    @SerialName("is_rewatching") val isRewatching: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null
)


@Serializable
data class MalMediaNode(
    val id: Int,
    val title: String,
    @SerialName("main_picture") val mainPicture: MalPicture? = null,
    @SerialName("media_type") val mediaType: String? = null
)