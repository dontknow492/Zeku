package com.ghost.zeku.presentation.components.media

import androidx.compose.runtime.Immutable
import com.ghost.zeku.domain.model.media.*


@Immutable
sealed interface ReviewAction {

    data class Click(val review: Review) : ReviewAction

    data class LongClick(val review: Review) : ReviewAction

    data class Expand(val review: Review) : ReviewAction

    data class Like(val review: Review) : ReviewAction

    data class AuthorClick(val review: Review) : ReviewAction
}

@Immutable
sealed interface MediaAction {

    data class MediaClick(val id: Int) : MediaAction

    data class LongClick(val id: Int) : MediaAction

    data class ToggleFavorite(val id: Int, val isFavorite: Boolean) : MediaAction

    data class AddToList(val id: Int) : MediaAction

    data class Share(val id: Int) : MediaAction

    data class RevealNsfw(val id: Int) : MediaAction

    // future-safe
    data class Custom(val key: String, val payload: Any? = null) : MediaAction


    data class EpisodeClick(val episode: Episode) : MediaAction
    data class ChapterClick(val chapter: Chapter) : MediaAction
    data class RelationClick(val relation: MediaRelation) : MediaAction
    data class CharacterClick(val character: MediaCharacter) : MediaAction
}

typealias OnPosterAction = (MediaAction) -> Unit
typealias OnListAction = (MediaAction) -> Unit
typealias OnWideAction = (MediaAction) -> Unit