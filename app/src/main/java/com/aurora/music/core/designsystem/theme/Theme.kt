package com.aurora.music.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.LocalMontageColors
import com.aurora.music.core.designsystem.montage.LocalMontageTypography
import com.aurora.music.core.designsystem.montage.LocalMontageMotion

@Composable
fun AuroraTheme(
    settings: AppSettings = AppSettings(),
    artworkAccent: Color? = null,
    content: @Composable () -> Unit,
) {
    MontageTheme(
        settings = settings,
        artworkAccent = artworkAccent,
    ) {
        val montageColors = MontageTheme.colors
        val dark = montageColors.isDark

        val materialScheme = if (dark) {
            androidx.compose.material3.darkColorScheme(
                primary = montageColors.accent,
                onPrimary = montageColors.onAccent,
                primaryContainer = montageColors.accentContainer,
                background = montageColors.background,
                onBackground = montageColors.textPrimary,
                surface = montageColors.surface,
                onSurface = montageColors.textPrimary,
                surfaceVariant = montageColors.surfaceVariant,
                onSurfaceVariant = montageColors.textSecondary,
                outline = montageColors.border,
                outlineVariant = montageColors.borderSubtle,
                error = montageColors.error,
                tertiary = montageColors.favourite,
                secondary = montageColors.accent,
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = montageColors.accent,
                onPrimary = montageColors.onAccent,
                primaryContainer = montageColors.accentContainer,
                background = montageColors.background,
                onBackground = montageColors.textPrimary,
                surface = montageColors.surface,
                onSurface = montageColors.textPrimary,
                surfaceVariant = montageColors.surfaceVariant,
                onSurfaceVariant = montageColors.textSecondary,
                outline = montageColors.border,
                outlineVariant = montageColors.borderSubtle,
                error = montageColors.error,
                tertiary = montageColors.favourite,
                secondary = montageColors.accent,
            )
        }

        val typography = MontageTheme.typography
        val materialTypography = androidx.compose.material3.Typography(
            displayLarge = androidx.compose.ui.text.TextStyle(fontSize = typography.display.size.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            headlineMedium = androidx.compose.ui.text.TextStyle(fontSize = typography.title.size.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = typography.heading.size.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            titleLarge = androidx.compose.ui.text.TextStyle(fontSize = typography.heading.size.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            titleMedium = androidx.compose.ui.text.TextStyle(fontSize = typography.body.size.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            titleSmall = androidx.compose.ui.text.TextStyle(fontSize = typography.caption.size.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = typography.body.size.sp),
            bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = typography.caption.size.sp),
            bodySmall = androidx.compose.ui.text.TextStyle(fontSize = typography.mini.size.sp),
            labelLarge = androidx.compose.ui.text.TextStyle(fontSize = typography.label.size.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            labelMedium = androidx.compose.ui.text.TextStyle(fontSize = typography.mini.size.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            labelSmall = androidx.compose.ui.text.TextStyle(fontSize = typography.mini.size.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
        )

        androidx.compose.material3.MaterialTheme(
            colorScheme = materialScheme,
            typography = materialTypography,
            content = content,
        )
    }
}

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

    fun duration(base: Int): Int =
        if (animationLevel == AnimationLevel.NONE) 0 else (base * motionScale).toInt()
}

val LocalAuroraTokens = staticCompositionLocalOf { AuroraTokens() }

object AuroraTheme {
    val tokens: AuroraTokens
        @Composable get() = LocalAuroraTokens.current
}
