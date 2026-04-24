package com.ghost.zeku.data.settings

import com.russhwolf.settings.Settings

/**
 * Common interface to generate platform-specific settings.
 */
expect class SettingsFactory {
    /**
     * Returns standard, unencrypted preferences.
     * Perfect for UI themes, default views, etc.
     */
    fun createStandardSettings(): Settings

    /**
     * Returns highly encrypted preferences.
     * Use ONLY for Access Tokens and Refresh Tokens.
     */
    fun createSecureSettings(): Settings
}