package com.ghost.zeku.data.remote

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun provideHttpClientEngine(): HttpClientEngine {
    return CIO.create {
        // Useful later if you implement batch downloading (e.g., 20 manga pages at once)
        maxConnectionsCount = 1000
        endpoint {
            maxConnectionsPerRoute = 100
        }
    }
}