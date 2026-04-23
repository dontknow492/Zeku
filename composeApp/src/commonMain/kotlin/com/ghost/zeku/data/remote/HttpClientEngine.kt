package com.ghost.zeku.data.remote


import io.ktor.client.engine.*

// We expect each platform to provide its own specific engine
expect fun provideHttpClientEngine(): HttpClientEngine