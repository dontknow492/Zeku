package com.ghost.zeku.presentation.screen.detail

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.presentation.components.media.character.MediaCharacterCardConfig
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.relation.MediaRelationCardConfig
import com.ghost.zeku.presentation.components.media.review.ReviewCardConfig
import com.ghost.zeku.presentation.components.section.MediaSectionConfig
import com.ghost.zeku.presentation.components.section.SectionLayout

@Immutable
data class MediaDetailUiConfig(
    val characterSection: MediaSectionConfig = MediaSectionConfig(),
    val relationSection: MediaSectionConfig = MediaSectionConfig(),
    val reviewSection: MediaSectionConfig = MediaSectionConfig(
        layout = SectionLayout.VerticalList()
    ),
    val recommendationSection: MediaSectionConfig = MediaSectionConfig(),

    val characterCard: MediaCharacterCardConfig = MediaCharacterCardConfig(),
    val relationCard: MediaRelationCardConfig = MediaRelationCardConfig(),
    val reviewCard: ReviewCardConfig = ReviewCardConfig(),
    val recommendationCard: PosterConfig = PosterConfig(),

    val showTrailer: Boolean = true,
    val showExternalLinks: Boolean = true,

    val desktopSideBarWidth: Dp = 390.dp,
    val desktopItemSpacing: Dp = 24.dp,
    val desktopHeroBannerSize: Dp = 716.dp,
    val desktopHeroMetaOffset: Dp = 140.dp,

    val androidItemSpacing: Dp = 16.dp,
    val androidHeroBannerHeight: Dp = 590.dp,
    val androidHeroMetaOffset: Dp = 140.dp,
)