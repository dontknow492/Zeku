package com.ghost.zeku

import android.content.Intent
import android.net.Uri
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
        val data: Uri? = intent?.data

        // Check if the scheme and host match what's in the manifest
        if (data?.scheme == "zeku" && data.host == "auth") {
            // Since you want to handle /mal and /anilist differently:
            val path = data.path // This will be "/mal" or "/anilist"

            // Pass the full URI string or just the code to your listener
            AndroidAuthRedirectListener.onRedirectReceived(data.toString())
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()

}