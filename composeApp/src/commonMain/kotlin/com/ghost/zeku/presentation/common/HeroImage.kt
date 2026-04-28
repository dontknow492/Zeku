package com.ghost.zeku.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ghost.zeku.presentation.theme.AppTheme

@Composable
fun HeroImage(
    modifier: Modifier,
    imageUrl: String,
    isDesktop: Boolean,
    blurRadius: Dp = 24.dp,
    scrim: Boolean = false
) {

    Box(
        modifier = modifier
    ) {
        // 1. Base Background Image
        MediaAsyncImage(
            url = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Gradient Blur Mask
        if (isDesktop) {
            MediaAsyncImage(
                url = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blurRadius)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithCache {
                        val horizontalMask = Brush.horizontalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startX = 0f,
                            endX = size.width * 0.9f
                        )
                        val verticalMask = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = size.height * 0.4f,
                            endY = size.height
                        )
                        onDrawWithContent {
                            drawContent()
                            drawIntoCanvas { canvas ->
                                canvas.saveLayer(
                                    bounds = Rect(0f, 0f, size.width, size.height),
                                    paint = Paint().apply { blendMode = BlendMode.DstIn }
                                )
                                drawRect(horizontalMask)
                                drawRect(verticalMask)
                                canvas.restore()
                            }
                        }
                    },
                contentScale = ContentScale.Crop
            )
        } else {
            MediaAsyncImage(
                url = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blurRadius)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = size.height * 0.4f,
                                endY = size.height
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    },
                contentScale = ContentScale.Crop
            )
        }

        // 3. Color Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f
                    )
                )
        )

        if (isDesktop) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                Color.Transparent,
                            ),
                            startX = 0f,
                        )
                    )
            )
        }

        if (scrim) {
            HeroScrim(
                isDesktop = isDesktop,
                intensity = 0.9f,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}


// ----------------------------------------------------------------------------
// RESPONSIVE SCRIM – Uses theme colors, not hardcoded black/white
// ----------------------------------------------------------------------------
@Composable
private fun HeroScrim(
    isDesktop: Boolean,
    intensity: Float,
    modifier: Modifier = Modifier,
) {
    val scrim = MaterialTheme.colorScheme.background

    // Left‑to‑right gradient (desktop only) – improves text readability on wide images
    if (isDesktop) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(scrim.copy(alpha = intensity - 0.05f), Color.Transparent),
                        startX = 0f,
                        endX = 1200f
                    )
                )
        )
    }

    // Bottom‑to‑top gradient (always present, stronger on mobile)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, scrim.copy(alpha = intensity + 0.05f)),
                    startY = if (isDesktop) 0.4f else 0.2f // earlier fade on mobile
                )
            )
    )
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HeroImagePreview() {
    AppTheme {
        HeroImage(
            modifier = Modifier,
            imageUrl = "https://media.themoviedb.org/t/p/w1066_and_h600_face/gmECX1DvFgdUPjtio2zaL8BPYPu.jpg",
            isDesktop = true
        )
    }
}
