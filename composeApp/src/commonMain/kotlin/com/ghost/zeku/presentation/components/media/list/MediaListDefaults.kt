package com.ghost.zeku.presentation.components.media.list

import androidx.compose.ui.unit.dp

object MediaListDefaults {

    fun config(variant: MediaListCardVariant) = when (variant) {

        MediaListCardVariant.COMPACT -> ListConfig(
            ui = MediaListUiConfig(
                maxWidth = 600.dp,
                imageWidth = 70.dp,
                spacing = 12.dp,
            ),
            content = MediaListContentConfig(
                showGenres = false,
                showProgress = false
            )


        )

        MediaListCardVariant.COMFORTABLE -> ListConfig(
            content = MediaListContentConfig(
                showDescription = false
            )
        )

        MediaListCardVariant.DETAILED -> ListConfig(
            ui = MediaListUiConfig(
                maxWidth = 900.dp,
                imageWidth = 110.dp,
                spacing = 20.dp
            )
        )
    }
}