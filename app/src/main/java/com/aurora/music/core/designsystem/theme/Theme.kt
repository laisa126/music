package com.aurora.music.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurora.music.domain.repository.AccentSource
import com.aurora.music.domain.repository.AnimationLevel
import com.aurora.music.domain.repository.AppSettings
import com.aurora.music.domain.repository.ThemeMode

/** Generous, ColorOS-style rounding. */
val AuroraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/**
 * Design-token extras that Material 3 doesn't model: the Contour Glow
 * treatment and the current motion budget (spec Section 3).
 */
@Immutable
data class AuroraTokens(
    val contourGlowEnabled: Boolean = true,
    val animationLevel: AnimationLevel = AnimationLevel.FULL,
    val glowColor: Color = AuroraViolet,
    val isDark: Boolean = true,
) {
    val motionScale: Float
        get() = when (animationLevel) {
            AnimationLevel.FULL -> 1f
            AnimationLevel.REDUCED -> 0.6f
            AnimationLevel.NONE -> 0f
        }

    val animationsEnabled: Boolean get() = animationLevel != AnimationLevel.NONE

    /** Duration in ms scaled by the user's motion preference. */
    fun duration(base: Int): Int =
        if (animationLevel == AnimationLevel.NONE) 0 else (base * motionScale).toInt()
}

val LocalAuroraTokens = staticCompositionLocalOf { AuroraTokens() }

@Composable
fun AuroraTheme(
    settings: AppSettings = AppSettings(),
    /** Accent sampled from current album artwork, when available. */
    artworkAccent: Color? = null,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val context = LocalContext.current

    val baseScheme: ColorScheme = remember(settings.themeMode, settings.accentSource, dark) {
        val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        when {
            settings.accentSource == AccentSource.DYNAMIC && supportsDynamic ->
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            settings.themeMode == ThemeMode.AMOLED -> AuroraAmoledColors
            dark -> AuroraDarkColors
            else -> AuroraLightColors
        }
    }

    val accent: Color? = when (settings.accentSource) {
        AccentSource.ARTWORK -> artworkAccent
        AccentSource.CUSTOM -> Color(settings.customAccentColor.toULong().toLong())
        AccentSource.DYNAMIC -> null
    }

    val scheme = remember(baseScheme, accent, settings.themeMode) {
        val tinted = accent?.let { baseScheme.tintedWith(it, dark) } ?: baseScheme
        if (settings.themeMode == ThemeMode.AMOLED) {
            tinted.copy(
                background = SurfaceAmoled,
                surface = SurfaceAmoled,
                surfaceContainer = Color(0xFF0A0A0F),
                surfaceContainerHigh = Color(0xFF101017),
                surfaceContainerHighest = Color(0xFF16161F),
            )
        } else {
            tinted
        }
    }

    val tokens = remember(settings, scheme, dark) {
        AuroraTokens(
            contourGlowEnabled = settings.contourGlowEnabled,
            animationLevel = settings.animationLevel,
            glowColor = scheme.primary,
            isDark = dark,
        )
    }

    CompositionLocalProvider(LocalAuroraTokens provides tokens) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AuroraTypography,
            shapes = AuroraShapes,
            content = content,
        )
    }
}

/** Blends an artwork-derived accent into the scheme without wrecking contrast. */
private fun ColorScheme.tintedWith(accent: Color, dark: Boolean): ColorScheme {
    val strength = if (dark) 0.85f else 0.7f
    return copy(
        primary = lerp(primary, accent, strength),
        primaryContainer = lerp(primaryContainer, accent, 0.35f),
        secondary = lerp(secondary, accent, 0.25f),
        surfaceContainer = lerp(surfaceContainer, accent, if (dark) 0.06f else 0.04f),
        surfaceContainerHigh = lerp(surfaceContainerHigh, accent, if (dark) 0.09f else 0.05f),
    )
}

object AuroraTheme {
    val tokens: AuroraTokens
        @Composable get() = LocalAuroraTokens.current
}
