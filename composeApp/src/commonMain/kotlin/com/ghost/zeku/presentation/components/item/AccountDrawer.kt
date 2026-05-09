package com.ghost.zeku.presentation.components.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.presentation.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherBottomSheet(
    isOpen: Boolean,
    currentUser: UserProfile?,
    allUsers: List<UserProfile>,
    onDismissRequest: () -> Unit,
    onSwitchAccount: (ProviderType) -> Unit,
    onAddAccountClick: () -> Unit,
    onLogoutClick: (ProviderType) -> Unit
) {
    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp) // Extra padding for navigation bar
            ) {
                Text(
                    text = "Switch Account",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                Divider()

                LazyColumn {
                    // 1. List all logged-in accounts
                    items(allUsers, key = { it.id }) { user ->
                        val isActive = user.id == currentUser?.id

                        AccountListItem(
                            user = user,
                            isActive = isActive,
                            onClick = {
                                if (!isActive) {
                                    onSwitchAccount(user.source)
                                    onDismissRequest()
                                }
                            },
                            onLogoutClick = {
                                onLogoutClick(user.source)
                                // Keep sheet open so they can see the account disappear
                            }
                        )
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // 2. Button to add a new account
                    item {
                        ListItem(
                            headlineContent = { Text("Add another account") },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add account",
                                    modifier = Modifier.padding(4.dp)
                                )
                            },
                            modifier = Modifier.clickable {
                                onAddAccountClick()
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DesktopUserProfileItem(
    currentUser: UserProfile?,
    allUsers: List<UserProfile>,
    onSwitchAccount: (ProviderType) -> Unit,
    onLogoutClick: (ProviderType) -> Unit,
    onAddAccountClick: () -> Unit,
    isSidebarExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    // Controls the visibility of the popup menu
    var isMenuExpanded by remember { mutableStateOf(!false) }

    Box(modifier = modifier) {
        // 1. The actual Sidebar Item
        Button(
            onClick = { isMenuExpanded = !isMenuExpanded },
        ) {
            Text(text = if (isMenuExpanded) "Menu" else "Collapse")
        }


        // 2. The sleek Desktop Dropdown Menu
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.medium)
        ) {
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                // Offsets the menu slightly so it floats nicely next to the sidebar
                offset = DpOffset(x = 16.dp, y = (-16).dp),
                modifier = Modifier.width(300.dp) // Keep it a fixed, clean width
            ) {
                Text(
                    text = "Switch Account",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                allUsers.forEach { user ->
                    val isActive = user.id == currentUser?.id

                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(user.username, style = MaterialTheme.typography.titleSmall)
                                Text(user.source.name, style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        leadingIcon = {
                            if (user.avatarUrl != null) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(32.dp).clip(CircleShape)
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp).padding(end = 8.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onLogoutClick(user.source) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Logout",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            if (!isActive) {
                                onSwitchAccount(user.source)
                                isMenuExpanded = false
                            }
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                DropdownMenuItem(
                    text = { Text("Add another account") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        onAddAccountClick()
                        isMenuExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AccountListItem(
    user: UserProfile,
    isActive: Boolean,
    onClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            // Shows "MYANIMELIST" or "ANILIST" below the username
            Text(text = user.source.name)
        },
        leadingContent = {
            // Profile Avatar
            if (user.avatarUrl != null) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active Account",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
                // Logout Button
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.clickable(onClick = onClick)
    )
}


@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun AccountListItemPreview() {
    val users = listOf(
        UserProfile(
            id = "1",
            source = ProviderType.MYANIMELIST,
            username = "alice",
            avatarUrl = "https://example.com/alice.png",
            bannerUrl = "https://example.com/alice/_banner.png"
        ),
        UserProfile(
            id = "2",
            source = ProviderType.ANILIST,
            username = "bob",
            avatarUrl = "https://example.com/bob.jpg",
            bannerUrl = null
        ),
    )
    AppTheme {
        DesktopUserProfileItem(
            currentUser = users[0],
            allUsers = users,
            onSwitchAccount = {},
            onLogoutClick = {},
            onAddAccountClick = {},
            isSidebarExpanded = true,

            )
//        AccountSwitcherBottomSheet(
//            isOpen = true,
//            currentUser = users.first(),
//            allUsers = users,
//            onDismissRequest = {},
//            onSwitchAccount = {},
//            onLogoutClick = {},
//            onAddAccountClick = {  }
//        )
    }
}