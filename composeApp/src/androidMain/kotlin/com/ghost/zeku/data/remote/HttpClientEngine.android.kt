package com.ghost.zeku.data.remote

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

actual fun provideHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        // You can add Android-specific OkHttp configurations here later
        // config { retryOnConnectionFailure(true) }
    }
}