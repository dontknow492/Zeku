package com.ghost.zeku

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.time.Duration.Companion.milliseconds

object DesktopProtocolHandler {

    // A random high port used ONLY for Instance 2 to talk to Instance 1
    private const val IPC_PORT = 49152

    /**
     * Start listening for the custom protocol redirect.
     * This opens a socket and waits for Instance 2 to send the URL.
     */
    suspend fun waitForRedirect(): String? = withContext(Dispatchers.IO) {
        Napier.d { "DesktopProtocolHandler: Starting IPC server to listen for redirect" }

        return@withContext withTimeoutOrNull(300_000.milliseconds) { // 5 minute timeout
            try {
                // Instance 1 binds to this port
                ServerSocket(IPC_PORT, 1, InetAddress.getByName("127.0.0.1")).use { server ->
                    val client = server.accept() // Suspends here until Instance 2 connects!
                    val reader = BufferedReader(InputStreamReader(client.inputStream))
                    val uri = reader.readLine() // Read the URL sent by Instance 2
                    client.close()
                    uri
                }
            } catch (e: Exception) {
                Napier.e(e) { "IPC Server Error" }
                null
            }
        }
    }

    /**
     * Called by Instance 2 when launched by the OS.
     * Sends the URI to Instance 1 and returns true if successful.
     */
    fun sendUriToMainInstance(uri: String): Boolean {
        return try {
            Socket("127.0.0.1", IPC_PORT).use { socket ->
                val writer = PrintWriter(socket.outputStream, true)
                writer.println(uri)
            }
            true // Successfully sent to Instance 1
        } catch (e: Exception) {
            false // Instance 1 is not running or listening
        }
    }

    /**
     * Register the custom protocol handler for the current OS
     */
    fun registerProtocolHandler() {
        val os = System.getProperty("os.name").lowercase()

        when {
            os.contains("win") -> registerOnWindows()
            os.contains("mac") -> registerOnMacOS()
            os.contains("nix") || os.contains("nux") -> registerOnLinux()
            else -> Napier.w { "Unknown OS, cannot register protocol handler" }
        }
    }

    private fun registerOnWindows() {
        Napier.d { "Registering custom protocol on Windows: com.ghost.zeku://" }
        try {
            val appPath = System.getProperty("java.home") + "\\bin\\javaw.exe"
            val classPath = System.getProperty("java.class.path")
            val mainClass = "com.ghost.zeku.MainKt"

            // Notice we format it exactly to pass --uri "%1" to your main function
            val command = """
                reg add HKEY_CLASSES_ROOT\com.ghost.zeku /ve /d "URL:Zeku Protocol" /f
                reg add HKEY_CLASSES_ROOT\com.ghost.zeku /v "URL Protocol" /d "" /f
                reg add HKEY_CLASSES_ROOT\com.ghost.zeku\shell\open\command /ve /d "\"$appPath\" -cp \"$classPath\" $mainClass --uri \"%1\"" /f
            """.trimIndent()

            val process = ProcessBuilder("cmd", "/c", command).redirectErrorStream(true).start()
            if (process.waitFor() == 0) Napier.i { "Custom protocol registered successfully on Windows" }
        } catch (e: Exception) {
            Napier.e(e) { "Error registering protocol on Windows" }
        }
    }

    private fun registerOnMacOS() {
        Napier.w { "macOS protocol registration should be handled via app bundle Info.plist" }
    }

    private fun registerOnLinux() {
        Napier.d { "Registering custom protocol on Linux: com.ghost.zeku://" }
        try {
            val desktopFile = """
                [Desktop Entry]
                Type=Application
                Name=Zeku
                Exec=${System.getProperty("java.home")}/bin/java -cp ${System.getProperty("java.class.path")} com.ghost.zeku.MainKt --uri %u
                MimeType=x-scheme-handler/com.ghost.zeku;
            """.trimIndent()

            val desktopDir = "${System.getProperty("user.home")}/.local/share/applications"
            File(desktopDir).mkdirs()
            File("$desktopDir/zeku.desktop").writeText(desktopFile)

            ProcessBuilder("xdg-mime", "default", "zeku.desktop", "x-scheme-handler/com.ghost.zeku")
                .start().waitFor()
            Napier.i { "Custom protocol registered successfully on Linux" }
        } catch (e: Exception) {
            Napier.e(e) { "Error registering protocol on Linux" }
        }
    }
}