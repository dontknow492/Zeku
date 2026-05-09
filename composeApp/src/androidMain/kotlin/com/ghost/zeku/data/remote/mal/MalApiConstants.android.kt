package com.ghost.zeku.data.remote.mal

import zeku.composeApp.BuildConfig

// androidMain
actual object MalConfig {
    actual val clientId: String = BuildConfig.MAL_ID
    actual val redirectUri: String = BuildConfig.MAL_REDIRECT_ANDROID
}