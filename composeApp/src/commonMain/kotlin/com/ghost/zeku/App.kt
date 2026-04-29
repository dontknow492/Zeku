package com.ghost.zeku

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ghost.zeku.presentation.navigation.AppNavigation
import com.ghost.zeku.presentation.theme.AppTheme


@Composable
fun App() = AppTheme {
//    HeroCarouselPreview()
//    PreviewMediaDetailContent()
//    PreviewHomeContent()

    AppNavigation(modifier = Modifier.fillMaxSize())

//    val userSettings: UserSettings = koinInject()
//    userSettings.updatePreferences {
//        it.copy(activeProvider = ProviderType.ANILIST)
//    }
//
//    val viewModel: HomeViewModel = koinViewModel()
//    viewModel.onEvent(HomeContract.Event.LoadHomeData(MediaType.ANIME))
//
//    MediaHomeScreen(koinViewModel(), {})

}

