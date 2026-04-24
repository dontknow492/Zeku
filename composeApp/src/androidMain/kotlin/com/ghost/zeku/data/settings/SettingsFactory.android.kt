package com.ghost.zeku.data.settings

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


actual class SettingsFactory(private val context: Context) {

    actual fun createStandardSettings(): Settings {
        val sharedPrefs = context.getSharedPreferences("zeku_standard_prefs", Context.MODE_PRIVATE)
        return SharedPreferencesSettings(sharedPrefs)
    }

    actual fun createSecureSettings(): Settings {
        // 1. Get a normal SharedPreferences instance to act as our storage mechanism
        val sharedPrefs = context.getSharedPreferences("zeku_secure_prefs", Context.MODE_PRIVATE)
        val baseSettings = SharedPreferencesSettings(sharedPrefs)

        // 2. Wrap it in our custom Android Hardware Keystore encryption
        return AndroidEncryptedSettings(baseSettings)
    }


    /**
     * A custom AES-256-GCM wrapper that uses the hardware-backed AndroidKeyStore.
     * This completely replaces the deprecated EncryptedSharedPreferences library.
     */
    private class AndroidEncryptedSettings(
        private val delegate: Settings
    ) : Settings by delegate {

        private val keyAlias = "zeku_auth_master_key"
        private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        /**
         * Retrieves the SecretKey from the hardware Keystore, or creates it if it doesn't exist.
         */
        private fun getSecretKey(): SecretKey {
            return if (keyStore.containsAlias(keyAlias)) {
                val entry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
                entry.secretKey
            } else {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                val spec = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()

                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        }

        private fun encrypt(value: String): String {
            return try {
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

                // The Initialization Vector (IV) is required to decrypt it later
                val iv = cipher.iv
                val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

                // Combine IV + Encrypted Data into a single ByteArray
                val combined = iv + encryptedBytes
                Base64.encodeToString(combined, Base64.NO_WRAP)
            } catch (e: Exception) {
                ""
            }
        }

        private fun decrypt(value: String): String {
            return try {
                val combined = Base64.decode(value, Base64.NO_WRAP)

                // GCM IV is exactly 12 bytes long
                val iv = combined.copyOfRange(0, 12)
                val encryptedBytes = combined.copyOfRange(12, combined.size)

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv) // 128 is the authentication tag length in bits

                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
                String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
            } catch (e: Exception) {
                "" // Gracefully fail if decryption fails (e.g. if the user wiped their device credentials)
            }
        }

        // ========================================================================
        // INTERCEPT & ENCRYPT STRING OPERATIONS
        // ========================================================================

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
}