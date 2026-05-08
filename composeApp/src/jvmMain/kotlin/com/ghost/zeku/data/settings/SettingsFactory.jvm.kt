package com.ghost.zeku.data.settings

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.security.MessageDigest
import java.util.*
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop (JVM) implementation.
 */
actual class SettingsFactory {

    actual fun createStandardSettings(): Settings {
        val delegate = Preferences.userRoot().node("com.ghost.zeku.standard")
        return PreferencesSettings(delegate)
    }

    actual fun createSecureSettings(): Settings {
        val delegate = Preferences.userRoot().node("com.ghost.zeku.secure")
        val baseSettings = PreferencesSettings(delegate)
        return DesktopEncryptedSettings(baseSettings)
    }
}

/**
 * A custom AES-256 wrapper for Desktop settings.
 * Since Desktop lacks a unified Keystore, we derive a machine-specific key.
 */
private class DesktopEncryptedSettings(
    private val delegate: Settings
) : Settings by delegate { // Delegates most methods (like clear, keys, etc.) to the base

    private val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    private val secretKey: SecretKeySpec

    init {
        // Generate a stable key based on the machine's hardware environment.
        // This ensures the tokens can only be decrypted on THIS exact computer.
        val machineIdentifier = (System.getenv("COMPUTERNAME") ?: "Unknown") +
                (System.getProperty("user.name") ?: "User") +
                System.getProperty("os.name")

        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(machineIdentifier.toByteArray(Charsets.UTF_8))
        secretKey = SecretKeySpec(keyBytes, "AES")
    }

    private fun encrypt(value: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    private fun decrypt(value: String): String {
        return try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decodedBytes = Base64.getDecoder().decode(value)
            String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            "" // Return empty if decryption fails (e.g., if OS user changed)
        }
    }

    // Override the string methods to intercept and encrypt/decrypt
    override fun putString(key: String, value: String) {
        delegate.putString(key, encrypt(value))
    }

    override fun getString(key: String, defaultValue: String): String {
        val encrypted = delegate.getString(key, "")
        if (encrypted.isEmpty()) return defaultValue
        val decrypted = decrypt(encrypted)
        return decrypted.ifEmpty { defaultValue }
    }

    override fun getStringOrNull(key: String): String? {
        val encrypted = delegate.getStringOrNull(key) ?: return null
        val decrypted = decrypt(encrypted)
        return decrypted.ifEmpty { null }
    }
}