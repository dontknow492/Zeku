package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ViewerWrapper(
    @SerialName("Viewer") val viewer: Viewer
)

@Serializable
data class Viewer(
    val id: Int,
    val name: String,
    val avatar: Avatar,
    val statistics: Statistics
)

@Serializable
data class Avatar(
    val large: String,
    val medium: String
)

@Serializable
data class Statistics(
    val anime: AnimeStats,
    val manga: MangaStats
)

@Serializable
data class AnimeStats(
    val count: Int,
    val episodesWatched: Int,
    val minutesWatched: Int
)

@Serializable
data class MangaStats(
    val count: Int,
    val chaptersRead: Int
)