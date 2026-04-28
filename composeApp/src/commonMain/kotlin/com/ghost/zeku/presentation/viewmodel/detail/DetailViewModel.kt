package com.ghost.zeku.presentation.viewmodel.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ghost.zeku.data.repository.DataResult
import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.ReviewAction
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class MediaDetailViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MediaDetailContract.State())
    val state: StateFlow<MediaDetailContract.State> = _state

    private val _effect = Channel<MediaDetailContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private val mediaId = MutableStateFlow<Int?>(null)
    private val mediaType = MutableStateFlow<MediaType?>(null)


    // -------------------------
    // PAGING (only heavy data)
    // -------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    val episodes = mediaId.filterNotNull().flatMapLatest {
        repository.getAnimeEpisodes(it)
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val recommendations = mediaId.filterNotNull().flatMapLatest {
        repository.getAnimeRecommendations(it)
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val reviews = mediaId.filterNotNull().flatMapLatest {
        repository.getAnimeReviews(it)
    }.cachedIn(viewModelScope)

    // -------------------------
    // EVENTS
    // -------------------------

    fun onEvent(event: MediaDetailContract.Event) {
        Napier.d(tag = "MediaDetailVM") { "Event received: $event" }

        viewModelScope.launch {
            when (event) {

                is MediaDetailContract.Event.Load -> {
                    Napier.i { "Loading media: id=${event.id}, type=${event.type}" }

                    mediaId.value = event.id
                    mediaType.value = event.type

                    observeDetails(event.id, event.type)
                }

                MediaDetailContract.Event.Retry -> {
                    val id = mediaId.value
                    val type = mediaType.value

                    if (id == null || type == null) {
                        Napier.w { "Retry ignored: missing id/type" }
                        return@launch
                    }

                    Napier.i { "Retrying load: id=$id, type=$type" }
                    observeDetails(id, type)
                }

                is MediaDetailContract.Event.OnMediaAction -> {
                    handleMediaAction(event.action)
                }

                is MediaDetailContract.Event.OnReviewAction -> {
                    handleReviewAction(event.action)
                }

                // -------------------------
                // TRAILER / LINKS
                // -------------------------
                is MediaDetailContract.Event.PlayTrailer -> {
                    Napier.d { "Play trailer: ${event.trailerId}" }
                    emitEffect(MediaDetailContract.Effect.PlayTrailer(event.trailerId))
                }

                is MediaDetailContract.Event.OpenExternalLink,
                is MediaDetailContract.Event.OnExternalLinkClick -> {
                    val link = when (event) {
                        is MediaDetailContract.Event.OpenExternalLink -> event.link
                        is MediaDetailContract.Event.OnExternalLinkClick -> event.link
                        else -> null
                    }

                    if (link == null) return@launch

                    Napier.d { "Opening external link: ${link.url}" }
                    emitEffect(MediaDetailContract.Effect.OpenExternalLink(link))
                }

                // -------------------------
                // NAVIGATION (SINGLE ITEMS)
                // -------------------------
                is MediaDetailContract.Event.ViewCharacter -> {
                    Napier.d { "Navigate → Character ${event.character.id}" }
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.CharacterDetail(event.character.id)
                        )
                    )
                }

                is MediaDetailContract.Event.ViewRelation -> {
                    Napier.d { "Navigate → Relation ${event.relation.id}" }
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.MediaDetail(event.relation.id, event.relation.mediaType)
                        )
                    )
                }

                is MediaDetailContract.Event.ViewRecommendation -> {
                    Napier.d { "Navigate → Recommendation ${event.media.id}" }
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.MediaDetail(event.media.id, event.media.mediaType)
                        )
                    )
                }

                // -------------------------
                // NAVIGATION (VIEW ALL)
                // -------------------------
                is MediaDetailContract.Event.ViewAllCharacters -> {
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.AllCharacters(event.mediaId)
                        )
                    )
                }

                is MediaDetailContract.Event.ViewAllRelations -> {
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.AllRelations(event.mediaId)
                        )
                    )
                }

                is MediaDetailContract.Event.ViewAllReviews -> {
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.AllReviews(event.mediaId)
                        )
                    )
                }

                is MediaDetailContract.Event.ViewAllRecommendations -> {
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.AllRecommendations(event.mediaId)
                        )
                    )
                }

                // -------------------------
                // USER ACTIONS
                // -------------------------
                MediaDetailContract.Event.ToggleWatchlist -> {
                    Napier.d { "Toggle watchlist clicked" }
                    TODO(": implement repository call")
                }

                MediaDetailContract.Event.StartWatching -> {
                    Napier.d { "Start watching clicked" }
                    TODO(": resume/play logic")
                }

                MediaDetailContract.Event.OpenChat -> {
                    val id = mediaId.value ?: return@launch
                    Napier.d { "Open chat for mediaId=$id" }

                    emitEffect(MediaDetailContract.Effect.OpenChat(id))
                }

                // -------------------------
                // CREDITS CLICK
                // -------------------------
                is MediaDetailContract.Event.OnAuthorClick -> {
                    Napier.d { "Author clicked: ${event.author}" }
                    emitEffectMessage("Author: ${event.author}")
                }

                is MediaDetailContract.Event.OnArtistClick -> {
                    Napier.d { "Artist clicked: ${event.artist}" }
                    emitEffectMessage("Artist: ${event.artist}")
                }

                is MediaDetailContract.Event.OnStudioClick -> {
                    Napier.d { "Studio clicked: ${event.studio}" }
                    emitEffectMessage("Studio: ${event.studio}")
                }

                is MediaDetailContract.Event.OnGenreClick -> {
                    Napier.d { "Genre clicked: ${event.genre}" }
                    emitEffectMessage("Genre clicked")
                }
            }
        }


    }

    // -------------------------
    // DETAILS (offline-first)
    // -------------------------

    private fun observeDetails(id: Int, type: MediaType) {
        when (type) {
            MediaType.ANIME -> observeAnimeDetail(id)
            MediaType.MANGA -> observeMangaDetail(id)
            else -> _state.update {
                MediaDetailContract.State(error = "Unsupported media type: $type")
            }
        }
    }


    private fun observeAnimeDetail(id: Int) {
        viewModelScope.launch {
            val flow = repository.getAnimeDetails(id)


            flow.collect { result ->

                when (result) {

                    is DataResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }

                    is DataResult.Success -> {
                        val data = result.data

                        _state.value = _state.value.copy(
                            id = data.id,
                            title = data.title.getPreferred(),
                            description = data.description,
                            coverImage = data.coverImage,
                            bannerImage = data.bannerImage,
                            genres = data.genres,
                            rating = data.averageScore,

                            // ✅ eager small lists
                            characters = data.characters,
                            relations = data.relations,

                            isLoading = false,
                            error = null
                        )
                    }

                    is DataResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.error.message
                            )
                        }

                        _effect.send(
                            MediaDetailContract.Effect.ShowMessage(
                                message = result.error.message,
                                type = MessageType.Error.Network
                            )
                        )
                    }
                }
            }
        }
    }

    private fun observeMangaDetail(id: Int) {
        viewModelScope.launch {
            val flow = repository.getMangaDetails(id)


            flow.collect { result ->
                when (result) {

                    is DataResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }

                    is DataResult.Success -> {
                        val data = result.data

                        _state.value = _state.value.copy(
                            id = data.id,
                            title = data.title.getPreferred(),
                            description = data.description,
                            coverImage = data.coverImage,
                            bannerImage = data.bannerImage,
                            genres = data.genres,
                            rating = data.averageScore,

                            // ✅ eager small lists
                            characters = data.characters,
                            relations = data.relations,

                            isLoading = false,
                            error = null
                        )
                    }

                    is DataResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.error.message
                            )
                        }

                        _effect.send(
                            MediaDetailContract.Effect.ShowMessage(
                                result.error.message,
                                MessageType.Error.Network
                            )
                        )
                    }
                }
            }
        }
    }


    // -------------------------
    // ACTION HANDLER
    // -------------------------

    private fun handleMediaAction(action: MediaAction) {
        Napier.d { "MediaAction: $action" }

        viewModelScope.launch {
            when (action) {

                is MediaAction.MediaClick -> {
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.MediaDetail(action.id, action.type)
                        )
                    )
                }

                is MediaAction.ToggleFavorite -> {
                    Napier.d { "Toggle favorite: ${action.id}" }
                    TODO("Implement adding to fav logic")
                }

                is MediaAction.AddToList -> {
                    Napier.d { "Add to list: ${action.id}" }
                    TODO("Implement saving to library logic")
                }

                is MediaAction.Share -> {
                    emitEffectMessage("Share not implemented yet")
                }

                is MediaAction.RevealNsfw -> {
                    Napier.d { "Reveal NSFW: ${action.id}" }
                }

                is MediaAction.LongClick -> {
                    Napier.d { "Long click: ${action.id}" }
                }

                is MediaAction.Custom -> {
                    Napier.w { "Unhandled custom action: ${action.key}" }
                }

                is MediaAction.GenreClick ->
                    Napier.d { "Genre clicked: ${action.genre}" }
            }
        }
    }

    private fun handleReviewAction(action: ReviewAction) {
        Napier.d { "ReviewAction: $action" }

        viewModelScope.launch {
            when (action) {

                is ReviewAction.AuthorClick -> {
                    emitEffectMessage("Author: ${action.review.author}")
                }

                is ReviewAction.Click -> {
                    Napier.d { "Review clicked: ${action.review.id}" }
                }

                is ReviewAction.Expand -> {
                    Napier.d { "Review expanded: ${action.review.id}" }
                }

                is ReviewAction.Like -> {
                    Napier.d { "Review liked: ${action.review.id}" }
                    TODO("backend")
                }

                is ReviewAction.LongClick -> {
                    Napier.d { "Review long click: ${action.review.id}" }
                }
            }
        }
    }


    private suspend fun emitEffect(effect: MediaDetailContract.Effect) {
        Napier.v { "Effect emitted: $effect" }
        _effect.send(effect)
    }

    private suspend fun emitEffectMessage(
        message: String,
        type: MessageType = MessageType.Info
    ) {
        emitEffect(MediaDetailContract.Effect.ShowMessage(message, type))
    }
}