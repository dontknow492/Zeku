package com.ghost.zeku.domain.model.api

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val accessToken: String) : AuthState()
    data class Error(val message: String) : AuthState()
}