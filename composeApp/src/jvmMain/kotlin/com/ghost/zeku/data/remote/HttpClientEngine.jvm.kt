package com.ghost.zeku.data.remote

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.Dispatchers
import kotlin.concurrent.thread

actual fun provideHttpClientEngine(): HttpClientEngine {
    return CIO.create {
        // Thread pool
        dispatcher = Dispatchers.IO

        // Total global connections
        maxConnectionsCount = 500

        endpoint {

            // Per-host connections
            maxConnectionsPerRoute = 100

            // Keep-alive for connection reuse
            keepAliveTime = 5_000

            // Connection timeout
            connectTimeout = 15_000

            // Socket timeout
            socketTimeout = 15_000

            // Retry failed TCP connects
            connectAttempts = 3

            // Pipeline optimizations
            pipelineMaxSize = 20
        }

        https {
            // Faster TLS session reuse
            trustManager = null
        }

        requestTimeout = 15_000
    }
}