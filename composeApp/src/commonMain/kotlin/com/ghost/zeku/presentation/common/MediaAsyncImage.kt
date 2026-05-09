package com.ghost.zeku.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ghost.zeku.utils.shimmerEffect
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.Res
import zeku.composeapp.generated.resources.error_loading
import zeku.composeapp.generated.resources.retry

@Composable
fun MediaAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showRetryOnError: Boolean = true,
    enableShimmer: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    onClick: (() -> Unit)? = null
) {

    var retryKey by remember { mutableIntStateOf(0) }

    val context = LocalPlatformContext.current

    val request = remember(url, retryKey) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .crossfade(350)
            .memoryCachePolicy(
                if (retryKey > 0) CachePolicy.DISABLED
                else CachePolicy.ENABLED
            )
            .diskCachePolicy(
                if (retryKey > 0) CachePolicy.DISABLED
                else CachePolicy.ENABLED
            )
            .build()
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(clickableModifier),
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {

        SubcomposeAsyncImage(
            model = request,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        ) {

            val state by painter.state.collectAsState()

            AnimatedContent(
                targetState = state,
                label = "media_image_state"
            ) { imageState ->

                when (imageState) {

                    is AsyncImagePainter.State.Loading -> {
                        MediaImageLoadingState(
                            enableShimmer = enableShimmer
                        )
                    }

                    is AsyncImagePainter.State.Success -> {
                        SubcomposeAsyncImageContent()
                    }

                    is AsyncImagePainter.State.Error -> {

                        MediaImageErrorState(
                            throwableMessage = imageState.result.throwable.message,
                            showRetry = showRetryOnError,
                            onRetry = {
                                retryKey++
                            }
                        )
                    }

                    AsyncImagePainter.State.Empty -> {
                        MediaImageEmptyState()
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaImageLoadingState(
    modifier: Modifier = Modifier,
    enableShimmer: Boolean = true
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (enableShimmer) Modifier.shimmerEffect()
                else Modifier.background(
                    MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
    ) {

        val infiniteTransition = rememberInfiniteTransition()

        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 900,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator(
                modifier = Modifier
                    .size(34.dp)
                    .alpha(alpha),
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Loading image...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // fake progress bar feel
        InfiniteLoadingBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun InfiniteLoadingBar(
    modifier: Modifier = Modifier
) {

    val infiniteTransition = rememberInfiniteTransition()

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        )
    )

    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier.height(3.dp)
    )
}

@Composable
private fun MediaImageErrorState(
    throwableMessage: String?,
    showRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorMessage = stringResource(Res.string.error_loading)

    val message = remember(throwableMessage) {
        throwableMessage
            ?.takeIf { it.isNotBlank() }
            ?: errorMessage ?: errorMessage
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = if (showRetry) {
                        Icons.Rounded.WifiOff
                    } else {
                        Icons.Rounded.BrokenImage
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (showRetry) {
                    "Failed to load image"
                } else {
                    "Image unavailable"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (showRetry) {

                Spacer(modifier = Modifier.height(18.dp))

                TextButton(
                    onClick = onRetry
                ) {

                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(Res.string.retry)
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaImageEmptyState(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Rounded.HideImage,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No image available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview
@Composable
private fun MediaAsyncImageStatesPreview() {
    MaterialTheme { // Wrap in your app's theme if you have one
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("1. Loading State (Shimmer)", color = MaterialTheme.colorScheme.onBackground)
                MediaImageLoadingState(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )

                Text("2. Error State (Retry Enabled)", color = MaterialTheme.colorScheme.onBackground)
                MediaImageErrorState(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    showRetry = true,
                    onRetry = {},
                    throwableMessage = null
                )

                Text("3. Error State (Retry Disabled)", color = MaterialTheme.colorScheme.onBackground)
                MediaImageErrorState(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    showRetry = false,
                    onRetry = {},
                    throwableMessage = null
                )

                Text("4. Success State (Live Network)", color = MaterialTheme.colorScheme.onBackground)
                // We use the actual component here with a placeholder image API to prove it works
                MediaAsyncImage(
                    url = "https://picsum.photos/800/400ashlllfhgsdff",
                    contentDescription = "Preview Image",
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}