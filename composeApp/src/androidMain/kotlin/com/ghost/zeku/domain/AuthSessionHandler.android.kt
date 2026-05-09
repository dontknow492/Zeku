package com.ghost.zeku.domain

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.channels.Channel
import androidx.core.net.toUri
import com.ghost.zeku.domain.AuthSessionHandler
import org.koin.core.context.GlobalContext

// This is the "actual" function commonMain calls
actual fun getAuthHandler(): AuthSessionHandler {
    return GlobalContext.get().get<AuthSessionHandler>()
}

class AndroidAuthHandler(private val context: Context) : AuthSessionHandler {
    override suspend fun getAuthorizationCode(authUrl: String, path: String): String? {
        // 1. Open the system browser
        val intent = Intent(Intent.ACTION_VIEW, authUrl.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

        // 2. Wait for the Messenger to receive the code
        return AuthRedirectMessenger.awaitCode()
    }
}


object AuthRedirectMessenger {
    // A Channel is perfect here because it waits until someone calls 'receive'
    private val codeChannel = Channel<String>(Channel.CONFLATED)

    // Your ViewModel calls this and "suspends" until the code arrives
    suspend fun awaitCode(): String {
        return codeChannel.receive()
    }

    // Your Activity calls this when the intent arrives
    fun onCodeReceived(code: String) {
        codeChannel.trySend(code)
    }
}