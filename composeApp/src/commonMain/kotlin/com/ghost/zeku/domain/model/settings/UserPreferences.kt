package com.ghost.zeku.domain.model.settings

import androidx.compose.ui.graphics.Color
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.enum.ThemeMode
import com.ghost.zeku.domain.model.enum.TitleLanguage
import com.ghost.zeku.presentation.theme.Primary
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit


@Serializable
data class UserPreferences(
    // --- Core ---
    val activeProvider: ProviderType = ProviderType.ANILIST,

    // --- Content & Display ---
    val titleLanguage: TitleLanguage = TitleLanguage.ROMAJI,
    val showNsfw: Boolean = false,
    val blurSpoilers: Boolean = true,

    // --- Network & Cache ---
    val mediaDetailTimeout: Long = TimeUnit.HOURS.toMillis(1),
    val homeTimeout: Long = TimeUnit.HOURS.toMillis(4),
    val chapterTimeout: Long = TimeUnit.HOURS.toMillis(12),
    val episodeTimeout: Long = TimeUnit.HOURS.toMillis(12),
    val perPage: Int = 20,
    val dataSaverMode: Boolean = false,
    val searchDebounceMillis: Long = 300L,

    // --- Tracking ---
    val autoUpdateProgress: Boolean = true,

    // --- Theme ---
    val dynamicTheme: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    @Serializable(with = ColorSerializer::class)
    val accentColor: Color = Primary
)


