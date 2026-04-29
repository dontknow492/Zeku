package com.ghost.zeku

import androidx.compose.runtime.Composable
import com.ghost.zeku.presentation.theme.AppTheme


@Composable
fun App() = AppTheme {
//    HeroCarouselPreview()
//    PreviewMediaDetailContent()
//    PreviewHomeContent()

//    AppNavigation(modifier = Modifier.fillMaxSize())

//    val userSettings: UserSettings = koinInject()
//    userSettings.updatePreferences {
//        it.copy(activeProvider = ProviderType.ANILIST)
//    }
//
//    val viewModel: HomeViewModel = koinViewModel()
//    viewModel.onEvent(HomeContract.Event.LoadHomeData(MediaType.ANIME))
//
//    MediaHomeScreen(koinViewModel(), {})

    ZekuAppWrapper()

}




