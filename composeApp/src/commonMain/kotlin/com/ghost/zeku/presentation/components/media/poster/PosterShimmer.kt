package com.ghost.zeku.presentation.components.media.poster

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun PosterCardShimmer(
    config: PosterConfig,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(config.cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = alpha))
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // =========================
            // Image Placeholder
            // =========================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(config.aspectRatio)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
            )

            // =========================
            // Text Placeholder
            // =========================
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(10.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(10.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(10.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            )
                    )
                }
            }
        }
    }
}