package com.ghost.zeku.presentation.screen.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import com.ghost.zeku.presentation.components.hero.HeroCarouselConfig
import com.ghost.zeku.presentation.components.media.list.ListConfig
import com.ghost.zeku.presentation.components.media.list.MediaListCardVariant
import com.ghost.zeku.presentation.components.media.list.MediaListDefaults
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.PosterLayout
import com.ghost.zeku.presentation.components.section.MediaSectionConfig
import com.ghost.zeku.presentation.components.section.SectionLayout

@Immutable
data class HomeUiConfig(
    val horizontalSectionConfig: MediaSectionConfig = MediaSectionConfig(
        layout = SectionLayout.HorizontalRow(
            itemSpacing = Dimens.paddingMedium,
            contentPadding = PaddingValues(horizontal = Dimens.paddingMedium)
        )
    ),
    val posterConfig: PosterConfig = PosterConfig(
        layout = PosterLayout.Modern

    ),
    val listCardVariant: MediaListCardVariant = MediaListCardVariant.COMFORTABLE,
    val listCardConfig: ListConfig = MediaListDefaults.config(
        MediaListCardVariant.COMFORTABLE,
    ),
    val heroCarouselConfig: HeroCarouselConfig? = null,
)

