package com.ghost.zeku.domain.model.api

sealed interface AuthState {
    data object LoggedOut : AuthState
    data object Loading : AuthState
    data object LoggedIn : AuthState

    // The superpower of Sealed Classes: Holding data!
    data class Error(val message: String) : AuthState
}



