package com.swordfish.lemuroid.app.mobile.feature.shortcuts

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import com.swordfish.lemuroid.app.shared.covers.CoverUtils
import com.swordfish.lemuroid.app.shared.deeplink.DeepLink
import com.swordfish.lemuroid.common.bitmap.cropToSquare
import com.swordfish.lemuroid.common.bitmap.toBitmap
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShortcutsGenerator(
    private val appContext: Context,
) {
    suspend fun pinShortcutForGame(game: Game) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val shortcutManager = appContext.getSystemService(ShortcutManager::class.java)!!
        val bitmap = retrieveBitmap(game)

        val shortcutInfo =
            ShortcutInfo.Builder(appContext, "game_${game.id}")
                .setShortLabel(game.displayName)
                .setLongLabel(game.displayName)
                .setIntent(DeepLink.launchIntentForGame(appContext, game))
                .setIcon(Icon.createWithBitmap(bitmap))
                .build()

        shortcutManager.requestPinShortcut(shortcutInfo, null)
    }

    private suspend fun retrieveBitmap(game: Game): Bitmap =
        withContext(Dispatchers.IO) {
            val result =
                runCatching {
                    val uri = android.net.Uri.parse(game.coverFrontUrl!!)
                    appContext.contentResolver.openInputStream(uri).use { input ->
                        requireNotNull(input)
                        requireNotNull(BitmapFactory.decodeStream(input)).cropToSquare()
                    }
                }
            result.getOrElse { retrieveFallbackBitmap(game) }
        }

    private fun retrieveFallbackBitmap(game: Game): Bitmap {
        val desiredIconSize = getDesiredIconSize()
        return CoverUtils.getFallbackDrawable(game).toBitmap(desiredIconSize, desiredIconSize)
    }

    private fun getDesiredIconSize(): Int {
        val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        return am?.launcherLargeIconSize ?: 256
    }

    fun supportShortcuts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }

        val shortcutManager = appContext.getSystemService(ShortcutManager::class.java)!!
        return shortcutManager.isRequestPinShortcutSupported
    }
}
