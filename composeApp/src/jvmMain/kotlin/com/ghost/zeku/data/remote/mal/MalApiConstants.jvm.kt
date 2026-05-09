package com.ghost.zeku.data.remote.mal

import zeku.composeApp.BuildConfig

// desktopMain
actual object MalConfig {
    actual val clientId: String = BuildConfig.MAL_ID
    actual val redirectUri: String = BuildConfig.MAL_REDIRECT_DESKTOP
}