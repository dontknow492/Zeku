package com.ghost.zeku.presentation.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ghost.zeku.data.repository.DataResult
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.presentation.components.media.MediaAction
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
        when (event) {

            is MediaDetailContract.Event.Load -> {
                mediaId.update {
                    event.id
                }
                mediaType.update {
                    event.type
                }
                observeDetails(event.id, event.type)
            }

            MediaDetailContract.Event.Retry -> {
                if (mediaId.value == null || mediaType.value == null) return

                mediaId.value?.let {
                    observeDetails(it, mediaType.value!!)
                }

            }

            is MediaDetailContract.Event.OnMediaAction -> {
                handleAction(event.action)
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
                            MediaDetailContract.Effect.ShowError(
                                result.error.message
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
                            MediaDetailContract.Effect.ShowError(
                                result.error.message
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

    private fun handleAction(action: MediaAction) {
        viewModelScope.launch {
            when (action) {

                is MediaAction.MediaClick -> {
                    _effect.send(
                        MediaDetailContract.Effect.NavigateToMedia(action.id)
                    )
                }

                is MediaAction.EpisodeClick -> {
                    TODO("Not yet implemented")
                }

                is MediaAction.CharacterClick -> {
                    _effect.send(
                        MediaDetailContract.Effect.NavigateToCharacter(action.character.id)
                    )
                }

                else -> Unit
            }
        }
    }
}