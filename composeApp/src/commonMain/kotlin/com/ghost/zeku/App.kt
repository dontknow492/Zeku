package com.ghost.zeku

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghost.zeku.domain.repository.MediaRepository
import com.ghost.zeku.domain.repository.UserSettings
import com.ghost.zeku.presentation.components.hero.HeroCarouselPreview
import com.ghost.zeku.presentation.screen.home.PreviewHomeContent
import com.ghost.zeku.presentation.theme.AppTheme
import org.koin.compose.koinInject


@Composable
fun App() = AppTheme {

//    val redirectListener: AuthRedirectListener = koinInject()
//    TestAuthScreen(
//        viewModel = AuthViewModel(koinInject(), koinInject())
//    )

    val mediaRepository: MediaRepository = koinInject()
    val userSettings: UserSettings = koinInject()

    val preferences by userSettings.preferences.collectAsStateWithLifecycle()

    HeroCarouselPreview()
//    PreviewMediaDetailContent()
    PreviewHomeContent()


//    Text(preferences.toString())

//    LaunchedEffect(Unit) {
//        delay(3.seconds)
//        userSettings.updatePreferences {
//            it.copy(activeProvider = ProviderType.ANILIST)
//        }
//    }


//    userSettings.setActiveProvider(ProviderType.MYANIMELIST)

//    var animeList by remember { mutableStateOf<Flow<PagingData<Anime>>?>(null) }


//    val mediaId = when (preferences.activeProvider) {
//        ProviderType.MYANIMELIST -> 40748
//        ProviderType.ANILIST -> 113415
//    }

//    val animeData: PagingData<Anime> by remember { mutableStateOf(PagingData.empty()) }


//    LaunchedEffect(Unit) {
//        val animeDetails = mediaRepository.getAnimeRecommendations(199221).first()
//        Napier.i("Details: $animeDetails")
//    }


//    animeList?.collectAsLazyPagingItems().let { pagingItems ->
//        Text(
//            text = "Total Anime: ${pagingItems?.itemCount}",
//            style = MaterialTheme.typography.displayLarge
//        )
//    }

//    TestReview(mediaRepository)

//    TestApp()


//    TestAnimeDetail(mediaRepository, mediaId)


}

