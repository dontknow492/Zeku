package org.ghost.zeku.ui.screen.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.zeku.R
import org.ghost.zeku.core.enum.SORTING
import org.ghost.zeku.database.models.CommandTemplate
import org.ghost.zeku.database.repository.CommandTemplateRepository
import org.ghost.zeku.ui.component.CommandTemplateItem
import org.ghost.zeku.ui.component.SearchableTopAppBar
import org.ghost.zeku.viewModels.CommandTemplateUiState

sealed interface CommandPageEvent {
    data class OnAddCommandTemplate(val commandTemplate: CommandTemplate) : CommandPageEvent
    data class OnDeleteCommandTemplates(val commandTemplates: Set<Long>) : CommandPageEvent
    data class OnUpdateCommandTemplate(val commandTemplate: CommandTemplate) : CommandPageEvent
    data class OnCommandTemplateSelected(val commandTemplate: CommandTemplate) : CommandPageEvent
    data class OnSearchQueryChange(val query: String) : CommandPageEvent
    data class OnSortingChange(val sorting: SORTING) : CommandPageEvent
    data class OnSortOrderChange(val sortOrder: CommandTemplateRepository.CommandTemplateSortType) :
        CommandPageEvent

    object OnSelectAllCommandTemplates : CommandPageEvent
    object OnClearSelection : CommandPageEvent
    object OnInvertSelection : CommandPageEvent


}

@Composable
fun CommandsPage(
    modifier: Modifier = Modifier,
    state: CommandTemplateUiState,
    onEvent: (CommandPageEvent) -> Unit,
    onBackClick: () -> Unit
) {

    val isInSelectionMenu = rememberSaveable(state.selectedIds) { state.selectedIds.isNotEmpty() }


    Scaffold(
        modifier = modifier,
        topBar = {
            SearchableTopAppBar(
                searchValue = state.query,
                title = stringResource(id = R.string.command_title),
                onBackClick = onBackClick,
                onSearchValueChange = { query -> onEvent(CommandPageEvent.OnSearchQueryChange(query)) },
            )
        },
        floatingActionButton = {
            if (!isInSelectionMenu) {
                FloatingActionButton(
                    onClick = { /* Handle add command button click */ }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_command)
                    )
                }
            }

        },
        bottomBar = {
            if (isInSelectionMenu) {
                CommandPageBottomNavigation(
                    modifier = Modifier,
                    onDeleteClick = { onEvent(CommandPageEvent.OnDeleteCommandTemplates(state.selectedIds)) },
                    onInvertSelectionClick = { onEvent(CommandPageEvent.OnInvertSelection) },
                    onSelectAllClick = { onEvent(CommandPageEvent.OnSelectAllCommandTemplates) },
                    onClearSelectionClick = { onEvent(CommandPageEvent.OnClearSelection) }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = state.templates,
                key = { item -> item.id }
            ) { commandTemplate ->
                val selected = state.selectedIds.contains(commandTemplate.id)
                CommandTemplateItem(
                    modifier = Modifier,
                    commandTemplate = commandTemplate,
                    selected = selected,
                    onClick = {
                        if (selected) {
                            onEvent(CommandPageEvent.OnCommandTemplateSelected(commandTemplate))
                        } else {
                            TODO("Not yet Implemented: // show bottom sheet with details")
                        }
                    },
                    onLongClick = { template ->
                        onEvent(CommandPageEvent.OnCommandTemplateSelected(template))
                    }
                )
            }
        }
    }
}

@Composable
fun CommandPageBottomNavigation(
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
    onInvertSelectionClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onClearSelectionClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = onSelectAllClick,
            label = { Text(text = stringResource(R.string.select_all)) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_select_all_24),
                    contentDescription = stringResource(R.string.select_all)
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = onInvertSelectionClick,
            label = { Text(text = stringResource(R.string.invert)) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_select_window_24),
                    contentDescription = stringResource(R.string.invert)
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = onClearSelectionClick,
            label = { Text(text = stringResource(R.string.clear)) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_deselect_24),
                    contentDescription = stringResource(R.string.clear)
                )
            }
        )
        NavigationBarItem(
            selected = false,
            label = { Text(text = stringResource(R.string.delete)) },
            onClick = onDeleteClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete)
                )
            }
        )

    }
}

@Preview
@Composable
fun CommandsPagePreview() {
    val templates = listOf(
        CommandTemplate(id = 1, title = "Template 1", content = "command 1"),
        CommandTemplate(id = 2, title = "Template 2", content = "command 2"),
        CommandTemplate(id = 3, title = "Template 3", content = "command 3")
    )
    val state = CommandTemplateUiState(
        templates = templates,
        query = "search",
        selectedIds = setOf(2L)
    )
    CommandsPage(
        state = state,
        onEvent = {},
        onBackClick = {}
    )
}

