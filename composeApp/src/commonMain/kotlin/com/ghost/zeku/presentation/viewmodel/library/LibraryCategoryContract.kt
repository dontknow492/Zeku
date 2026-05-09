package com.ghost.zeku.presentation.viewmodel.library

import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity


object LibraryCategoryContract {

    data class State(
        val categories: List<LibraryCategoryEntity> = emptyList(),
        val isLoading: Boolean = true,
        val selectedCategory: LibraryCategoryEntity? = null
    )

    sealed interface Event {
        data object LoadCategories : Event
        data class SelectCategory(val category: LibraryCategoryEntity?) : Event
        data class CreateCategory(
            val item: LibraryCategoryEntity,
        ) : Event

        data class UpdateCategory(val category: LibraryCategoryEntity) : Event
        data class DeleteCategory(val categoryId: Long) : Event
        data class ToggleVisibility(val categoryId: Long, val visible: Boolean) : Event
        data class Reorder(val categoryId: Long, val newOrder: Int) : Event
    }

    sealed interface Effect {
        data class ShowMessage(val message: String) : Effect
        data object NavigateBack : Effect
    }
}


// LibraryContract.kt




