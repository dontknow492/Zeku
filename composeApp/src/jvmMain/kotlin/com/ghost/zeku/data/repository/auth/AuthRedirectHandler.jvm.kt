package com.ghost.zeku.data.repository.auth

import com.ghost.zeku.AppState
import com.ghost.zeku.DesktopProtocolHandler
import io.github.aakira.napier.Napier

actual class AuthRedirectHandler {
    private val protocolHandler = DesktopProtocolHandler

    init {
        // Register the protocol when app starts
        protocolHandler.registerProtocolHandler()
    }

    actual suspend fun waitForRedirect(): String? {
        // Check if we were launched with a URI
        AppState.pendingRedirectUri?.let { uri ->
            AppState.pendingRedirectUri = null
            Napier.d { "Using pending redirect URI: ${uri.take(100)}..." }
            return uri
        }

        // Otherwise, wait for it
        return protocolHandler.waitForRedirect()
    }
}