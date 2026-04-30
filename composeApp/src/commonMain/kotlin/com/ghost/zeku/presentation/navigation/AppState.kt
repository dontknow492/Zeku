package com.ghost.zeku.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.aakira.napier.Napier
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun rememberZekuAppState(
    backStack: NavBackStack<NavKey> = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AnimeHomeRoute::class, AnimeHomeRoute.serializer())
                    subclass(MangaHomeRoute::class, MangaHomeRoute.serializer())
                    subclass(SearchRoute::class, SearchRoute.serializer())
                    subclass(LibraryRoute::class, LibraryRoute.serializer())
                    subclass(LibraryRoute::class, LibraryRoute.serializer())
                }
            }
        },
        AnimeHomeRoute
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

    fun navigateToDestination(destination: Destination) {
        Napier.d("Navigating to $destination")
        when (destination) {
            is Destination.AllCharacters -> TODO()
            is Destination.AllRecommendations -> TODO()
            is Destination.AllRelations -> TODO()
            is Destination.AllReviews -> TODO()
            is Destination.CharacterDetail -> TODO()
            is Destination.EpisodeDetail -> TODO()
            is Destination.MediaDetail -> navigateTo(MediaDetailsRoute(destination.id, destination.type))
            is Destination.Search -> navigateTo(SearchRoute)
            is Destination.ViewAllCategories -> navigateTo(
                AllCategoriesRoute(
                    categoryId = destination.categoryId,
                    type = destination.type,
                    title = destination.title
                )
            )

            Destination.Back -> popBackStack()
        }
    }

    fun navigateTo(key: NavKey) {
        backStack.add(key)
    }

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun resetStack() {
        backStack.clear()
    }
}