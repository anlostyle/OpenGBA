package com.swordfish.lemuroid.app.shared.covers

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.disk.DiskCache
import coil.imageLoader
import coil.load
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.swordfish.lemuroid.common.drawable.TextDrawable
import com.swordfish.lemuroid.common.graphics.ColorUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game

object CoverUtils {
    fun loadCover(
        game: Game,
        imageView: ImageView?,
    ) {
        if (imageView == null) return

        imageView.load(game.coverFrontUrl, imageView.context.imageLoader) {
            val fallbackDrawable = getFallbackDrawable(game)
            fallback(fallbackDrawable)
            error(fallbackDrawable)
        }
    }

    fun buildImageLoader(applicationContext: Context): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .diskCache(
                DiskCache.Builder()
                    .directory(applicationContext.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.20)
                    .build(),
            )
            .memoryCache {
                MemoryCache.Builder(applicationContext)
                    .maxSizePercent(0.20)
                    .build()
            }
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .build()
    }

    fun getFallbackDrawable(game: Game) = TextDrawable(computeTitle(game), computeColor(game))

    private fun computeTitle(game: Game): String {
        val sanitizedName =
            game.displayName
                .replace(Regex("\\(.*\\)"), "")

        return sanitizedName.asSequence()
            .filter { it.isDigit() or it.isUpperCase() or (it == '&') }
            .take(3)
            .joinToString("")
            .ifBlank { game.displayName.firstOrNull()?.toString().orEmpty() }
            .capitalize()
    }

    private fun computeColor(game: Game): Int {
        return ColorUtils.randomColor(game.displayName)
    }
}
