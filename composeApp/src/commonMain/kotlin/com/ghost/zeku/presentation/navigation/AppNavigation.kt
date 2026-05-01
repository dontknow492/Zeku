package com.ghost.zeku.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.presentation.components.sidebar.ZekuAdaptiveSidebar
import com.ghost.zeku.presentation.screen.category.CategoryScreen
import com.ghost.zeku.presentation.screen.details.DetailScreen
import com.ghost.zeku.presentation.screen.home.MediaHomeScreen
import com.ghost.zeku.presentation.screen.search.SearchScreen
import com.ghost.zeku.presentation.viewmodel.main.MainContract
import com.ghost.zeku.presentation.viewmodel.main.MainViewModel
import com.ghost.zeku.presentation.viewmodel.search.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun ZekuAppWrapper(
    viewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val layoutDirection = LocalLayoutDirection.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val uiState = rememberZekuAppState(
            screenWidthDp = maxWidth.value.toInt()
        )

        LaunchedEffect(state.currentUser) {
            uiState.resetStack()
            uiState.navigateTo(AnimeHomeRoute)
        }

        // Remember whether the user has toggled the sidebar to be wide or narrow.
        // Defaults to expanded on large screens, collapsed on tablets.
        var isSidebarExpanded by rememberSaveable(uiState.isExpandedScreen) {
            mutableStateOf(uiState.isExpandedScreen)
        }

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = uiState.shouldShowBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    ZekuBottomNavigationBar(uiState)
                }
            },
            snackbarHost = { SnackbarHost(SnackbarHostState()) }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) {
                // Adaptive Animated Sidebar
                AnimatedVisibility(
                    visible = uiState.shouldShowSideNavigation,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                ) {
                    ZekuAdaptiveSidebar(
                        currentDestination = uiState.currentTopLevelDestination,
                        expanded = isSidebarExpanded,
                        expandEnabled = uiState.isExpandedScreen,
                        canGoBack = uiState.canGoBack,
                        onToggleExpanded = { isSidebarExpanded = !isSidebarExpanded },
                        onNavigate = { destination ->
                            uiState.navigateToTopLevelDestination(destination)
                        },
                        onBackPressed = {
                            uiState.popBackStack()
                        },
                        onLogoClick = { viewModel.onEvent(MainContract.Event.OpenZekuSite) },
                        currentUser = state.currentUser,
                        allUsers = state.availableUsers,
                        onAccountSwitch = { viewModel.onEvent(MainContract.Event.SwitchAccount(it)) },
                        onLogoutClick = { viewModel.onEvent(MainContract.Event.Logout(it)) },
                        onAddAccountClick = { viewModel.onEvent(MainContract.Event.AddAccountClick) },
                        onAvatarClick = { viewModel.onEvent(MainContract.Event.ViewAccount(it)) },
                    )
                }

                // Content is rendered via NavDisplay
                ZekuNavDisplay(
                    appState = uiState,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }


}

// ============================================================================
// 4. NAVIGATION COMPONENTS (Mobile, Tablet, Desktop)
// ============================================================================

@Composable
private fun ZekuBottomNavigationBar(appState: ZekuAppState) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val isSelected = appState.currentTopLevelDestination == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { appState.navigateToTopLevelDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.title
                    )
                },
                label = { Text(destination.title) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}


// ============================================================================
// 5. NAV DISPLAY & CONTENT (Nav 3 Native)
// ============================================================================

@Composable
fun ZekuNavDisplay(
    appState: ZekuAppState,
    modifier: Modifier = Modifier
) {
    // NavDisplay resolves the backstack natively and triggers the composable block for the current key
    val navBackStack = appState.backStack
    NavDisplay(
        backStack = navBackStack,
        modifier = modifier,
        onBack = {
            // Only allow back if not at login
            if (navBackStack.size > 1) {
                navBackStack.removeAt(navBackStack.lastIndex)
            }
        },
        entryProvider = { key ->
            when (key) {
                is AnimeHomeRoute -> NavEntry(key) {
                    MediaHomeScreen(
                        viewModel = koinViewModel(),
                        mediaType = MediaType.ANIME,
                        onNavigate = appState::navigateToDestination
                    )
                }

                is MangaHomeRoute -> NavEntry(key) {
                    MediaHomeScreen(
                        viewModel = koinViewModel(),
                        mediaType = MediaType.MANGA,
                        onNavigate = appState::navigateToDestination
                    )
                }

                is SearchRoute -> NavEntry(key) {
                    SearchScreen(koinViewModel(), onNavigate = appState::navigateToDestination)
                }

                is LibraryRoute -> NavEntry(key) {
                    MockScreen("${navBackStack.size}: Library") {}
                }

                is MediaDetailsRoute -> NavEntry(key) {
                    DetailScreen(
                        mediaId = key.id,
                        mediaType = key.type,
                        onNavigate = appState::navigateToDestination,
                        onBack = appState::popBackStack
                    )
                }

                is AllCategoriesRoute -> NavEntry(key) {
                    CategoryScreen(
                        viewModel = koinViewModel(),
                        categoryId = key.categoryId,
                        title = key.title,
                        mediaType = key.type,
                        onNavigate = appState::navigateToDestination,
                        onBack = appState::popBackStack,
                    )
                }

                else -> throw IllegalStateException("Unexpected key $key")
            }
        },
//        transitionSpec = {
//            slideInHorizontally(
//                animationSpec = tween(600, easing = EaseInOutCubic),
//                initialOffsetX = { it }
//            ) togetherWith slideOutHorizontally(
//                animationSpec = tween(600, easing = EaseInOutCubic),
//                targetOffsetX = { -it }
//            )
//        },
//        popTransitionSpec = {
//            slideInHorizontally(
//                animationSpec = tween(600, easing = EaseInOutCubic),
//                initialOffsetX = { -it }
//            ) togetherWith slideOutHorizontally(
//                animationSpec = tween(600, easing = EaseInOutCubic),
//                targetOffsetX = { it }
//            )
//        },
    )
}

// --- MOCK SCREENS TO PROVE IT WORKS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockScreen(title: String, onNavigateToDetails: (Int) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(title) })
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This is the $title screen", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onNavigateToDetails(101) }) {
                    Text("Open Details (ID: 101)")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockDetailsScreen(id: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Watching Media ID: $id", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Notice how the bottom bar disappeared on mobile, but the sidebar remains on desktop!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}


/**
 * Smart navigation that prevents duplicate screens and manages the back stack intelligently.
 *
 * Rules:
 * - If the target route is already at the top, do nothing
 * - If the target route exists in the stack, pop back to it
 * - Otherwise, push the new route
 */
private fun navigateSmartly(backStack: MutableList<NavKey>, targetRoute: NavKey) {
    // If already on this screen, do nothing
    if (backStack.lastOrNull() == targetRoute) {
        return
    }

    // Find if target route exists in the stack
    val existingIndex = backStack.indexOfLast { it::class == targetRoute::class }

    if (existingIndex != -1) {
        // Pop back to the existing route (remove everything after it)
        while (backStack.size > existingIndex + 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    } else {
        // New route, add it
        backStack.add(targetRoute)
    }
}