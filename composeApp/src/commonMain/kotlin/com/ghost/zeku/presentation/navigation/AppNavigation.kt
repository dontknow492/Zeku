package com.ghost.zeku.presentation.navigation

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.presentation.screen.home.MediaHomeScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
) {
    val navBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AnimeHomeRoute::class, AnimeHomeRoute.serializer())
                    subclass(MangaHomeRoute::class, MangaHomeRoute.serializer())
                    subclass(SearchRoute::class, SearchRoute.serializer())
                }
            }
        },
        AnimeHomeRoute // Always start at login
    )


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
                    MediaHomeScreen(MediaType.ANIME, koinViewModel(), {})
                }

                is MangaHomeRoute -> NavEntry(key) {
                    MediaHomeScreen(MediaType.MANGA, koinViewModel(), {})
                }

                is SearchRoute -> NavEntry(key) {
                    Box(Modifier.fillMaxSize()) {
                        Text(text = "Search")
                    }
                }

                else -> throw IllegalStateException("Unknown route: $key")
            }
        },
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(600, easing = EaseInOutCubic),
                initialOffsetX = { it }
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(600, easing = EaseInOutCubic),
                targetOffsetX = { -it }
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                animationSpec = tween(600, easing = EaseInOutCubic),
                initialOffsetX = { -it }
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(600, easing = EaseInOutCubic),
                targetOffsetX = { it }
            )
        },
    )
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