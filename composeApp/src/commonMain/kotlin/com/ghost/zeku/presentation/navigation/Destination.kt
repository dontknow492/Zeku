package com.ghost.zeku.presentation.navigation

import com.ghost.zeku.domain.model.enum.MediaType

sealed interface Destination {

    data class MediaDetail(
        val id: Int,
        val type: MediaType
    ) : Destination

    data class EpisodeDetail(val id: Int) : Destination

    data class CharacterDetail(val id: Int) : Destination

    data class AllReviews(val mediaId: Int) : Destination
    data class AllRecommendations(val mediaId: Int) : Destination
    data class AllCharacters(val mediaId: Int) : Destination
    data class AllRelations(val mediaId: Int) : Destination

    data class Search(val query: String?) : Destination

    data class ViewAllCategories(
        val title: String, // e.g., "Trending Now", "Popular"
        val categoryId: String, // The enum name used to trigger "View All"
        val type: MediaType,
    ) : Destination

    data object Back : Destination

}