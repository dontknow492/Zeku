package com.ghost.zeku.data.repository.auth

interface AuthRedirectListener {
    /**
     * Starts listening for a redirect and returns the full URI.
     * This will suspend until the browser returns or it times out.
     */
    suspend fun waitForRedirect(): String?
}