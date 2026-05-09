package com.ghost.zeku.utils

// desktopMain
import io.github.aakira.napier.Napier
import java.awt.Desktop
import java.net.URI

actual object BrowserLauncher {
    actual fun openUrl(url: String) {
        Napier.d("Opening $url")
        val os = System.getProperty("os.name").lowercase()

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
            } else {
                // Fallback for Linux environments where AWT Desktop might fail
                val runtime = Runtime.getRuntime()
                when {
                    os.contains("win") -> runtime.exec("rundll32 url.dll,FileProtocolHandler $url")
                    os.contains("mac") -> runtime.exec("open $url")
                    os.contains("nix") || os.contains("nux") -> runtime.exec("xdg-open $url")
                }
            }
        } catch (e: Exception) {
            Napier.e("Failed to open url $url", e)
            e.printStackTrace()
            // In a real app, you might want to log this or show an error UI
        }
    }
}