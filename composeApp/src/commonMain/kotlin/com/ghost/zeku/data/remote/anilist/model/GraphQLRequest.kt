package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 1. Add these data classes to represent the GraphQL request strictly
@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Variables
) {
    @Serializable
    data class Variables(
        val page: Int? = null,
        val perPage: Int? = null,
        val sort: List<String>? = null,
        val search: String? = null,
        val id: Int? = null,        // For getting specific Media or Entry ID
        val userId: Int? = null,
        val mediaId: Int? = null,   // Specific to MediaListEntry updates
        val status: String? = null, // Media status (RELEASING) OR List status (CURRENT)
        val type: String? = null,   // ANIME or MANGA
        val format: String? = null, // ADDED: Critical for the 'MOVIE' category (MOVIE, TV, OVA)
        val score: Float? = null,
        val progress: Int? = null,
        val progressIncrement: Int? = null,
        val season: String? = null,
        val seasonYear: Int? = null,
        val countryOfOrigin: String? = null, // ADDED: For the 'MANHWA' (KR) or 'MANHUA' (CN) filter
        val isAdult: Boolean? = false,        // ADDED: Useful for keeping the UI "clean" and professional

        // NEW: Filter Arrays
        @SerialName("genre_in") val genreIn: List<String>? = null,
        @SerialName("genre_not_in") val genreNotIn: List<String>? = null,
        @SerialName("tag_in") val tagIn: List<String>? = null,
        @SerialName("tag_not_in") val tagNotIn: List<String>? = null,
    )
}