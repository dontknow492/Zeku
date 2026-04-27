package com.ghost.zeku.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.zeku.presentation.components.media.poster.BadgeConfig
import com.ghost.zeku.presentation.components.media.poster.BadgePosition
import com.ghost.zeku.presentation.components.media.poster.MediaImageConfig
import com.ghost.zeku.presentation.components.media.poster.NsfwConfig

@Composable
fun MediaImage(
    imageUrl: String,
    title: String,
    isNsfw: Boolean,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    mediaImageConfig: MediaImageConfig,
    nsfwConfig: NsfwConfig,
    badge: (@Composable BoxScope.() -> Unit)?
) {


    val shouldBlur = isNsfw &&
            nsfwConfig.enabled &&
            !isRevealed

    val blurRadius by animateFloatAsState(
        targetValue = if (shouldBlur) nsfwConfig.blurRadius else 0f
    )

    Box(
        modifier = Modifier
            .aspectRatio(mediaImageConfig.aspectRatio)
    ) {

        // 🎞️ Image
        MediaAsyncImage(
            url = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .fillMaxSize()

                .then(
                    if (shouldBlur)
                        Modifier.blur(blurRadius.dp)
                    else Modifier
                ),
            contentScale = mediaImageConfig.contentScale,
        )

        // 🌑 Dark overlay
        if (shouldBlur) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = nsfwConfig.dimAlpha))
            )

            // 🔞 Label + CTA
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (nsfwConfig.showLabel) {
                    Text(
                        text = "NSFW",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (nsfwConfig.clickToReveal) {
                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Tap to reveal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // 👇 Click to reveal (only when blurred)
        if (shouldBlur && nsfwConfig.clickToReveal) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onReveal() }
            )
        }

        // Normal badges (only when visible OR always if you want)
        if (!shouldBlur) {
            badge?.invoke(this)
        }
    }
}


@Composable
fun BoxScope.MediaBadge(
    score: Float?,
    badgeText: String?,
    badgeConfig: BadgeConfig,
) {


    fun Modifier.alignBadge(position: BadgePosition) = when (position) {
        BadgePosition.TOP_START -> align(Alignment.TopStart)
        BadgePosition.TOP_END -> align(Alignment.TopEnd)
        BadgePosition.BOTTOM_START -> align(Alignment.BottomStart)
        BadgePosition.BOTTOM_END -> align(Alignment.BottomEnd)
    }

    if (badgeConfig.showScore && score != null) {
        Surface(
            modifier = Modifier
                .alignBadge(badgeConfig.scorePosition)
                .padding(6.dp),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, null, Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text(
                    "%.1f".format(score),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                )
            }
        }
    }

    if (badgeConfig.showBadge && badgeText != null) {
        Surface(
            modifier = Modifier
                .alignBadge(badgeConfig.badgePosition),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = badgeText.uppercase(),
                fontSize = 10.sp,
                modifier = Modifier.padding(4.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}