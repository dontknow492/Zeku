package com.ghost.zeku.presentation.screen.library

// CategoryScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghost.zeku.data.local.room.DefaultCategories
import com.ghost.zeku.data.local.room.entities.LibraryCategoryEntity
import com.ghost.zeku.presentation.theme.AppTheme
import com.ghost.zeku.presentation.viewmodel.library.LibraryCategoryContract
import com.ghost.zeku.presentation.viewmodel.library.LibraryCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryCategoryScreen(
    viewModel: LibraryCategoryViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Collect effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryCategoryContract.Effect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                LibraryCategoryContract.Effect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }


    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<LibraryCategoryEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        CategoryScreenContent(
            modifier = Modifier.fillMaxSize().padding(padding),
            state = state,
            onEvent = viewModel::handleEvent,
            onEdit = { editingCategory = it },
        )
    }

    // Create dialog
    if (showCreateDialog) {
        CategoryDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { item ->
                viewModel.handleEvent(
                    LibraryCategoryContract.Event.CreateCategory(item)
                )
                showCreateDialog = false
            }
        )
    }

    // Edit dialog
    editingCategory?.let { category ->
        CategoryDialog(
            initial = category,
            onDismiss = { editingCategory = null },
            onConfirm = { item ->
                val updated = category.copy(
                    name = item.name,
                    type = item.type,
                    displayName = item.displayName,
                    color = item.color,
                    icon = item.icon
                )
                viewModel.handleEvent(LibraryCategoryContract.Event.UpdateCategory(updated))
                editingCategory = null
            }
        )
    }
}


@Composable
private fun CategoryScreenContent(
    modifier: Modifier = Modifier,
    state: LibraryCategoryContract.State,
    onEdit: (LibraryCategoryEntity) -> Unit,
    onEvent: (LibraryCategoryContract.Event) -> Unit,
) {
    // Dialogs state

    Box(modifier = modifier) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories, key = { it }) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = category.id == state.selectedCategory?.id,
                        onSelect = {
                            onEvent(LibraryCategoryContract.Event.SelectCategory(category))
                        },
                        onToggleVisibility = { visible ->
                            onEvent(
                                LibraryCategoryContract.Event.ToggleVisibility(category.id, visible)
                            )
                        },
                        onEdit = { onEdit(category) },
                        onDelete = {
                            onEvent(LibraryCategoryContract.Event.DeleteCategory(category.id))
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun CategoryItem(
    category: LibraryCategoryEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleVisibility: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            category.color?.let { hex ->
                Surface(
                    modifier = Modifier.size(16.dp),
                    shape = MaterialTheme.shapes.small,
                    color = try {
                        Color(hex.toARGB())
                    } catch (e: Exception) {
                        Color.Gray
                    }
                ) { /* just color */ }
                Spacer(Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.displayName ?: category.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (category.description != null) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (category.isDefault) "Default" else "Custom",
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (category.type != null) {
                        Text(
                            text = " · ${category.type.name}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Visibility toggle
            IconToggleButton(
                checked = category.isVisible,
                onCheckedChange = onToggleVisibility
            ) {
                Icon(
                    imageVector = if (category.isVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle visibility"
                )
            }

            // Edit button
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }

            // Delete button (disabled for default categories)
            IconButton(
                onClick = onDelete,
                enabled = !category.isDefault
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (category.isDefault) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    initial: LibraryCategoryEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (LibraryCategoryEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type) }
    var displayName by remember { mutableStateOf(initial?.displayName ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Create Category" else "Edit Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name (optional)") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color (hex, optional)") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Icon name (optional)") },
                    singleLine = true
                )
                // MediaType dropdown could be added here; for simplicity we skip
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            LibraryCategoryEntity(
                                name = name,
                                type = type,
                                displayName = displayName,
                                color = color,
                                icon = icon
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


fun String.toARGB(): Int {
    return Color.Red.toArgb()
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CategoryDialogPreview() {
    AppTheme {
        CategoryDialog(
            initial = DefaultCategories.SYSTEM_CATEGORIES.first(),
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CategoryItemPreview() {
    val state = LibraryCategoryContract.State(
        categories = DefaultCategories.SYSTEM_CATEGORIES,
        isLoading = false,
        selectedCategory = null,
    )
    AppTheme {
        CategoryScreenContent(
            state = state,
            onEvent = {},
            onEdit = {}
        )
    }

}