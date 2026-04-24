package com.ghost.zeku.data.repository.auth

// The factory to get the listener (we use expect/actual for the factory)
expect class AuthRedirectListenerFactory {
    fun createListener(): AuthRedirectListener
}



