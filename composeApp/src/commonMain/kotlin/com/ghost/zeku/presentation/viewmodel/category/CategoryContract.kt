package com.ghost.zeku.presentation.viewmodel.category

import androidx.paging.PagingData
import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.navigation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface CategoryContract {
    data class State(
        val categoryId: String = "",
        val type: MediaType = MediaType.ANIME,
        val title: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val data: Flow<PagingData<Media>> = emptyFlow()
    )

    sealed interface Event {
        // Matches your HomeContract's LoadHomeData pattern
        data class LoadCategory(
            val categoryId: String,
            val title: String,
            val mediaType: MediaType
        ) : Event

        data class OnMediaAction(val action: MediaAction) : Event
        data object OnRefresh : Event
        data object OnBack : Event
    }

    sealed interface Effect {
        data class Navigate(val destination: Destination) : Effect
        data class ShowMessage(val message: String, val type: MessageType) : Effect
    }
}