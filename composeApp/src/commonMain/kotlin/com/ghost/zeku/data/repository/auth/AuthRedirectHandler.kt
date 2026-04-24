package com.ghost.zeku.data.repository.auth

expect class AuthRedirectHandler {
    suspend fun waitForRedirect(): String?
}