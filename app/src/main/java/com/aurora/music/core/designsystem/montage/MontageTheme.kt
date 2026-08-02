package com.aurora.music.core.designsystem.montage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aurora.music.domain.repository.AccentSource
import com.aurora.music.domain.repository.AnimationLevel
import com.aurora.music.domain.repository.AppSettings
import com.aurora.music.domain.repository.ThemeMode
import androidx.compose.ui.graphics.lerp

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MONTAGE DESIGN SYSTEM — Theme
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Replaces MaterialTheme entirely. Every screen uses MontageTheme.colors,
 * MontageTheme.typography, etc.
 */

// ── Unified color container (works for light/dark/amoled) ──────────────

@Immutable
data class MontageColors(
    val background: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceElevated: Color,
    val accent: Color,
    val accentDeep: Color,
    val accentContainer: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnAccent: Color,
    val border: Color,
    val borderSubtle: Color,
    val error: Color,
    val success: Color,
    val shadow: Color,
    val shadowStrong: Color,
    val overlay: Color,
    val scrim: Color,
    val favourite: Color,
    val isDark: Boolean,
)

// ── Composition locals ─────────────────────────────────────────────────

val LocalMontageColors = compositionLocalOf { lightMontageColors() }
val LocalMontageTypography = compositionLocalOf { MontageTypography() }
val LocalMontageMotion = compositionLocalOf { MontageMotionScale() }

@Immutable
data class MontageMotionScale(
    val level: AnimationLevel = AnimationLevel.FULL,
) {
    val enabled: Boolean get() = level != AnimationLevel.NONE
    val scale: Float get() = when (level) {
        AnimationLevel.FULL -> 1f
        AnimationLevel.REDUCED -> 0.6f
        AnimationLevel.NONE -> 0f
    }
    fun duration(baseMs: Int): Int = if (level == AnimationLevel.NONE) 0 else (baseMs * scale).toInt()
}

// ── Theme composable ───────────────────────────────────────────────────

@Composable
fun MontageTheme(
    settings: AppSettings = AppSettings(),
    artworkAccent: Color? = null,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }

    val baseColors = remember(settings.themeMode) {
        when (settings.themeMode) {
            ThemeMode.AMOLED -> amoledMontageColors()
            ThemeMode.LIGHT -> lightMontageColors()
            ThemeMode.DARK -> darkMontageColors()
            ThemeMode.SYSTEM -> if (systemDark) darkMontageColors() else lightMontageColors()
        }
    }

    val tintedColors = remember(baseColors, artworkAccent, settings.accentSource) {
        val accent = when (settings.accentSource) {
            AccentSource.ARTWORK -> artworkAccent
            AccentSource.CUSTOM -> Color(settings.customAccentColor)
            AccentSource.DYNAMIC -> null
        }
        if (accent != null) baseColors.tintedWith(accent) else baseColors
    }

    val motionScale = remember(settings.animationLevel) {
        MontageMotionScale(level = settings.animationLevel)
    }

    CompositionLocalProvider(
        LocalMontageColors provides tintedColors,
        LocalMontageTypography provides MontageTypography(),
        LocalMontageMotion provides motionScale,
        content = content,
    )
}

// ── Accessor object ────────────────────────────────────────────────────

object MontageTheme {
    val colors: MontageColors @Composable get() = LocalMontageColors.current
    val typography: MontageTypography @Composable get() = LocalMontageTypography.current
    val motion: MontageMotionScale @Composable get() = LocalMontageMotion.current
}

// ── Color constructors ─────────────────────────────────────────────────

fun lightMontageColors() = MontageColors(
    background = Color(0xFFFFFFFF),
    backgroundSecondary = Color(0xFFF8F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF2F1F6),
    surfaceElevated = Color(0xFFFFFFFF),
    accent = Color(0xFF7C6BFF),
    accentDeep = Color(0xFF4B3CD9),
    accentContainer = Color(0xFFEDEAFF),
    onAccent = Color.White,
    textPrimary = Color(0xFF1A1A2E),
    textSecondary = Color(0xFF6E6E80),
    textTertiary = Color(0xFF9E9EAF),
    textOnAccent = Color.White,
    border = Color(0xFFE8E8EE),
    borderSubtle = Color(0xFFF0F0F5),
    error = Color(0xFFE5484D),
    success = Color(0xFF30A46C),
    shadow = Color(0x1A000000),
    shadowStrong = Color(0x2E000000),
    overlay = Color(0x52000000),
    scrim = Color(0x7A000000),
    favourite = Color(0xFFE5467C),
    isDark = false,
)

fun darkMontageColors() = MontageColors(
    background = Color(0xFF0F0F17),
    backgroundSecondary = Color(0xFF16161F),
    surface = Color(0xFF1C1C28),
    surfaceVariant = Color(0xFF232330),
    surfaceElevated = Color(0xFF262635),
    accent = Color(0xFF9B8AFF),
    accentDeep = Color(0xFF7C6BFF),
    accentContainer = Color(0xFF2A2650),
    onAccent = Color.White,
    textPrimary = Color(0xFFEAEAF2),
    textSecondary = Color(0xFF9E9EAF),
    textTertiary = Color(0xFF6E6E80),
    textOnAccent = Color.White,
    border = Color(0xFF2E2E3E),
    borderSubtle = Color(0xFF222230),
    error = Color(0xFFFF6369),
    success = Color(0xFF4CD964),
    shadow = Color(0x2E000000),
    shadowStrong = Color(0x4D000000),
    overlay = Color(0x7A000000),
    scrim = Color(0x9A000000),
    favourite = Color(0xFFFF6B9D),
    isDark = true,
)

fun amoledMontageColors() = MontageColors(
    background = Color(0xFF000000),
    backgroundSecondary = Color(0xFF0A0A0F),
    surface = Color(0xFF121218),
    surfaceVariant = Color(0xFF1A1A22),
    surfaceElevated = Color(0xFF1E1E28),
    accent = Color(0xFF9B8AFF),
    accentDeep = Color(0xFF7C6BFF),
    accentContainer = Color(0xFF2A2650),
    onAccent = Color.White,
    textPrimary = Color(0xFFEAEAF2),
    textSecondary = Color(0xFF9E9EAF),
    textTertiary = Color(0xFF6E6E80),
    textOnAccent = Color.White,
    border = Color(0xFF1E1E28),
    borderSubtle = Color(0xFF141418),
    error = Color(0xFFFF6369),
    success = Color(0xFF4CD964),
    shadow = Color(0x2E000000),
    shadowStrong = Color(0x4D000000),
    overlay = Color(0x7A000000),
    scrim = Color(0x9A000000),
    favourite = Color(0xFFFF6B9D),
    isDark = true,
)

// ── Accent tinting ─────────────────────────────────────────────────────

private fun MontageColors.tintedWith(accent: Color): MontageColors {
    val strength = if (isDark) 0.85f else 0.7f
    return copy(
        accent = lerp(this.accent, accent, strength),
        accentDeep = lerp(accentDeep, accent, 0.5f),
        accentContainer = lerp(accentContainer, accent, if (isDark) 0.15f else 0.10f),
    )
}
