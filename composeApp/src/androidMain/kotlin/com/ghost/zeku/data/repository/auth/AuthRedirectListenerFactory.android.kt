package com.ghost.zeku.data.repository.auth

import kotlinx.coroutines.CompletableDeferred

actual class AuthRedirectListenerFactory {
    actual fun createListener(): AuthRedirectListener = AndroidAuthRedirectListener
}


/**
 * Singleton object on Android so the MainActivity can find it easily.
 */
object AndroidAuthRedirectListener : AuthRedirectListener {
    private var redirectDeferred = CompletableDeferred<String?>()

    override suspend fun waitForRedirect(): String? {
        redirectDeferred = CompletableDeferred() // Reset for new login attempt
        return redirectDeferred.await()
    }

    // This is called by your MainActivity.onNewIntent
    fun onRedirectReceived(uri: String?) {
        if (redirectDeferred.isActive) {
            redirectDeferred.complete(uri)
        }
    }
}