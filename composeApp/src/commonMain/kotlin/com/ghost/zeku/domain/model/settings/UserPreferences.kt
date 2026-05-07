package com.ghost.zeku.domain.model.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.enum.ThemeMode
import com.ghost.zeku.domain.model.enum.TitleLanguage
import com.ghost.zeku.presentation.components.media.GridStyle
import com.ghost.zeku.presentation.components.media.MediaDisplayMode
import com.ghost.zeku.presentation.components.media.list.ListConfig
import com.ghost.zeku.presentation.components.media.list.MediaListCardVariant
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.theme.Primary
import com.ghost.zeku.utils.serializer.ColorSerializer
import com.ghost.zeku.utils.serializer.DpSerializer
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
    val accentColor: Color = Primary,


    val displayPreferences: DisplayPreferences = DisplayPreferences(),


    )

@Serializable
data class DisplayPreferences(
    val category: MediaDisplayPreference = MediaDisplayPreference(),
    val search: MediaDisplayPreference = MediaDisplayPreference()
)

@Serializable
data class MediaDisplayPreference(
    val mode: MediaDisplayMode = MediaDisplayMode.PosterGrid,
    val posterConfig: PosterConfig = PosterConfig(),
    val listConfig: ListConfig = ListConfig(),
    val gridStyle: GridStyle = GridStyle.Adaptive,
    @Serializable(with = DpSerializer::class)
    val gridSpacing: Dp = 8.dp,
    @Serializable(with = DpSerializer::class)
    val gridMinSize: Dp = 140.dp,
    val gridCount: Int = 4,
    // 3. The saved List settings
    val listVariant: MediaListCardVariant = MediaListCardVariant.COMFORTABLE,
    @Serializable(with = DpSerializer::class)
    val listSpacing: Dp = 12.dp
)




