package org.ghost.zeku.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ghost.zeku.core.enum.SORTING
import org.ghost.zeku.database.models.CommandTemplate
import org.ghost.zeku.database.models.TemplateShortcut
import org.ghost.zeku.database.repository.CommandTemplateRepository
import javax.inject.Inject


data class CommandTemplateUiState(
    val sortOrder: SORTING = SORTING.DESC,
    val sortType: CommandTemplateRepository.CommandTemplateSortType = CommandTemplateRepository.CommandTemplateSortType.DATE,
    val query: String = "",
    val templates: List<CommandTemplate> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),

    val error: String? = null
)

@HiltViewModel
class CommandTemplateViewModel @Inject constructor(
    private val commandTemplateRepository: CommandTemplateRepository
) : ViewModel() {
    // PRIVATE: These are the mutable sources of state, changed only by the ViewModel
//    private val _templates = commandTemplateRepository.getFiltered()
    // PRIVATE: These are the mutable sources of state, changed only by the ViewModel
    private val _sortOrder = MutableStateFlow(SORTING.DESC)
    private val _sortType =
        MutableStateFlow(CommandTemplateRepository.CommandTemplateSortType.TITLE)
    private val _query = MutableStateFlow("")
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    // 1. Combine the filter parameters into one flow.
    private val filterParams = combine(_query, _sortType, _sortOrder) { query, type, order ->
        Triple(query, type, order)
    }

    // 2. Use flatMapLatest to react to filter changes and get a new data flow from the repository.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val templatesFlow: Flow<List<CommandTemplate>> =
        filterParams.flatMapLatest { (query, sortType, sortOrder) ->
            commandTemplateRepository.getFiltered(query, sortType, sortOrder)
        }

    // PUBLIC: The single, observable state for the UI
    // 3. The final combine is now much simpler!
    val uiState: StateFlow<CommandTemplateUiState> = combine(
        templatesFlow, // Use the new flow that comes from the database
        _selectedIds,
        filterParams // Also include filterParams to update UI controls if needed
    ) { templates, selectedIds, (query, sortType, sortOrder) ->
        CommandTemplateUiState(
            templates = templates,
            selectedIds = selectedIds,
            query = query,
            sortType = sortType,
            sortOrder = sortOrder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CommandTemplateUiState()
    )


    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onSortOrderChanged(newSortOrder: SORTING) {
        _sortOrder.value = newSortOrder
    }

    fun onSortTypeChanged(newSortType: CommandTemplateRepository.CommandTemplateSortType) {
        _sortType.value = newSortType
    }

    fun toggleSelection(template: CommandTemplate) {
        _selectedIds.update { currentIds ->
            if (template.id in currentIds) currentIds - template.id
            else currentIds + template.id
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun selectAll() {
        _selectedIds.value = uiState.value.templates.map { it.id }.toSet()
    }

    /** Selects all unselected items and deselects all selected items. */
    fun inverseSelection() {
        val allIds = uiState.value.templates.map { it.id }.toSet()
        _selectedIds.update { currentSelectedIds ->
            allIds - currentSelectedIds // Uses set difference for a clean inversion
        }
    }


    fun getTemplate(itemId: Long): CommandTemplate {
        return commandTemplateRepository.getItem(itemId)
    }

    fun getAll(): List<CommandTemplate> {
        return commandTemplateRepository.getAll()
    }

    fun getAllShortcuts(): List<TemplateShortcut> {
        return commandTemplateRepository.getAllShortCuts()
    }

    fun getTotalNumber(): Int {
        return commandTemplateRepository.getTotalNumber()
    }

    fun getTotalShortcutNumber(): Int {
        return commandTemplateRepository.getTotalShortcutNumber()
    }

    fun insert(item: CommandTemplate) = viewModelScope.launch(Dispatchers.IO) {
        commandTemplateRepository.insert(item)
    }

    fun delete(item: CommandTemplate) = viewModelScope.launch(Dispatchers.IO) {
        commandTemplateRepository.delete(item)
    }

    fun insertShortcut(item: TemplateShortcut) = viewModelScope.launch(Dispatchers.IO) {
        commandTemplateRepository.insertShortcut(item)
    }

    fun deleteShortcut(item: TemplateShortcut) = viewModelScope.launch(Dispatchers.IO) {
        commandTemplateRepository.deleteShortcut(item)
    }

    fun deleteAllShortcuts() = viewModelScope.launch(Dispatchers.IO) {
        commandTemplateRepository.deleteAllShortcuts()
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        commandTemplateRepository.deleteAll()
    }

    fun update(item: CommandTemplate) = viewModelScope.launch(Dispatchers.IO) {
        commandTemplateRepository.update(item)
    }

}