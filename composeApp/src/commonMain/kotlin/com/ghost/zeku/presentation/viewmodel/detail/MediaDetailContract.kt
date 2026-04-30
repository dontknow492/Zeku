package com.ghost.zeku.presentation.viewmodel.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.paging.PagingData
import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.ReviewAction
import kotlinx.coroutines.flow.Flow

interface MediaDetailContract {

    // -------------------------
    // STATE
    // -------------------------
    data class State(
        val id: Int = 0,
        val type: MediaType = MediaType.UNKNOWN,
        val source: ProviderType? = null,
        val title: String = "",
        val description: String? = null,
        val coverImage: String = "",
        val bannerImage: String? = null,
        val genres: List<String> = emptyList(),
        val rating: Double? = null,
        val releaseDate: Long? = null,
        val studio: String? = null,
        val author: String? = null,
        val artist: String? = null,
        val trailer: MediaTrailer? = null,

        // info
        val status: MediaReleaseStatus = MediaReleaseStatus.UNKNOWN,
        val format: MediaFormat = MediaFormat.UNKNOWN,
        val episodeDuration: Int? = null,
        val totalEpisodes: Int? = null,
        val totalVolumes: Int? = null,
        val totalChapters: Int? = null,
        val score: Float? = null,


        // ✅ small eager data
        val externalLinks: List<ExternalLink> = emptyList(),
        val characters: List<MediaCharacter> = emptyList(),
        val relations: List<MediaRelation> = emptyList(),

        val isLoading: Boolean = false,
        val error: String? = null
    )

    // -------------------------
    // EVENTS (UI → VM)
    // -------------------------
    sealed interface Event {
        data class Load(
            val id: Int,
            val type: MediaType
        ) : Event

        object Retry : Event

        data class OnMediaAction(val action: MediaAction) : Event
        data class OnReviewAction(val action: ReviewAction) : Event

        // Trailer events
        data class PlayTrailer(val trailerId: String) : Event

        // External link events
        data class OpenExternalLink(val link: ExternalLink) : Event

        // Navigation events
        data class ViewCharacter(val character: MediaCharacter) : Event
        data class ViewRelation(val relation: MediaRelation) : Event
        data class ViewRecommendation(val media: Media) : Event
        data class ViewAllCharacters(val mediaId: Int) : Event
        data class ViewAllRelations(val mediaId: Int) : Event
        data class ViewAllReviews(val mediaId: Int) : Event
        data class ViewAllRecommendations(val mediaId: Int) : Event

        // Action events
        object ToggleWatchlist : Event
        object StartWatching : Event

        // Chat event
        object OpenChat : Event

        // External link click
        data class OnExternalLinkClick(val link: ExternalLink) : Event


        // Extra
        data class OnAuthorClick(val author: String) : Event
        data class OnArtistClick(val artist: String) : Event
        data class OnStudioClick(val studio: String) : Event
        data class OnGenreClick(val genre: String) : Event
    }

    // -------------------------
    // SIDE EFFECTS (one-time)
    // -------------------------
    sealed interface Effect {

        data class Navigate(val destination: Destination) : Effect

        data class ShowMessage(val message: String, val type: MessageType) : Effect

        data class OpenExternalLink(val link: ExternalLink) : Effect
        data class PlayTrailer(val trailerId: String) : Effect
        data class OpenChat(val id: Int) : Effect
    }
}


sealed class CreditType(
    val label: String,
    val icon: ImageVector
) {
    data class Artist(val name: String) : CreditType(
        label = "Artist",
        icon = Icons.Filled.Brush
    )

    data class Author(val name: String) : CreditType(
        label = "Author",
        icon = Icons.Filled.Person
    )

    data class Studio(val name: String) : CreditType(
        label = "Studio",
        icon = Icons.Filled.Work
    )
}


fun CreditType.toEvent(): MediaDetailContract.Event = when (this) {
    is CreditType.Artist -> MediaDetailContract.Event.OnArtistClick(name)
    is CreditType.Author -> MediaDetailContract.Event.OnAuthorClick(name)
    is CreditType.Studio -> MediaDetailContract.Event.OnStudioClick(name)
}

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