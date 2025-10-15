package org.ghost.zeku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.ghost.zeku.ui.navigation.AppNavigationGraph
import org.ghost.zeku.ui.navigation.NavRoute
import org.ghost.zeku.ui.screen.settings.SettingsViewModel
import org.ghost.zeku.ui.theme.ZekuTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var navHostController: NavHostController

    private val settingsViewModel: SettingsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            navHostController = rememberNavController()
            ZekuTheme {
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