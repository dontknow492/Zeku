package com.ghost.zeku

// Navigation 3 Imports

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.ghost.zeku.presentation.components.sidebar.ZekuAdaptiveSidebar
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

// ============================================================================
// 1. DESTINATIONS & ROUTING (Nav 3 Architecture)
// ============================================================================


@Serializable
data object AnimeHome : NavKey

@Serializable
data object MangaHome : NavKey

@Serializable
data object Search : NavKey

@Serializable
data object Library : NavKey

@Serializable
data class MediaDetails(val id: Int) : NavKey

enum class TopLevelDestination(
    val routeInstance: NavKey,
    val routeClass: KClass<out NavKey>,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val title: String
) {
    ANIME(
        routeInstance = AnimeHome,
        routeClass = AnimeHome::class,
        selectedIcon = Icons.Filled.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle,
        title = "Anime"
    ),
    MANGA(
        routeInstance = MangaHome,
        routeClass = MangaHome::class,
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        title = "Manga"
    ),
    SEARCH(
        routeInstance = Search,
        routeClass = Search::class,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        title = "Search"
    ),
    LIBRARY(
        routeInstance = Library,
        routeClass = Library::class,
        selectedIcon = Icons.Filled.Bookmarks,
        unselectedIcon = Icons.Outlined.Bookmarks,
        title = "Library"
    )
}

// ============================================================================
// 2. STATE MANAGEMENT (The "Brain" of the Wrapper)
// ============================================================================

@Composable
fun rememberZekuAppState(
    backStack: NavBackStack<NavKey> = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AnimeHome::class, AnimeHome.serializer())
                    subclass(MangaHome::class, MangaHome.serializer())
                    subclass(Search::class, Search.serializer())
                    subclass(Library::class, Library.serializer())
                    subclass(Library::class, Library.serializer())
                }
            }
        },
        AnimeHome
    ),
    screenWidthDp: Int
): ZekuAppState {
    return remember(backStack, screenWidthDp) {
        ZekuAppState(backStack, screenWidthDp)
    }
}

@Stable
class ZekuAppState(
    val backStack: NavBackStack<NavKey>,
    private val screenWidthDp: Int
) {
    val currentDestination: NavKey?
        get() = backStack.lastOrNull() // Nav3 backstack resolves current destination at the end of the stack

    val currentTopLevelDestination: TopLevelDestination?
        get() {
            val current = currentDestination ?: return null
            return TopLevelDestination.entries.firstOrNull { topLevel ->
                current::class == topLevel.routeClass
            }
        }

    // Adaptive Layout Breakpoints (Material 3 Standards)
    val isCompactScreen: Boolean get() = screenWidthDp < 600  // Phones
    val isMediumScreen: Boolean get() = screenWidthDp in 600..839 // Tablets / Small Desktop
    val isExpandedScreen: Boolean get() = screenWidthDp >= 840 // Large Desktop / Wide monitors

    // Visibility Rules
    val shouldShowBottomBar: Boolean
        get() = isCompactScreen && isTopLevelDestination

    // Desktop & Tablet both use the adaptive side bar
    val shouldShowSideNavigation: Boolean
        get() = isMediumScreen || isExpandedScreen

    val canGoBack: Boolean
        get() = backStack.size > 1

    private val isTopLevelDestination: Boolean
        get() = currentTopLevelDestination != null

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        // Clear backstack to root and add top level tab
        backStack.clear()
        backStack.add(topLevelDestination.routeInstance)
    }

    fun navigateTo(key: NavKey) {
        backStack.add(key)
    }

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}

// ============================================================================
// 3. THE MASTER WRAPPER
// ============================================================================

@Composable
fun ZekuAppWrapper() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val appState = rememberZekuAppState(
            screenWidthDp = maxWidth.value.toInt()
        )

        // Remember whether the user has toggled the sidebar to be wide or narrow.
        // Defaults to expanded on large screens, collapsed on tablets.
        var isSidebarExpanded by rememberSaveable(appState.isExpandedScreen) {
            mutableStateOf(appState.isExpandedScreen)
        }

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = appState.shouldShowBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    ZekuBottomNavigationBar(appState)
                }
            },
            snackbarHost = { SnackbarHost(SnackbarHostState()) }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Adaptive Animated Sidebar
                AnimatedVisibility(
                    visible = appState.shouldShowSideNavigation,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                ) {
                    ZekuAdaptiveSidebar(
                        currentDestination = appState.currentTopLevelDestination,
                        expanded = isSidebarExpanded,
                        expandEnabled = appState.isExpandedScreen,
                        canGoBack = appState.canGoBack,
                        onToggleExpanded = { isSidebarExpanded = !isSidebarExpanded },
                        onNavigate = { destination ->
                            appState.navigateToTopLevelDestination(destination)
                        },
                        onBackPressed = {
                            appState.popBackStack()
                        },
                        onLogoClick = { TODO("Implement: Open app site") }
                    )
                }

                // Content is rendered via NavDisplay
                ZekuNavDisplay(
                    appState = appState,
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
                is AnimeHome -> NavEntry(key) {
                    MockScreen("${navBackStack.size}: Anime Home") { id ->
                        appState.navigateTo(MediaDetails(id = id))
                    }
                }

                is MangaHome -> NavEntry(key) {
                    MockScreen("${navBackStack.size}: Manga Home") { id ->
                        appState.navigateTo(MediaDetails(id = id))
                    }
                }

                is Search -> NavEntry(key) {
                    MockScreen("${navBackStack.size}: Search") {}
                }

                is Library -> NavEntry(key) {
                    MockScreen("${navBackStack.size}: Library") {}
                }

                is MediaDetails -> NavEntry(key) {
                    MockScreen("${navBackStack.size}: MediaDetails") {}
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