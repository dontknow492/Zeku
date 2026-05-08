package com.ghost.zeku.presentation.components.media

import androidx.compose.runtime.Immutable
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Review


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

    data class MediaClick(val id: Int, val type: MediaType) : MediaAction

    data class GenreClick(val genre: String) : MediaAction

    data class LongClick(val id: Int, val type: MediaType) : MediaAction

    data class ToggleFavorite(val id: Int, val isFavorite: Boolean) : MediaAction

    data class AddToList(val id: Int) : MediaAction

    data class Share(val id: Int) : MediaAction

    data class RevealNsfw(val id: Int) : MediaAction

    data class TrailingClick(val id: Int, val type: MediaType) : MediaAction

    // future-safe
    data class Custom(val key: String, val payload: Any? = null) : MediaAction
}

typealias OnMediaAction = (MediaAction) -> Unit