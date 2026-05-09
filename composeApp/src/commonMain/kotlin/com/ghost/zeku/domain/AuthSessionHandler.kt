package com.ghost.zeku.domain

// commonMain
interface AuthSessionHandler {
    /**
     * Opens the browser and waits for the redirect code.
     * Returns the 'code' from the redirect URI.
     */
    suspend fun getAuthorizationCode(authUrl: String, path: String): String?
}


// You can use expect/actual to provide the implementation
expect fun getAuthHandler(): AuthSessionHandler