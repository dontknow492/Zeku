package com.ghost.zeku.presentation.screen.category

import androidx.compose.foundation.lazy.grid.GridCells
import com.ghost.zeku.presentation.components.media.list.MediaListCardConfig
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.PosterLayout

data class CategoryUiConfig(
    val posterConfig: PosterConfig = PosterConfig(layout = PosterLayout.Modern),
    val listConfig: MediaListCardConfig = MediaListCardConfig(),
) {

    val layout get() = GridCells.Adaptive(this.posterConfig.content.width)
}