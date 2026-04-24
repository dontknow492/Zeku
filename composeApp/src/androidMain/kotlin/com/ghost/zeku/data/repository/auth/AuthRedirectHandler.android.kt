package com.ghost.zeku.data.repository.auth

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

actual class AuthRedirectHandler {
    private val redirectDeferred = CompletableDeferred<String>()

    actual suspend fun waitForRedirect(): String? {
        return withTimeoutOrNull(300_000.milliseconds) {
            redirectDeferred.await()
        }
    }

    fun onRedirectReceived(uri: String) {
        redirectDeferred.complete(uri)
    }
}
