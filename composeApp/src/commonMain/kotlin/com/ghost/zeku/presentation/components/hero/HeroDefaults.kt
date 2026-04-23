package com.ghost.zeku.presentation.components.hero

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object HeroDefaults {

    fun config(isDesktop: Boolean): HeroConfig {
        return if (isDesktop) {
            HeroConfig(
                cornerRadius = 24.dp,
                aspectRatioDesktop = 21f / 9f,
                contentPaddingDesktop = PaddingValues(48.dp),
                showDescriptionOnMobile = false
            )
        } else {
            HeroConfig(
                cornerRadius = 16.dp,
                aspectRatioMobile = 4f / 5f,
                contentPaddingMobile = PaddingValues(16.dp),
                showDescriptionOnMobile = false
            )
        }
    }
}


