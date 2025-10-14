package org.ghost.zeku

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.decode.BitmapFactoryDecoder
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath
import timber.log.Timber

@HiltAndroidApp
class MyApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        // This is the key part!
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(BitmapFactoryDecoder.Factory())
            }
            .memoryCache {
                // 🧠 Set the max size of the memory cache to 25% of the app's available memory.
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                // 💾 Set a disk cache for persisting images.
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizePercent(0.02) // Use 2% of available disk space
                    .build()
            }
            .apply {
                // Only add the logger if the app is a debug build
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}