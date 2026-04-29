package com.ghost.zeku.presentation.components.media.poster

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun PosterCardShimmer(config: PosterConfig) {
    Box(
        modifier = Modifier
            .width(config.content.width)
            .aspectRatio(config.image.aspectRatio)
            .clip(RoundedCornerShape(config.image.cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    )
}