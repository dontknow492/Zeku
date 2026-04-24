package com.ghost.zeku

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ghost.zeku.data.repository.auth.AndroidAuthRedirectListener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 2. Handle the intent if the app was already in the background
        handleIntent(intent)
    }

    /**
     * Centralized logic to capture the browser redirect.
     * It checks if the URI matches our custom scheme and passes it to the
     * common code's listener.
     */
    private fun handleIntent(intent: Intent?) {
        val uri = intent?.dataString
        if (uri != null && uri.startsWith("com.ghost.zeku://auth")) {
            AndroidAuthRedirectListener.onRedirectReceived(uri)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}