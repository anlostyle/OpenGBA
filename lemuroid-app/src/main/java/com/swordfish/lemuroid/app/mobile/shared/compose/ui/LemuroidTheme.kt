package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        primary = PixelGreen,
        onPrimary = PixelInk,
        primaryContainer = Color(0xFF354D1B),
        onPrimaryContainer = PixelGreen,
        secondary = PixelPaper,
        onSecondary = PixelInk,
        secondaryContainer = PixelPanelLight,
        onSecondaryContainer = PixelPaper,
        tertiary = PixelAmber,
        onTertiary = PixelInk,
        tertiaryContainer = Color(0xFF604717),
        onTertiaryContainer = PixelPaper,
        error = PixelRed,
        errorContainer = Color(0xFF93000A),
        onError = PixelInk,
        onErrorContainer = Color(0xFFFFDAD6),
        background = PixelInk,
        onBackground = PixelPaper,
        surface = PixelInk,
        onSurface = PixelPaper,
        surfaceVariant = PixelPanel,
        onSurfaceVariant = PixelMuted,
        outline = PixelOutline,
        inverseOnSurface = PixelInk,
        inverseSurface = PixelPaper,
        inversePrimary = Color(0xFF5F7F21),
        surfaceTint = PixelGreen,
        outlineVariant = PixelOutline,
        scrim = Color(0xFF000000),
        surfaceBright = PixelPanelLight,
        surfaceDim = PixelInk,
        surfaceContainer = PixelPanel,
        surfaceContainerHigh = PixelPanelLight,
        surfaceContainerHighest = Color(0xFF2A3C46),
        surfaceContainerLow = Color(0xFF10191E),
        surfaceContainerLowest = PixelShadow,
    )

private val PixelTypography =
    Typography().let { base ->
        base.copy(
            headlineLarge =
                base.headlineLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                ),
            headlineMedium =
                base.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                ),
            titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
            titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Bold),
            labelLarge =
                base.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp,
                ),
            labelMedium = base.labelMedium.copy(fontFamily = FontFamily.Monospace),
            labelSmall = base.labelSmall.copy(fontFamily = FontFamily.Monospace),
        )
    }

private val PixelShapes =
    Shapes(
        extraSmall = CutCornerShape(2.dp),
        small = CutCornerShape(3.dp),
        medium = CutCornerShape(4.dp),
        large = CutCornerShape(6.dp),
        extraLarge = CutCornerShape(8.dp),
    )

@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    // Keep the handheld palette stable instead of inheriting the phone's wallpaper colors.
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = PixelTypography,
        shapes = PixelShapes,
    ) {
        content()
    }
}
