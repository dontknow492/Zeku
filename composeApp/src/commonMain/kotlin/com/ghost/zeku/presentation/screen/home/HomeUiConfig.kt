package com.ghost.zeku.presentation.screen.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import com.ghost.zeku.presentation.components.hero.HeroCarouselConfig
import com.ghost.zeku.presentation.components.media.list.ListCardConfig
import com.ghost.zeku.presentation.components.media.list.ListCardDefaults
import com.ghost.zeku.presentation.components.media.list.ListCardLayout
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.PosterDefaults
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
    val posterLayout: PosterLayout = PosterLayout.Modern,
    val posterConfig: PosterConfig = PosterDefaults.forLayout(posterLayout),

    val listCardVariant: ListCardLayout = ListCardLayout.Modern,
    val listCardConfig: ListCardConfig = ListCardDefaults.forLayout(listCardVariant),
    val heroCarouselConfig: HeroCarouselConfig? = null,
)

