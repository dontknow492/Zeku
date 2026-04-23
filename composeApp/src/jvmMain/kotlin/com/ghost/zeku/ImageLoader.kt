package com.ghost.zeku

import androidx.compose.ui.InternalComposeUiApi
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import io.github.aakira.napier.Napier
import java.io.File

@OptIn(InternalComposeUiApi::class)
fun createDesktopImageLoader(
    context: PlatformContext
): ImageLoader {
    Napier.i("Creating desktop image loader")
    val jarPath = File(
        object {}.javaClass.protectionDomain.codeSource.location.toURI()
    )

    val installDir = jarPath.parentFile

    val cacheDir = File(installDir, "cache").apply {
        mkdirs()
    }
    Napier.i("Cache dir: $cacheDir")
    return ImageLoader.Builder(
        context = context
    )
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizePercent(0.02)
                .build()
        }
        .crossfade(true)
        .apply {
            logger(DebugLogger()) // always ok on desktop
        }
        .build()
}