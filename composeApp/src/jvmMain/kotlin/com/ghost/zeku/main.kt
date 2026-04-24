package com.ghost.zeku

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import coil3.annotation.DelicateCoilApi
import coil3.compose.LocalPlatformContext
import com.ghost.zeku.data.repository.auth.DesktopRedirectHandler
import com.ghost.zeku.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

@OptIn(DelicateCoilApi::class)
fun main(args: Array<String>) {
    // Initialize Napier for Desktop
    Napier.base(DebugAntilog())
    initKoin()


    application {
        val context = LocalPlatformContext.current

        LaunchedEffect(Unit) {
            coil3.SingletonImageLoader.setUnsafe {
                createDesktopImageLoader(context)
            }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Zeku",
        ) {
            App()
        }
    }
}


// Global state to hold the redirect URI
object AppState {
    var pendingRedirectUri: String? = null
}