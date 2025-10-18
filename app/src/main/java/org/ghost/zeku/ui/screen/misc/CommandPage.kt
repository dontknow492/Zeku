package org.ghost.zeku.ui.screen.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.ghost.zeku.R
import org.ghost.zeku.core.enum.SORTING
import org.ghost.zeku.database.models.CommandTemplate
import org.ghost.zeku.database.repository.CommandTemplateRepository
import org.ghost.zeku.ui.common.ErrorSnackBar
import org.ghost.zeku.ui.common.SelectedItemsIndicator
import org.ghost.zeku.ui.component.CommandTemplateItem
import org.ghost.zeku.ui.component.EmptyScreen
import org.ghost.zeku.ui.component.SearchableTopAppBar
import org.ghost.zeku.ui.component.SelectionBottomNavigation
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.viewModels.CommandTemplateUiState

sealed interface CommandPageEvent {
    data class OnDeleteCommandTemplates(val commandTemplates: Set<Long>) : CommandPageEvent
    data class OnUpdateCommandTemplate(val commandTemplate: CommandTemplate) : CommandPageEvent
    data class OnAddCommandTemplate(val commandTemplate: CommandTemplate) : CommandPageEvent
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
    var selectedTemplate by rememberSaveable { mutableStateOf<CommandTemplate?>(null) }
    var isAddSheetOpen by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(
                state.error,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                ErrorSnackBar(snackbarData = snackbarData)
            }
        },
        topBar = {
            if (isInSelectionMenu){
                SelectedItemsIndicator(
                    selectedIds = state.selectedIds,
                )
            }
            else{
                SearchableTopAppBar(
                    searchValue = state.query,
                    title = stringResource(id = R.string.command_title),
                    onBackClick = onBackClick,
                    onSearchValueChange = { query -> onEvent(CommandPageEvent.OnSearchQueryChange(query)) },
                )
            }

        },
        floatingActionButton = {
            if (!isInSelectionMenu) {
                FloatingActionButton(
                    onClick = { isAddSheetOpen = true }
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
                SelectionBottomNavigation(
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
            if (state.templates.isEmpty()) {
                item {
                    EmptyScreen(
                        model = R.drawable.dog,
                        text = stringResource(R.string.empty_command_template_text),
                        contentDescription = stringResource(R.string.empty_command_template),
                        button = {
                            Button(
                                onClick = { isAddSheetOpen = true }
                            ) {
                                Text(text = stringResource(R.string.add_command))
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = stringResource(R.string.add_command)
                                )
                            }
                        }
                    )
                }

            } else {
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
                            if (selected || isInSelectionMenu) {
                                onEvent(CommandPageEvent.OnCommandTemplateSelected(commandTemplate))
                            } else {
                                selectedTemplate = commandTemplate
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

    if (selectedTemplate != null) {
        EditCommandTemplateSheet(
            modifier = Modifier,
            value = state.templates.first(),
            onDismiss = { selectedTemplate = null },
            onValueChange = {
                onEvent(CommandPageEvent.OnUpdateCommandTemplate(it))
            }
        )
    } else if (isAddSheetOpen) {
        AddCommandTemplateSheet(
            modifier = Modifier,
            onDismiss = { isAddSheetOpen = false },
            onAddCommandTemplate = {
                onEvent(CommandPageEvent.OnAddCommandTemplate(it))
            }
        )
    }
}


@Composable
private fun CommandTemplateDetail(
    modifier: Modifier = Modifier,
    value: CommandTemplate,
    onValueChange: (CommandTemplate) -> Unit,
    title: @Composable () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()


    // Request focus for the title field when the sheet appears
    LaunchedEffect(Unit) {
        delay(100) // A small delay ensures the UI is ready
        focusRequester.requestFocus()
    }
    Column(
        modifier = Modifier
            // Add padding and handle system bars (like the keyboard)
            .padding(horizontal = 24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ### 1. Improved Header ###
        title()

        // ### 2. Better TextFields ###
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = value.title,
            onValueChange = { onValueChange(value.copy(title = it)) },
            label = { Text(stringResource(R.string.template_name)) },
            placeholder = { Text(stringResource(R.string.template_name_placeholder)) },
            singleLine = true
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value.content,
            onValueChange = { onValueChange(value.copy(content = it)) },
            label = { Text(stringResource(R.string.template_content)) },
            placeholder = { Text(stringResource(R.string.template_content_placeholder)) },
            minLines = 3 // Allow more space for longer commands
        )

        // ### 3. Section for Switches (Missing Options) ###
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.titleMedium
        )

        // Reusable composable for settings rows
        SwitchSettingItem(
            title = stringResource(R.string.preferred_template),
            description = stringResource(R.string.preferred_template_description),
            icon = Icons.Outlined.Delete,
            checked = value.preferredCommandTemplate,
            onSelectionChange = { onValueChange(value.copy(preferredCommandTemplate = it)) }
        )

        SwitchSettingItem(
            title = stringResource(R.string.use_as_extra_command),
            description = stringResource(R.string.use_as_extra_command_description),
            icon = Icons.Outlined.Delete,
            checked = value.useAsExtraCommand,
            onSelectionChange = { onValueChange(value.copy(useAsExtraCommand = it)) }
        )

        // Sub-options, only enabled if the parent is checked
        Column(modifier = Modifier.padding(start = 16.dp)) {
            SwitchSettingItem(
                title = stringResource(R.string.for_audio_downloads),
                description = stringResource(R.string.for_audio_download_placeholder),
                checked = value.useAsExtraCommandAudio,
                icon = Icons.Outlined.Delete,
                onSelectionChange = { onValueChange(value.copy(useAsExtraCommandAudio = it)) },
                enabled = value.useAsExtraCommand
            )
            SwitchSettingItem(
                title = stringResource(R.string.command_for_video_title),
                description = stringResource(R.string.command_for_video_description),
                checked = value.useAsExtraCommandVideo,
                icon = Icons.Outlined.Delete,
                onSelectionChange = { onValueChange(value.copy(useAsExtraCommandVideo = it)) },
                enabled = value.useAsExtraCommand
            )
            SwitchSettingItem(
                title = stringResource(R.string.command_for_data_fetching_title),
                description = stringResource(R.string.command_for_data_fetching_description),
                checked = value.useAsExtraCommandDataFetching,
                icon = Icons.Outlined.Delete,
                onSelectionChange = { onValueChange(value.copy(useAsExtraCommandDataFetching = it)) },
                enabled = value.useAsExtraCommand
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ### 4. Field for URL Regex List ###
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            // Join the list to a string for display, and split it back on change
            value = value.urlRegex.joinToString(", "),
            onValueChange = {
                val regexList = it.split(',')
                    .map { regex -> regex.trim() }
                    .filter { regex -> regex.isNotBlank() }
                    .toMutableList()
                onValueChange(value.copy(urlRegex = regexList))
            },
            label = { Text(stringResource(R.string.command_url_regex)) },
            placeholder = { Text(stringResource(R.string.command_url_regex_placeholder)) },
            supportingText = { Text(stringResource(R.string.command_url_regex_supporting_text)) }
        )

        // Add a spacer at the bottom for better padding
        Spacer(Modifier.height(16.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCommandTemplateSheet(
    modifier: Modifier = Modifier,
    value: CommandTemplate,
    onDismiss: () -> Unit,
    onValueChange: (CommandTemplate) -> Unit
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss
    ) {
        // Main column for sheet content, made scrollable
        CommandTemplateDetail(
            modifier = Modifier,
            value = value,
            onValueChange = onValueChange
        ) {
            Text(
                text = "Edit Template",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCommandTemplateSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onAddCommandTemplate: (CommandTemplate) -> Unit
) {
    var value by remember { mutableStateOf(CommandTemplate(id = 0, title = "", content = "")) }
    val onValueChange = { newValue: CommandTemplate -> value = newValue }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss
    ) {
        CommandTemplateDetail(
            modifier = Modifier,
            value = value,
            onValueChange = onValueChange
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_template),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { onAddCommandTemplate(value) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_save_24),
                        contentDescription = stringResource(R.string.add_command_template)
                    )
                    Text(text = stringResource(R.string.add))
                }
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ViewDetailedCategoryPreview() {
    val commandTemplate = CommandTemplate(
        id = 1,
        title = "My favorite song",
        content = "ytsearch: My favorite song"
    )
    EditCommandTemplateSheet(value = commandTemplate, onDismiss = {}, onValueChange = {})
    AddCommandTemplateSheet(onDismiss = {}, onAddCommandTemplate = {})
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
        selectedIds = setOf()
    )
    CommandsPage(
        state = state.copy(templates = emptyList()),
        onEvent = {},
        onBackClick = {}
    )
}

