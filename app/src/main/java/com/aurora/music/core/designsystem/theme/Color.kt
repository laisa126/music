package com.aurora.music.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Aurora palette — a premium identity of its own, not a Spotify clone.
val AuroraViolet = Color(0xFF7C6BFF)
val AuroraVioletDeep = Color(0xFF4B3CD9)
val AuroraCyan = Color(0xFF48D6FF)
val AuroraMagenta = Color(0xFFFF6BC1)
val AuroraAmber = Color(0xFFFFC46B)

val SurfaceNight = Color(0xFF0F0F17)
val SurfaceNightElevated = Color(0xFF171722)
val SurfaceAmoled = Color(0xFF000000)
val SurfaceDay = Color(0xFFFBFAFF)
val SurfaceDayElevated = Color(0xFFFFFFFF)

val AuroraLightColors = lightColorScheme(
    primary = AuroraVioletDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E0FF),
    onPrimaryContainer = Color(0xFF1B1148),
    secondary = Color(0xFF00889E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDF2FB),
    onSecondaryContainer = Color(0xFF002B33),
    tertiary = Color(0xFFB03A7E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9EB),
    onTertiaryContainer = Color(0xFF3B0A26),
    background = SurfaceDay,
    onBackground = Color(0xFF16151C),
    surface = SurfaceDay,
    onSurface = Color(0xFF16151C),
    surfaceVariant = Color(0xFFE7E3F0),
    onSurfaceVariant = Color(0xFF48455A),
    surfaceContainer = Color(0xFFF3F1FA),
    surfaceContainerHigh = SurfaceDayElevated,
    surfaceContainerHighest = Color(0xFFFFFFFF),
    outline = Color(0xFF7A7690),
    outlineVariant = Color(0xFFCBC7DA),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

val AuroraDarkColors = darkColorScheme(
    primary = AuroraViolet,
    onPrimary = Color(0xFF1B1148),
    primaryContainer = Color(0xFF352A8C),
    onPrimaryContainer = Color(0xFFE5E0FF),
    secondary = AuroraCyan,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF00505E),
    onSecondaryContainer = Color(0xFFCDF2FB),
    tertiary = AuroraMagenta,
    onTertiary = Color(0xFF54103A),
    tertiaryContainer = Color(0xFF762A55),
    onTertiaryContainer = Color(0xFFFFD9EB),
    background = SurfaceNight,
    onBackground = Color(0xFFE7E4F2),
    surface = SurfaceNight,
    onSurface = Color(0xFFE7E4F2),
    surfaceVariant = Color(0xFF2A2836),
    onSurfaceVariant = Color(0xFFC9C4DA),
    surfaceContainer = SurfaceNightElevated,
    surfaceContainerHigh = Color(0xFF1E1E2B),
    surfaceContainerHighest = Color(0xFF262635),
    outline = Color(0xFF938FA6),
    outlineVariant = Color(0xFF48455A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

/** True-black variant for AMOLED panels. */
val AuroraAmoledColors = AuroraDarkColors.copy(
    background = SurfaceAmoled,
    surface = SurfaceAmoled,
    surfaceContainer = Color(0xFF0A0A0F),
    surfaceContainerHigh = Color(0xFF101017),
    surfaceContainerHighest = Color(0xFF16161F),
)
