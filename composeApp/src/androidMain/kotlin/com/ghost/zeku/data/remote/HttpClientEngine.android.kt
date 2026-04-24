package com.ghost.zeku.data.remote

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

actual fun provideHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            // Crucial for mobile devices switching between WiFi and Cellular
            retryOnConnectionFailure(true)
        }
    }
}