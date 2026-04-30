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
import com.ghost.zeku.presentation.navigation.Destination
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MediaDetailViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MediaDetailContract.State())
    val state: StateFlow<MediaDetailContract.State> = _state.asStateFlow()

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
        viewModelScope.launch {
            when (event) {
                is MediaDetailContract.Event.Load -> {
                    Napier.i(tag = "MediaDetailVM") { "📥 Loading details for ${event.type.name} ID: ${event.id}" }

                    mediaId.value = event.id
                    mediaType.value = event.type

                    observeDetails(event.id, event.type)
                }

                MediaDetailContract.Event.Retry -> {
                    val id = mediaId.value
                    val type = mediaType.value

                    if (id == null || type == null) {
                        Napier.w(tag = "MediaDetailVM") { "⚠️ Retry ignored: Missing active media ID or Type in State." }
                        return@launch
                    }

                    Napier.i(tag = "MediaDetailVM") { "🔄 Retrying load for ${type.name} ID: $id" }
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
                    Napier.d(tag = "MediaDetailVM") { "🎬 Playing trailer: ${event.trailerId}" }
                    emitEffect(MediaDetailContract.Effect.PlayTrailer(event.trailerId))
                }

                is MediaDetailContract.Event.OpenExternalLink -> {
                    val link = event.link

                    Napier.d(tag = "MediaDetailVM") { "🔗 Opening external link [${link.site}]: ${link.url}" }
                    emitEffect(MediaDetailContract.Effect.OpenExternalLink(link))
                }

                // -------------------------
                // NAVIGATION (SINGLE ITEMS)
                // -------------------------
                is MediaDetailContract.Event.ViewCharacter -> {
                    Napier.d(tag = "MediaDetailVM") { "🧭 Navigate → Character ID: ${event.character.id} (${event.character.name})" }
                    emitEffect(MediaDetailContract.Effect.Navigate(Destination.CharacterDetail(event.character.id)))
                }

                is MediaDetailContract.Event.ViewRelation -> {
                    Napier.d(tag = "MediaDetailVM") { "🧭 Navigate → Relation ${event.relation.mediaType.name} ID: ${event.relation.id}" }
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.MediaDetail(
                                event.relation.id,
                                event.relation.mediaType
                            )
                        )
                    )
                }

                is MediaDetailContract.Event.ViewRecommendation -> {
                    Napier.d(tag = "MediaDetailVM") { "🧭 Navigate → Recommendation ${event.media.mediaType.name} ID: ${event.media.id}" }
                    emitEffect(
                        MediaDetailContract.Effect.Navigate(
                            Destination.MediaDetail(
                                event.media.id,
                                event.media.mediaType
                            )
                        )
                    )
                }

                // -------------------------
                // NAVIGATION (VIEW ALL)
                // -------------------------
                is MediaDetailContract.Event.ViewAllCharacters -> {
                    emitEffect(MediaDetailContract.Effect.Navigate(Destination.AllCharacters(event.mediaId)))
                }

                is MediaDetailContract.Event.ViewAllRelations -> {
                    emitEffect(MediaDetailContract.Effect.Navigate(Destination.AllRelations(event.mediaId)))
                }

                is MediaDetailContract.Event.ViewAllReviews -> {
                    emitEffect(MediaDetailContract.Effect.Navigate(Destination.AllReviews(event.mediaId)))
                }

                is MediaDetailContract.Event.ViewAllRecommendations -> {
                    emitEffect(MediaDetailContract.Effect.Navigate(Destination.AllRecommendations(event.mediaId)))
                }

                // -------------------------
                // USER ACTIONS
                // -------------------------
                MediaDetailContract.Event.ToggleWatchlist -> {
                    Napier.d(tag = "MediaDetailVM") { "⭐ Toggle watchlist clicked for ID: ${mediaId.value}" }
                    TODO("Implement repository call to track entry")
                }

                MediaDetailContract.Event.StartWatching -> {
                    Napier.d(tag = "MediaDetailVM") { "▶️ Start watching clicked for ID: ${mediaId.value}" }
                    TODO("Implement resume/play logic based on trackEntry progress")
                }

                MediaDetailContract.Event.OpenChat -> {
                    val id = mediaId.value ?: return@launch
                    Napier.d(tag = "MediaDetailVM") { "💬 Open chat for Media ID: $id" }
                    emitEffect(MediaDetailContract.Effect.OpenChat(id))
                }

                // -------------------------
                // ENTITY CLICKS
                // -------------------------
                is MediaDetailContract.Event.OnStaffClick -> {
                    Napier.d(tag = "MediaDetailVM") { "👤 Staff clicked: ${event.staff.name} (Role: ${event.staff.role})" }
                    emitEffectMessage("Staff: ${event.staff.name}")
                }

                is MediaDetailContract.Event.OnStudioClick -> {
                    Napier.d(tag = "MediaDetailVM") { "🏢 Studio clicked: ${event.studio.name} (Animation: ${event.studio.isAnimationStudio})" }
                    emitEffectMessage("Studio: ${event.studio.name}")
                }

                is MediaDetailContract.Event.OnTagClick -> {
                    Napier.d(tag = "MediaDetailVM") { "🏷️ Tag clicked: ${event.tag.name} (Rank: ${event.tag.rank}%)" }
                    emitEffectMessage("Tag: ${event.tag.name}")
                }

                is MediaDetailContract.Event.OnGenreClick -> {
                    Napier.d(tag = "MediaDetailVM") { "🎭 Genre clicked: ${event.genre}" }
                    emitEffectMessage("Genre: ${event.genre}")
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
            else -> {
                Napier.e(tag = "MediaDetailVM") { "❌ Unsupported media type requested: $type" }
                _state.update { MediaDetailContract.State(error = "Unsupported media type: $type") }
            }
        }
    }

    private fun observeAnimeDetail(id: Int) {
        viewModelScope.launch {
            repository.getAnimeDetails(id).collect { result ->
                when (result) {
                    is DataResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }

                    is DataResult.Success -> {
                        Napier.i(tag = "MediaDetailVM") { "✅ Successfully loaded ANIME details for ID: $id" }
                        _state.value = result.data.toState()
                    }

                    is DataResult.Error -> {
                        Napier.e(
                            tag = "MediaDetailVM",
                            throwable = result.error.cause
                        ) { "❌ Failed to load ANIME details for ID: $id" }
                        _state.update { it.copy(isLoading = false, error = result.error.message) }
                        emitEffect(
                            MediaDetailContract.Effect.ShowMessage(
                                result.error.message ?: "Unknown error",
                                MessageType.Error.Network
                            )
                        )
                    }
                }
            }
        }
    }

    private fun observeMangaDetail(id: Int) {
        viewModelScope.launch {
            repository.getMangaDetails(id).collect { result ->
                when (result) {
                    is DataResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }

                    is DataResult.Success -> {
                        Napier.i(tag = "MediaDetailVM") { "✅ Successfully loaded MANGA details for ID: $id" }
                        _state.value = result.data.toState()
                    }

                    is DataResult.Error -> {
                        Napier.e(
                            tag = "MediaDetailVM",
                            throwable = result.error.cause
                        ) { "❌ Failed to load MANGA details for ID: $id" }
                        _state.update { it.copy(isLoading = false, error = result.error.message) }
                        emitEffect(
                            MediaDetailContract.Effect.ShowMessage(
                                result.error.message ?: "Unknown error",
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
        viewModelScope.launch {
            when (action) {
                is MediaAction.MediaClick -> {
                    Napier.d(tag = "MediaDetailVM") { "👆 MediaCard clicked: ID ${action.id}" }
                    emitEffect(MediaDetailContract.Effect.Navigate(Destination.MediaDetail(action.id, action.type)))
                }

                is MediaAction.ToggleFavorite -> {
                    Napier.d(tag = "MediaDetailVM") { "❤️ Toggle favorite clicked for ID: ${action.id}" }
                    TODO("Implement adding to fav logic")
                }

                is MediaAction.AddToList -> {
                    Napier.d(tag = "MediaDetailVM") { "📋 Add to list clicked for ID: ${action.id}" }
                    TODO("Implement saving to library logic")
                }

                is MediaAction.Share -> {
                    Napier.d(tag = "MediaDetailVM") { "📤 Share clicked" }
                    emitEffectMessage("Share not implemented yet")
                }

                is MediaAction.RevealNsfw -> {
                    Napier.d(tag = "MediaDetailVM") { "👀 Reveal NSFW clicked for ID: ${action.id}" }
                }

                is MediaAction.LongClick -> {
                    Napier.d(tag = "MediaDetailVM") { "⏱️ Long click detected on ID: ${action.id}" }
                }

                is MediaAction.GenreClick -> {
                    Napier.d(tag = "MediaDetailVM") { "🎭 Genre chip clicked: ${action.genre}" }
                }

                is MediaAction.Custom -> {
                    Napier.w(tag = "MediaDetailVM") { "⚠️ Unhandled custom MediaAction: ${action.key}" }
                }
            }
        }
    }

    private fun handleReviewAction(action: ReviewAction) {
        viewModelScope.launch {
            when (action) {
                is ReviewAction.AuthorClick -> {
                    Napier.d(tag = "MediaDetailVM") { "👤 Review author clicked: ${action.review.author}" }
                    emitEffectMessage("Review by: ${action.review.author}")
                }

                is ReviewAction.Click -> {
                    Napier.d(tag = "MediaDetailVM") { "📝 Review clicked: ${action.review.id}" }
                }

                is ReviewAction.Expand -> {
                    Napier.d(tag = "MediaDetailVM") { "↕️ Review expanded: ${action.review.id}" }
                }

                is ReviewAction.Like -> {
                    Napier.d(tag = "MediaDetailVM") { "👍 Review liked: ${action.review.id}" }
                    TODO("Implement review upvote API call")
                }

                is ReviewAction.LongClick -> {
                    Napier.d(tag = "MediaDetailVM") { "⏱️ Review long click: ${action.review.id}" }
                }
            }
        }
    }

    private suspend fun emitEffect(effect: MediaDetailContract.Effect) {
        _effect.send(effect)
    }

    private suspend fun emitEffectMessage(message: String, type: MessageType = MessageType.Info) {
        emitEffect(MediaDetailContract.Effect.ShowMessage(message, type))
    }
}