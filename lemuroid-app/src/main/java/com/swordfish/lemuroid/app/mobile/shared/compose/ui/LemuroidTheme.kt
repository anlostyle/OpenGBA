package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
    lightColorScheme(
        primary = md_theme_light_primary,
        onPrimary = md_theme_light_onPrimary,
        primaryContainer = md_theme_light_primaryContainer,
        onPrimaryContainer = md_theme_light_onPrimaryContainer,
        secondary = md_theme_light_secondary,
        onSecondary = md_theme_light_onSecondary,
        secondaryContainer = md_theme_light_secondaryContainer,
        onSecondaryContainer = md_theme_light_onSecondaryContainer,
        tertiary = md_theme_light_tertiary,
        onTertiary = md_theme_light_onTertiary,
        tertiaryContainer = md_theme_light_tertiaryContainer,
        onTertiaryContainer = md_theme_light_onTertiaryContainer,
        error = md_theme_light_error,
        errorContainer = md_theme_light_errorContainer,
        onError = md_theme_light_onError,
        onErrorContainer = md_theme_light_onErrorContainer,
        background = md_theme_light_background,
        onBackground = md_theme_light_onBackground,
        surface = md_theme_light_surface,
        onSurface = md_theme_light_onSurface,
        surfaceVariant = md_theme_light_surfaceVariant,
        onSurfaceVariant = md_theme_light_onSurfaceVariant,
        outline = md_theme_light_outline,
        inverseOnSurface = md_theme_light_inverseOnSurface,
        inverseSurface = md_theme_light_inverseSurface,
        inversePrimary = md_theme_light_inversePrimary,
        surfaceTint = md_theme_light_surfaceTint,
        outlineVariant = md_theme_light_outlineVariant,
        scrim = md_theme_light_scrim,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF78F08B),
        onPrimary = Color(0xFF06200D),
        primaryContainer = Color(0xFF194A24),
        onPrimaryContainer = Color(0xFFC2FFCA),
        secondary = Color(0xFFB9C9BA),
        onSecondary = Color(0xFF1D2B20),
        secondaryContainer = Color(0xFF304636),
        onSecondaryContainer = Color(0xFFD4E7D5),
        tertiary = Color(0xFFFFC861),
        onTertiary = Color(0xFF372200),
        tertiaryContainer = Color(0xFF5A4100),
        onTertiaryContainer = Color(0xFFFFDEA0),
        error = Color(0xFFFFB4AB),
        errorContainer = Color(0xFF93000A),
        onError = Color(0xFF690005),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0B0F0D),
        onBackground = Color(0xFFE6EDE5),
        surface = Color(0xFF0F1411),
        onSurface = Color(0xFFE6EDE5),
        surfaceVariant = Color(0xFF18211B),
        onSurfaceVariant = Color(0xFFA7B6A9),
        outline = Color(0xFF455148),
        inverseOnSurface = Color(0xFF0B0F0D),
        inverseSurface = Color(0xFFE6EDE5),
        inversePrimary = Color(0xFF17652B),
        surfaceTint = Color(0xFF78F08B),
        outlineVariant = Color(0xFF29352C),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF27322B),
        surfaceDim = Color(0xFF0B0F0D),
        surfaceContainer = Color(0xFF141C17),
        surfaceContainerHigh = Color(0xFF1A251E),
        surfaceContainerHighest = Color(0xFF202D24),
        surfaceContainerLow = Color(0xFF0D1310),
        surfaceContainerLowest = Color(0xFF060A08),
    )

@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    // Keep the handheld palette stable instead of inheriting the phone's wallpaper colors.
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(colorScheme = colors) {
        content()
    }
}
