package com.ghost.zeku.presentation.screen.detail

import com.ghost.zeku.domain.model.enum.MediaFormat
import com.ghost.zeku.domain.model.enum.MediaReleaseStatus
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.media.ExternalLink
import com.ghost.zeku.domain.model.media.MediaCharacter
import com.ghost.zeku.domain.model.media.MediaRelation
import com.ghost.zeku.domain.model.media.MediaTrailer
import com.ghost.zeku.presentation.components.media.MediaAction

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

        // Trailer events
        data class PlayTrailer(val trailerId: String) : Event

        // External link events
        data class OpenExternalLink(val link: ExternalLink) : Event

        // Navigation events
        data class ViewCharacter(val characterId: Int) : Event
        data class ViewRelation(val relationId: Int) : Event
        data class ViewRecommendation(val mediaId: Int) : Event
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
    }

    // -------------------------
    // SIDE EFFECTS (one-time)
    // -------------------------
    sealed interface Effect {

        data class ShowError(val message: String) : Effect

        data class NavigateToMedia(val id: Int) : Effect
        data class NavigateToEpisode(val id: Int) : Effect
        data class NavigateToCharacter(val id: Int) : Effect
    }
}