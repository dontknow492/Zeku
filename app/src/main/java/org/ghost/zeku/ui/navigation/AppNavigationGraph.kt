package org.ghost.zeku.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.ghost.zeku.ui.screen.settings.SettingsScreen
import org.ghost.zeku.ui.screen.settings.SettingsUiState
import org.ghost.zeku.ui.screen.settings.SettingsViewModel
import org.ghost.zeku.ui.screen.settings.page.AdvanceSettings
import org.ghost.zeku.ui.screen.settings.page.AppearanceSettings
import org.ghost.zeku.ui.screen.settings.page.FileSettings
import org.ghost.zeku.ui.screen.settings.page.GeneralSettings
import org.ghost.zeku.ui.screen.settings.page.MediaSettings
import org.ghost.zeku.ui.screen.settings.page.NetworkSettings
import org.ghost.zeku.ui.screen.settings.page.PostProcessingSettings
import timber.log.Timber

@Composable
fun AppNavigationGraph(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    startDestination: NavRoute,
    settingsViewModel: SettingsViewModel
) {
    val settingsUiState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()

    val popBackStack: () -> Unit = {
        Timber.d("Popping back stack")
        navHostController.popBackStack()
    }

    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination
    ){
        settingGraph(
            navHostController,
            settingsViewModel,
            settingsUiState,
            popBackStack,
        )
    }
}

fun NavGraphBuilder.settingGraph(
    navHostController: NavHostController,
    settingsViewModel: SettingsViewModel,
    settingsUiState: SettingsUiState,
    onBackClick: () -> Unit
){
    composable<NavRoute.Settings> {
        SettingsScreen(
            modifier = Modifier,
            onBackClick = onBackClick,
            onSettingClick = { route ->
                navHostController.navigate(route)
            }
        )
    }
    composable<SettingsRoute.GeneralSettings> {
        GeneralSettings(
            state = settingsUiState.general,
            eventHandler = {},
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.FileAndDirectorySettings> {
        FileSettings(
            state = settingsUiState.file,
            eventHandler = {},
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.InterfaceSettings> {
        AppearanceSettings(
            state = settingsUiState.appearance,
            eventHandler = {},
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.MediaSettings> {
        MediaSettings(
            state = settingsUiState.media,
            eventHandler = {},
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.NetworkSettings> {
        NetworkSettings(
            state = settingsUiState.network,
            eventHandler = {},
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.PostProcessingSettings> {
        PostProcessingSettings(
            state = settingsUiState.postProcessing,
            eventHandler = {},
            onBackClick = onBackClick,
        )
    }

    composable<SettingsRoute.AdvancedSettings> {
        AdvanceSettings(
            state = settingsUiState.advanced,
            event = {},
            onBackClick = onBackClick,
            onCustomCommandClick = {
                // TODO: Custom command screen
            }
        )
    }

    composable<SettingsRoute.About> {

    }
}