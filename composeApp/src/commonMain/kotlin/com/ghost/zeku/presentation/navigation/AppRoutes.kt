package com.ghost.zeku.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


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