package com.ghost.zeku.presentation.components.hero

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object HeroDefaults {

    fun config(isDesktop: Boolean): HeroConfig {
        return if (isDesktop) {
            HeroConfig(
                cornerRadius = 0.dp, // edge to edge
                contentPaddingDesktop = PaddingValues(48.dp),
                showDescriptionOnMobile = false
            )
        } else {
            HeroConfig(
                cornerRadius = 16.dp,
                contentPaddingMobile = PaddingValues(16.dp),
                showDescriptionOnMobile = false
            )
        }
    }
}


