package org.ghost.zeku.ui.screen.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import org.ghost.zeku.database.models.CookieItem
import org.ghost.zeku.ui.component.CookieListItem
import org.ghost.zeku.ui.component.EmptyScreen
import org.ghost.zeku.ui.component.SelectionBottomNavigation
import org.ghost.zeku.ui.component.SimpleTopAppBar
import org.ghost.zeku.viewModels.CookiesUiState

sealed interface CookiesPageEvent {
    data class OnDeleteCookieItem(val commandTemplates: Set<Long>) : CookiesPageEvent
    data class OnUpdateCookieItem(val cookieItem: CookieItem) : CookiesPageEvent
    data class OnAddCookieItem(val cookieItem: CookieItem) : CookiesPageEvent
    data class OnEnableChange(val commandTemplate: CookieItem, val enable: Boolean) :
        CookiesPageEvent

    data class OnCookieItemSelected(val commandTemplate: CookieItem) : CookiesPageEvent

    object OnSelectAllCookieItem : CookiesPageEvent
    object OnClearSelection : CookiesPageEvent
    object OnInvertSelection : CookiesPageEvent
}

@Composable
fun CookiesPage(
    modifier: Modifier = Modifier,
    state: CookiesUiState,
    onEvent: (CookiesPageEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedCookie by rememberSaveable { mutableStateOf<CookieItem?>(null) }
    var isAddSheetOpen by rememberSaveable { mutableStateOf(false) }
    val isInSelectionMenu = rememberSaveable(state.selectedIds) { state.selectedIds.isNotEmpty() }

    Scaffold(
        modifier = modifier,
        topBar = {
            SimpleTopAppBar(
                title = stringResource(R.string.title_authentication_cookies),
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            if (!isInSelectionMenu) {
                FloatingActionButton(
                    onClick = { isAddSheetOpen = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.cookies_label)
                    )
                }
            }

        },
        bottomBar = {
            if (isInSelectionMenu) {
                SelectionBottomNavigation(
                    modifier = Modifier,
                    onDeleteClick = { onEvent(CookiesPageEvent.OnDeleteCookieItem(state.selectedIds)) },
                    onInvertSelectionClick = { onEvent(CookiesPageEvent.OnInvertSelection) },
                    onSelectAllClick = { onEvent(CookiesPageEvent.OnSelectAllCookieItem) },
                    onClearSelectionClick = { onEvent(CookiesPageEvent.OnClearSelection) }
                )
            }
        }

    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (state.cookies.isEmpty()) {
                item {
                    EmptyScreen(
                        model = R.drawable.cat,
                        text = stringResource(R.string.empty_cookie_text),
                        contentDescription = stringResource(R.string.empty_cookies),
                        button = {
                            Button(
                                onClick = { isAddSheetOpen = true }
                            ) {
                                Text(text = stringResource(R.string.add_cookies))
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = stringResource(R.string.add_cookies)
                                )
                            }
                        }
                    )
                }
            } else {
                items(state.cookies, key = { it.id }) { item ->
                    val selected = state.selectedIds.contains(item.id)
                    CookieListItem(
                        cookie = item,
                        selected = selected,
                        onClick = {
                            if (selected) {
                                onEvent(CookiesPageEvent.OnCookieItemSelected(item))
                            } else {
                                selectedCookie = item
                            }
                        },
                        onLongClick = { item ->
                            onEvent(CookiesPageEvent.OnCookieItemSelected(item))
                        },
                        onEnableChange = { item, enable ->
                            onEvent(CookiesPageEvent.OnEnableChange(item, enable))
                        }
                    )
                }
            }

        }
    }

    if (selectedCookie != null) {
        EditCookieBottomSheet(
            value = selectedCookie!!,
            onValueChange = { onEvent(CookiesPageEvent.OnUpdateCookieItem(it)) },
            onDismiss = { selectedCookie = null }
        )
    } else if (isAddSheetOpen) {
        AddCookieBottomSheet(
            onAddNewCookie = { onEvent(CookiesPageEvent.OnAddCookieItem(it)) },
            onDismiss = { isAddSheetOpen = false },
            modifier = Modifier
        )
    }

}


@Composable
private fun CookieDetailView(
    modifier: Modifier = Modifier,
    value: CookieItem,
    onValueChange: (CookieItem) -> Unit,
    title: @Composable () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()


    // Request focus for the title field when the sheet appears
    LaunchedEffect(Unit) {
        delay(100) // A small delay ensures the UI is ready
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            // Add padding and handle system bars (like the keyboard)
            .padding(horizontal = 24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        title()
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = value.url,
            onValueChange = { onValueChange(value.copy(url = it)) },
            label = { Text(stringResource(R.string.cookie_url_label)) },
            placeholder = { Text(stringResource(R.string.cookie_placeholder)) },
            singleLine = true
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value.content,
            onValueChange = { onValueChange(value.copy(content = it)) },
            label = { Text(stringResource(R.string.content)) },
            placeholder = { Text(stringResource(R.string.cookie_content_placeholder)) },
            minLines = 3 // Allow more space for longer commands
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCookieBottomSheet(
    modifier: Modifier = Modifier,
    value: CookieItem,
    onValueChange: (CookieItem) -> Unit,
    onDismiss: () -> Unit,
) {

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss
    ) {
        CookieDetailView(
            value = value,
            onValueChange = onValueChange,
        ) {
            Text(
                text = stringResource(R.string.edit_cookie),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCookieBottomSheet(
    modifier: Modifier = Modifier,
    onAddNewCookie: (CookieItem) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf(CookieItem(id = 0, url = "", content = "")) }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss
    ) {
        CookieDetailView(
            value = value,
            onValueChange = { value = it },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.new_cookie),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        onAddNewCookie(value)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_save_24),
                        contentDescription = stringResource(R.string.new_cookie)
                    )
                    Text(text = stringResource(R.string.add))
                }
            }
        }
    }
}


@Preview
@Composable
fun CookiesPagePreview() {
    val cookies = listOf(
        CookieItem(
            id = 1,
            url = "https://example.com",
            content = "cookie1=value1",
            description = "Example Cookie 1",
            enabled = true
        ),
        CookieItem(
            id = 2,
            url = "https://test.com",
            content = "cookie2=value2",
            description = "Test Cookie 2",
            enabled = false
        ),
        CookieItem(
            id = 3,
            url = "https://anothersite.org",
            content = "cookie3=value3",
            description = "Another Site Cookie 3",
            enabled = true
        )
    )
    val state = CookiesUiState(
        cookies = cookies,
        selectedIds = setOf(1L)
    )
    CookiesPage(state = state, onEvent = {}, onBackClick = {})
}


@Preview
@Composable
fun CookiesPageEmptyPreview() {
    val state = CookiesUiState(
        cookies = emptyList(),
        selectedIds = emptySet()
    )
    CookiesPage(state = state, onEvent = {}, onBackClick = {})
}