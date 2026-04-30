package com.ghost.zeku.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.ghost.zeku.domain.model.enum.MediaType
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


@Serializable
data object AnimeHomeRoute : NavKey

@Serializable
data object MangaHomeRoute : NavKey

@Serializable
data object SearchRoute : NavKey

@Serializable
data object LibraryRoute : NavKey

@Serializable
data class MediaDetailsRoute(val id: Int, val type: MediaType) : NavKey

@Serializable
data class AllCategoriesRoute(val type: MediaType, val categoryId: String, val title: String) : NavKey

enum class TopLevelDestination(
    val routeInstance: NavKey,
    val routeClass: KClass<out NavKey>,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val title: String
) {
    ANIME(
        routeInstance = AnimeHomeRoute,
        routeClass = AnimeHomeRoute::class,
        selectedIcon = Icons.Filled.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle,
        title = "Anime"
    ),
    MANGA(
        routeInstance = MangaHomeRoute,
        routeClass = MangaHomeRoute::class,
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        title = "Manga"
    ),
    SEARCH(
        routeInstance = SearchRoute,
        routeClass = SearchRoute::class,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        title = "Search"
    ),
    LIBRARY(
        routeInstance = LibraryRoute,
        routeClass = LibraryRoute::class,
        selectedIcon = Icons.Filled.Bookmarks,
        unselectedIcon = Icons.Outlined.Bookmarks,
        title = "Library"
    )
}