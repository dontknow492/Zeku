package com.ghost.zeku

import androidx.compose.runtime.Composable
import com.ghost.zeku.domain.model.enum.MediaType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.UserSettings
import com.ghost.zeku.presentation.screen.home.MediaHomeScreen
import com.ghost.zeku.presentation.theme.AppTheme
import com.ghost.zeku.presentation.viewmodel.home.HomeContract
import com.ghost.zeku.presentation.viewmodel.home.HomeViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun App() = AppTheme {
//    HeroCarouselPreview()
//    PreviewMediaDetailContent()
//    PreviewHomeContent()

    val userSettings: UserSettings = koinInject()
    userSettings.updatePreferences {
        it.copy(activeProvider = ProviderType.ANILIST)
    }

    val viewModel: HomeViewModel = koinViewModel()
    viewModel.onEvent(HomeContract.Event.LoadHomeData(MediaType.ANIME))

    MediaHomeScreen(koinViewModel(), {})

}

