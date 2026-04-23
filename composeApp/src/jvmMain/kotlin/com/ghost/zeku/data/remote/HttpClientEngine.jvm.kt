package com.ghost.zeku.data.remote

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun provideHttpClientEngine(): HttpClientEngine {
    return CIO.create {
        // You can add Desktop-specific CIO configurations here later
        // maxConnectionsCount = 1000
    }
}