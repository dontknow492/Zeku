package com.ghost.zeku.domain.model.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.presentation.components.media.GridStyle
import com.ghost.zeku.presentation.components.media.MediaDisplayMode
import com.ghost.zeku.presentation.components.media.list.ListCardConfig
import com.ghost.zeku.presentation.components.media.list.ListCardDefaults
import com.ghost.zeku.presentation.components.media.list.ListCardLayout
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.PosterDefaults
import com.ghost.zeku.presentation.components.media.poster.PosterLayout
import com.ghost.zeku.utils.serializer.DpSerializer
import kotlinx.serialization.Serializable

@Serializable
data class MediaDisplayPreference(
    val mode: MediaDisplayMode = MediaDisplayMode.PosterGrid,
    val posterLayout: PosterLayout = PosterLayout.Modern,
    val posterConfig: PosterConfig = PosterDefaults.forLayout(posterLayout),

    val gridStyle: GridStyle = GridStyle.Adaptive,
    @Serializable(with = DpSerializer::class)
    val gridSpacing: Dp = 8.dp,
    @Serializable(with = DpSerializer::class)
    val gridMinSize: Dp = 140.dp,
    val gridCount: Int = 4,
    // 3. The saved List settings
    val listCardLayout: ListCardLayout = ListCardLayout.Modern,
    val listConfig: ListCardConfig = ListCardDefaults.forLayout(listCardLayout),
    @Serializable(with = DpSerializer::class)
    val listSpacing: Dp = 12.dp
)