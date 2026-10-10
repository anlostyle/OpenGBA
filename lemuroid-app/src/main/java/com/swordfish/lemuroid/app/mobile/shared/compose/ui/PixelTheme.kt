package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

/**
 * Launcher color palettes. Besides the original GBA green, each palette follows one of the
 * official KONKR Pocket Advance shell colors.
 */
enum class PixelPalette(
    val id: String,
    val ink: Color,
    val panel: Color,
    val panelLight: Color,
    val paper: Color,
    val accent: Color,
    val amber: Color,
    val red: Color,
    val muted: Color,
    val outline: Color,
    val shadow: Color,
    val isLight: Boolean = false,
) {
    GBA(
        id = "gba",
        ink = Color(0xFF091014),
        panel = Color(0xFF0F191E),
        panelLight = Color(0xFF22323B),
        paper = Color(0xFFF1E7C9),
        accent = Color(0xFFA8D944),
        amber = Color(0xFFF2A93B),
        red = Color(0xFFD86A55),
        muted = Color(0xFF91A1A8),
        outline = Color(0xFF344751),
        shadow = Color(0xFF030709),
    ),
    CORAL_ORANGE(
        id = "coral_orange",
        ink = Color(0xFF140C09),
        panel = Color(0xFF1F1410),
        panelLight = Color(0xFF3B2A22),
        paper = Color(0xFFF7EADC),
        accent = Color(0xFFFF8A4C),
        amber = Color(0xFFFFC56B),
        red = Color(0xFFE5534B),
        muted = Color(0xFFAE9A8D),
        outline = Color(0xFF4D3A2F),
        shadow = Color(0xFF070403),
    ),
    INDIGO(
        id = "indigo",
        ink = Color(0xFF0A0D1C),
        panel = Color(0xFF111731),
        panelLight = Color(0xFF262F58),
        paper = Color(0xFFE9ECFF),
        accent = Color(0xFF9AA8FF),
        amber = Color(0xFFF2B94B),
        red = Color(0xFFE46A6A),
        muted = Color(0xFF959DC2),
        outline = Color(0xFF353F6E),
        shadow = Color(0xFF04050C),
    ),
    RETRO_DMG(
        id = "retro_dmg",
        ink = Color(0xFFC9C6BF),
        panel = Color(0xFFD9D6CF),
        panelLight = Color(0xFFB4B0A8),
        paper = Color(0xFF26252E),
        accent = Color(0xFFB0254F),
        amber = Color(0xFF3F4AA0),
        red = Color(0xFFB0254F),
        muted = Color(0xFF66646B),
        outline = Color(0xFF8E8A83),
        shadow = Color(0xFF98948C),
        isLight = true,
    ),
    PEACH(
        id = "peach",
        ink = Color(0xFF1C1216),
        panel = Color(0xFF291A21),
        panelLight = Color(0xFF46303B),
        paper = Color(0xFFFFF0EA),
        accent = Color(0xFFFFB09C),
        amber = Color(0xFFFFD28A),
        red = Color(0xFFF06F7C),
        muted = Color(0xFFB99FA9),
        outline = Color(0xFF5B404C),
        shadow = Color(0xFF0A0608),
    ),
    ONYX_ICE(
        id = "onyx_ice",
        ink = Color(0xFF060708),
        panel = Color(0xFF0E1114),
        panelLight = Color(0xFF232A30),
        paper = Color(0xFFE6F4F7),
        accent = Color(0xFF7FE3F0),
        amber = Color(0xFFF2C14B),
        red = Color(0xFFE5676B),
        muted = Color(0xFF8C9CA4),
        outline = Color(0xFF2F3A41),
        shadow = Color(0xFF000000),
    ),
    ;

    companion object {
        fun fromId(id: String?): PixelPalette = entries.firstOrNull { it.id == id } ?: GBA
    }
}

object PixelTheme {
    const val PREF_KEY = "pixel_palette"

    var palette by mutableStateOf(PixelPalette.GBA)
        private set

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    /** Loads the saved palette and keeps following later changes from the settings screen. */
    fun init(context: Context) {
        if (listener != null) return
        val preferences = SharedPreferencesHelper.getSharedPreferences(context.applicationContext)
        palette = PixelPalette.fromId(preferences.getString(PREF_KEY, null))
        listener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                if (key == PREF_KEY) palette = PixelPalette.fromId(prefs.getString(PREF_KEY, null))
            }
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }
}
