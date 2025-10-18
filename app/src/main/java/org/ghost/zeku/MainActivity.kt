package org.ghost.zeku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.ghost.zeku.core.enum.ThemeMode
import org.ghost.zeku.ui.navigation.AppNavigationGraph
import org.ghost.zeku.ui.navigation.NavRoute
import org.ghost.zeku.ui.theme.ZekuTheme
import org.ghost.zeku.viewModels.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var navHostController: NavHostController

    private val settingsViewModel: SettingsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsState by settingsViewModel.settingsStateFlow.collectAsStateWithLifecycle()
            navHostController = rememberNavController()
            ZekuTheme(
                appTheme = settingsState.appearance.theme,
                darkTheme = when (settingsState.appearance.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                },
                isAmoled = settingsState.appearance.amoled,
                contrastLevel = settingsState.appearance.highContrast.toDouble(),
                dynamicColor = settingsState.appearance.dynamicColor,
            ) {
                val startDestination = NavRoute.Settings
                AppNavigationGraph(
                    navHostController = navHostController,
                    settingsViewModel = settingsViewModel,
                    startDestination = startDestination
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ZekuTheme {
        Greeting("Android")
    }
}