package com.limpyo.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/** What the user picked in Settings. Dark (Night Route) is the default. */
enum class ThemeMode { Dark, Light, System }

val LocalLimpyoColors = staticCompositionLocalOf { LimpyoPalettes.Night }
val LocalCategoryColors = staticCompositionLocalOf { LimpyoPalettes.NightCategories }
val LocalLimpyoType = staticCompositionLocalOf { LimpyoTypography.Default }

/**
 * How screens read the theme:
 *   LimpyoTheme.colors.accent, LimpyoTheme.type.body, LimpyoSpace.l, LimpyoRadius.large ...
 * Never hard-code a color or size in a screen (docs/QUALITY.md, definition of done).
 */
object LimpyoTheme {
    val colors: LimpyoColors
        @Composable @ReadOnlyComposable get() = LocalLimpyoColors.current
    val categories: LimpyoCategoryColors
        @Composable @ReadOnlyComposable get() = LocalCategoryColors.current
    val type: LimpyoTypography
        @Composable @ReadOnlyComposable get() = LocalLimpyoType.current
}

@Composable
fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.Dark -> true
    ThemeMode.Light -> false
    ThemeMode.System -> isSystemInDarkTheme()
}

@Composable
fun LimpyoTheme(mode: ThemeMode = ThemeMode.Dark, content: @Composable () -> Unit) {
    val dark = isDarkTheme(mode)
    val colors = if (dark) LimpyoPalettes.Night else LimpyoPalettes.Day
    val categories = if (dark) LimpyoPalettes.NightCategories else LimpyoPalettes.DayCategories
    val type = LimpyoTypography.Default

    // Material 3 is only a base (ripples, pull-to-refresh, text selection).
    // Its color roles are mapped to our tokens so nothing falls back to Material purple.
    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.accent, onPrimary = colors.onAccent,
            primaryContainer = colors.accentContainer, onPrimaryContainer = colors.ink,
            secondary = colors.info, onSecondary = colors.ground,
            background = colors.ground, onBackground = colors.ink,
            surface = colors.surface, onSurface = colors.ink,
            surfaceVariant = colors.raised, onSurfaceVariant = colors.muted,
            surfaceContainer = colors.surface, surfaceContainerHigh = colors.raised,
            outline = colors.border, outlineVariant = colors.divider,
            error = colors.danger, errorContainer = colors.dangerContainer,
            scrim = colors.scrim,
        )
    } else {
        lightColorScheme(
            primary = colors.accentStrong, onPrimary = colors.surface,
            primaryContainer = colors.accentContainer, onPrimaryContainer = colors.ink,
            secondary = colors.info, onSecondary = colors.surface,
            background = colors.ground, onBackground = colors.ink,
            surface = colors.surface, onSurface = colors.ink,
            surfaceVariant = colors.raised, onSurfaceVariant = colors.muted,
            surfaceContainer = colors.surface, surfaceContainerHigh = colors.raised,
            outline = colors.border, outlineVariant = colors.divider,
            error = colors.danger, errorContainer = colors.dangerContainer,
            scrim = colors.scrim,
        )
    }

    CompositionLocalProvider(
        LocalLimpyoColors provides colors,
        LocalCategoryColors provides categories,
        LocalLimpyoType provides type,
    ) {
        MaterialTheme(colorScheme = scheme, typography = type.toMaterial(), content = content)
    }
}
