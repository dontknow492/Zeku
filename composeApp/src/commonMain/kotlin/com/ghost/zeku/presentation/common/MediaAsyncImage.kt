package com.ghost.zeku.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ghost.zeku.utils.shimmerEffect
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.Res
import zeku.composeapp.generated.resources.error_loading
import zeku.composeapp.generated.resources.retry

/**
 * A highly reusable, Compose Multiplatform image loader using Coil 3.
 * Handles loading and error states automatically using Material 3 theme colors.
 */
@Composable
fun MediaAsyncImageV1(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.BrokenImage,
                        contentDescription = stringResource(Res.string.error_loading),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.error_loading),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )

}


@Composable
fun MediaAsyncImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showRetryOnError: Boolean = true
) {
    var retryHash by remember { mutableIntStateOf(0) }
    val context = LocalPlatformContext.current

    val request = remember(url, retryHash) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .crossfade(durationMillis = 400)
            .apply {
                if (retryHash > 0) {
                    memoryCachePolicy(CachePolicy.WRITE_ONLY)
                    diskCachePolicy(CachePolicy.WRITE_ONLY)
                }
            }
            .build()
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    ) {
        val painterState by painter.state.collectAsState()
        when (val state = painterState) {
            is AsyncImagePainter.State.Loading -> {
                MediaImageLoadingState() // Used extracted UI
            }

            is AsyncImagePainter.State.Error -> {
                MediaImageErrorState( // Used extracted UI
                    showRetryOnError = showRetryOnError,
                    errorMessage = state.result.throwable.message ?: state.result.throwable.toString(),
                    onRetryClick = { retryHash++ }
                )
                Text("Retry: $retryHash")
            }
//            else -> {
//                SubcomposeAsyncImageContent()
//            }
            AsyncImagePainter.State.Empty -> {
                MediaImageEmptyState()
            }

            is AsyncImagePainter.State.Success -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}


@Composable
internal fun MediaImageLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .shimmerEffect()
    )
}

@Composable
internal fun MediaImageErrorState(
    modifier: Modifier = Modifier,
    showRetryOnError: Boolean,
    errorMessage: String = stringResource(Res.string.error_loading),
    onRetryClick: () -> Unit
) {

    val defaultErrorMessage = stringResource(Res.string.error_loading)
    val error = remember(errorMessage) {
        errorMessage.ifEmpty { defaultErrorMessage }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(enabled = showRetryOnError) { onRetryClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = if (showRetryOnError) Icons.Filled.Refresh else Icons.Filled.BrokenImage,
                contentDescription = stringResource(Res.string.error_loading),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (showRetryOnError) stringResource(Res.string.retry) else error,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
internal fun MediaImageEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Row {
            Icon(
                imageVector = Icons.Filled.HideImage,
                contentDescription = stringResource(Res.string.error_loading),
            )
            Text("No Image")
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
                    showRetryOnError = true,
                    onRetryClick = {}
                )

                Text("3. Error State (Retry Disabled)", color = MaterialTheme.colorScheme.onBackground)
                MediaImageErrorState(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    showRetryOnError = false,
                    onRetryClick = {}
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