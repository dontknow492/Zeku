package com.ghost.zeku.presentation.screen.detail

import com.ghost.zeku.domain.model.enum.MediaType
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
        val title: String = "",
        val description: String? = null,
        val coverImage: String = "",
        val bannerImage: String? = null,
        val genres: List<String> = emptyList(),
        val rating: Double? = null,
        val releaseDate: Long? = null,
        val studio: String? = null,
        val author: String? = null,
        val trailer: MediaTrailer? = null,


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
            val type: MediaType // ANIME / MANGA
        ) : Event

        object Retry : Event

        data class OnMediaAction(val action: MediaAction) : Event
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