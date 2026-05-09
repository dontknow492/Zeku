package com.ghost.zeku.presentation.viewmodel.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity
import com.ghost.zeku.domain.repository.CategoryRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryCategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private companion object {
        const val TAG = "CategoryVM"
    }

    // ---- State & Effect ----
    private val _state = MutableStateFlow(LibraryCategoryContract.State(isLoading = true))
    val state: StateFlow<LibraryCategoryContract.State> = _state.asStateFlow()

    private val _effect = Channel<LibraryCategoryContract.Effect>(Channel.BUFFERED)
    val effect: Flow<LibraryCategoryContract.Effect> = _effect.receiveAsFlow()

    init {
        Napier.d(tag = TAG) { "ViewModel created" }
        handleEvent(LibraryCategoryContract.Event.LoadCategories)
    }

    fun handleEvent(event: LibraryCategoryContract.Event) {
        Napier.d(tag = TAG) { "Event: $event" }
        when (event) {
            is LibraryCategoryContract.Event.LoadCategories -> loadCategories()
            is LibraryCategoryContract.Event.SelectCategory -> selectCategory(event.category)
            is LibraryCategoryContract.Event.CreateCategory -> createCategory(event)
            is LibraryCategoryContract.Event.UpdateCategory -> updateCategory(event.category)
            is LibraryCategoryContract.Event.DeleteCategory -> deleteCategory(event.categoryId)
            is LibraryCategoryContract.Event.ToggleVisibility -> toggleVisibility(event.categoryId, event.visible)
            is LibraryCategoryContract.Event.Reorder -> reorder(event.categoryId, event.newOrder)
        }
    }

    // ---- Event handlers ----

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                // Ensure defaults exist before loading
                categoryRepository.ensureDefaultsExist()
                // Collect flow
                categoryRepository.observeAll()
                    .catch { e ->
                        Napier.e(tag = TAG, throwable = e) { "Error observing categories" }
                        _effect.send(LibraryCategoryContract.Effect.ShowMessage("Failed to load categories"))
                        _state.update { it.copy(isLoading = false) }
                    }
                    .collect { categories ->
                        _state.update {
                            it.copy(
                                categories = categories,
                                isLoading = false,
                                selectedCategory = it.selectedCategory?.id?.let { id ->
                                    categories.find { cat -> cat.id == id }
                                }
                            )
                        }
                    }
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "loadCategories failed" }
                _state.update { it.copy(isLoading = false) }
                _effect.send(LibraryCategoryContract.Effect.ShowMessage("Failed to load categories"))
            }
        }
    }

    private fun selectCategory(category: LibraryCategoryEntity?) {
        _state.update { it.copy(selectedCategory = category) }
        Napier.d(tag = TAG) { "Selected category: $category" }
    }

    private fun createCategory(event: LibraryCategoryContract.Event.CreateCategory) {
        viewModelScope.launch {
            try {
                categoryRepository.create(event.item)
                Napier.i(tag = TAG) { "Category created: ${event.item.name}" }
                _effect.send(LibraryCategoryContract.Effect.ShowMessage("Category created"))
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Failed to create category" }
                _effect.send(LibraryCategoryContract.Effect.ShowMessage("Failed to create category"))
            }
        }
    }

    private fun updateCategory(category: LibraryCategoryEntity) {
        viewModelScope.launch {
            try {
                categoryRepository.update(category.copy(updatedAt = System.currentTimeMillis()))
                Napier.i(tag = TAG) { "Category updated: ${category.id}" }
                _effect.send(LibraryCategoryContract.Effect.ShowMessage("Category updated"))
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Failed to update category" }
                _effect.send(LibraryCategoryContract.Effect.ShowMessage("Failed to update category"))
            }
        }
    }

    private fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.delete(categoryId)
                Napier.i(tag = TAG) { "Category deleted: $categoryId" }
                _effect.send(LibraryCategoryContract.Effect.ShowMessage("Category deleted"))
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Failed to delete category" }
                _effect.send(LibraryCategoryContract.Effect.ShowMessage("Failed to delete category"))
            }
        }
    }

    private fun toggleVisibility(categoryId: Long, visible: Boolean) {
        viewModelScope.launch {
            try {
                categoryRepository.setVisibility(categoryId, visible)
                Napier.d(tag = TAG) { "Visibility toggled: id=$categoryId visible=$visible" }
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Toggle visibility failed" }
                _effect.send(LibraryCategoryContract.Effect.ShowMessage("Failed to change visibility"))
            }
        }
    }

    private fun reorder(categoryId: Long, newOrder: Int) {
        viewModelScope.launch {
            try {
                categoryRepository.updateSortOrder(categoryId, newOrder)
                Napier.d(tag = TAG) { "Reorder: id=$categoryId order=$newOrder" }
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Reorder failed" }
                _effect.send(LibraryCategoryContract.Effect.ShowMessage("Failed to reorder"))
            }
        }
    }
}