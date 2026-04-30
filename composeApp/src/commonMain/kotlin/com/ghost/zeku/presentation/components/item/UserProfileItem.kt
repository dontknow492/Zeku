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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ghost.zeku.data.remote.anilist.model.Avatar
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.presentation.theme.AppTheme


@Composable
fun UserProfileItem(
    user: UserProfile?,
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
        onClick = {},
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
            ExpandedContent(user, onAvatarClick, onLogout)
        } else {
            CollapsedContent(user, onAvatarClick, onLogout)
        }
    }
}


@Composable
private fun ExpandedContent(
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

            Text(
                text = "View profile",
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
private fun CollapsedContent(
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
private fun Avatar(
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


@Preview(showBackground = true)
@Composable
fun UserProfileItemPreview() {
    val user = UserProfile(
        id = "12345",
        username = "User",
        source = ProviderType.MYANIMELIST,
        avatarUrl = "https://picsum.photos/200"
    )
    AppTheme {
        Column {
            UserProfileItem(user = user, onLogout = {}, onAvatarClick = {}, true)
            UserProfileItem(user = user, onLogout = {}, onAvatarClick = {}, false)
        }
    }
}