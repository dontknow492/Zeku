package com.ghost.zeku.presentation.components.sidebar

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.presentation.components.item.UserProfileItem
import com.ghost.zeku.presentation.navigation.TopLevelDestination
import com.ghost.zeku.presentation.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.Res
import zeku.composeapp.generated.resources.app_name


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ZekuAdaptiveSidebar(
    currentDestination: TopLevelDestination?,
    expanded: Boolean,
    expandEnabled: Boolean,
    canGoBack: Boolean,
    onNavigate: (topLevelDestination: TopLevelDestination) -> Unit,
    onToggleExpanded: () -> Unit,
    onBackPressed: () -> Unit,
    onLogoClick: () -> Unit,
    userProfile: UserProfile? = null,
) {
    // Determine whether the sidebar should be permanently expanded
//    val autoExpanded = appState.isMediumScreen || appState.isExpandedScreen
    // On compact screens, user can manually toggle; larger screens are always expanded
    val expanded = expanded && expandEnabled


    // Smoothly animate width between collapsed Rail (80dp) and expanded Drawer (260dp)
    val sidebarWidth by animateDpAsState(
        targetValue = if (expanded) 260.dp else 80.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "sidebar_width_anim"
    )

    Surface(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            // 1. Header: Hamburger toggle (compact only) + Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle button always visible, but only active on compact screens
                IconButton(
                    onClick = onToggleExpanded,
                    enabled = expandEnabled   // disable interaction when auto-expanded
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.MenuOpen else Icons.Filled.Menu,
                        contentDescription = if (expanded) "Collapse sidebar" else "Expand sidebar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Animated logo/name area – uses shared axis for smoother reveal
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(animationSpec = tween(200)) +
                            expandHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(150)) +
                            shrinkHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = onLogoClick,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Animation,
                                contentDescription = "Zeku Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.app_name),
                                maxLines = 1,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 2. Back button (only when history available)
            AnimatedVisibility(
                visible = canGoBack,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    SidebarItem(
                        onClick = onBackPressed,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = "Back",
                        expanded = expanded,
                        isSelected = false,   // back button never “selected”
                        badge = null,
                        compactLabel = null
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // 3. Navigation destinations
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = currentDestination == destination
                SidebarItem(
                    onClick = { onNavigate(destination) },
                    icon = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                    label = destination.title,
                    expanded = expanded,
                    isSelected = isSelected,
                    badge = null,
                    compactLabel = null
                )
            }

            Spacer(Modifier.weight(1f))
            AnimatedVisibility(
                userProfile != null,
            ) {
                UserProfileItem(
                    user = userProfile,           // nullable UserProfile
                    onLogout = { /* handle logout */ },
                    onAvatarClick = { /* navigate to profile */ },
                    isExpanded = expanded,
                    modifier = Modifier
                )
            }

        }
    }
}

@Composable
private fun SidebarItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    expanded: Boolean,
    isSelected: Boolean,
    badge: @Composable (() -> Unit)? = null,
    compactLabel: String? = null
) {
    // Animate selected state
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        animationSpec = tween(200),
        label = "container_color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "content_color"
    )

    BadgedBox(
        badge = {
            badge?.invoke()
        },
        modifier = modifier
    ) {
        // Ripple + elevation for selected item
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(50),
            color = containerColor,
            enabled = true
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = if (expanded) 16.dp else 0.dp,
                    vertical = 12.dp
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center
            ) {
                // Icon with optional tiny selection indicator in collapsed mode
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    // Show a small dot when selected and collapsed
                    if (isSelected && !expanded) {
                        Canvas(modifier = Modifier.size(4.dp).offset(y = (-12).dp)) {
                            drawCircle(color = contentColor)
                        }
                    }
                }

                // Animated label (shared axis + fade for a polished reveal)
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(animationSpec = tween(200)) +
                            expandHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(150)) +
                            shrinkHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing))
                ) {
                    Row {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = label,
                            maxLines = 1,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun PreviewApp() {
    var currentDestination by remember { mutableStateOf(TopLevelDestination.ANIME) }
    var expanded by remember { mutableStateOf(!false) }
    AppTheme {
        ZekuAdaptiveSidebar(
            currentDestination = currentDestination,
            expanded = expanded,
            expandEnabled = true,
            canGoBack = true,
            onToggleExpanded = { expanded = !expanded },
            onNavigate = { currentDestination = it },
            onBackPressed = { },
            onLogoClick = {},
            userProfile = UserProfile(
                id = "12345",
                username = "User",
                source = ProviderType.MYANIMELIST,
                avatarUrl = "https://picsum.photos/200"
            )
        )
    }
}