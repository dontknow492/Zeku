package com.ghost.zeku.data.repository.auth

actual class AuthRedirectListenerFactory {
    actual fun createListener(): AuthRedirectListener = JvmAuthRedirectListener()
}

private class JvmAuthRedirectListener : AuthRedirectListener {
    override suspend fun waitForRedirect(): String? {
        // Here we call the DesktopRedirectHandler you have in your Canvas
        return DesktopRedirectHandler().listenForCode()
    }
}