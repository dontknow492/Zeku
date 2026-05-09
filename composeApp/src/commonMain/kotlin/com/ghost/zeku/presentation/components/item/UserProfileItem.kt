package com.ghost.zeku.presentation.components.item

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.ProviderType
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.Res
import zeku.composeapp.generated.resources.unknown

@Composable
fun UserProfileWithDropdown(
    currentUser: UserProfile?,
    allAccounts: List<UserProfile>, // includes current
    expanded: Boolean, // ui expanded not drop down
    onAccountSwitch: (UserProfile) -> Unit,
    onAddAccount: () -> Unit,
    onLogout: (UserProfile) -> Unit,
    onAvatarClick: (UserProfile) -> Unit, // kept as separate for avatar click if they still want it
    modifier: Modifier = Modifier
) {
    var dropDownExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Trigger row
        UserProfileItem(
            user = currentUser,
            onClick = { dropDownExpanded = !dropDownExpanded },
            onLogout = {
                if (currentUser != null) {
                    onLogout(currentUser)
                }
            },
            onAvatarClick = {
                if (currentUser != null) {
                    onAvatarClick(currentUser)
                }
            },
            modifier = Modifier,
            isExpanded = expanded
        )

        // Dropdown menu
        DropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { dropDownExpanded = false },
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

            allAccounts.forEach { user ->
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
                            Avatar(user, onClick = { onAvatarClick(user) }, size = 32.dp)
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
                                onClick = { onLogout(user) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        if (!isActive) {
                            onAccountSwitch(user)
                            dropDownExpanded = false
                        }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            DropdownMenuItem(
                text = { Text("Add another account") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = {
                    onAddAccount()
                    dropDownExpanded = false
                }
            )
        }
    }
}


@Composable
fun UserProfileItem(
    user: UserProfile?,
    onClick: () -> Unit,
    onLogout: () -> Unit,
    onAvatarClick: () -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val isPressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.97f
            isHovered -> 1.02f
            else -> 1f
        }
    )

    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
        alpha = if (isHovered) 0.35f else 0.22f
    )

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {

        if (isExpanded) {
            ExpandedUserContent(user, onAvatarClick, onLogout)
        } else {
            CollapsedUserContent(user, onAvatarClick, onLogout)
        }
    }
}


@Composable
private fun ExpandedUserContent(
    user: UserProfile?,
    onAvatarClick: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Avatar(
            user = user,
            onClick = onAvatarClick,
            size = 42.dp
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {

            Text(
                text = user?.username ?: "Guest",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(4.dp))
            // Source pill / label

            Text(
                text = user?.source?.name ?: stringResource(Res.string.unknown),
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }


        // 🔥 Better logout (icon button, not stretched text button)
        IconButton(
            onClick = onLogout
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
            )
        }
    }
}


@Composable
private fun CollapsedUserContent(
    user: UserProfile?,
    onAvatarClick: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Avatar(
            user = user,
            onClick = onAvatarClick,
            size = 40.dp
        )

        Spacer(Modifier.height(6.dp))

        IconButton(
            onClick = onLogout,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


@Composable
fun Avatar(
    user: UserProfile?,
    onClick: () -> Unit,
    size: Dp
) {
    val initials = user?.username
        ?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.uppercase() }
        ?.take(2)
        ?.joinToString("") ?: "?"

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                2.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {

        if (!user?.avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = user?.avatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            // ✨ fallback: initials → icon
            Text(
                text = initials,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserProfileItemPreview() {
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

    UserProfileWithDropdown(
        currentUser = users[0],
        allAccounts = users,
        expanded = false,
        onAccountSwitch = {},
        onLogout = {},
        onAvatarClick = {},
        onAddAccount = {}
    )

}