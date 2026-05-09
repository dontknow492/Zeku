package com.ghost.zeku.utils

// androidMain
import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.aakira.napier.Napier
import org.koin.core.context.GlobalContext

actual object BrowserLauncher {
    actual fun openUrl(url: String) {
        Napier.d("Opening $url")
        // Grab the Context from Koin
        val context = GlobalContext.get().get<Context>()

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            // NEW_TASK is required because we are launching from the Application Context
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Failsafe: check if a browser exists to handle the intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}