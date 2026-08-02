package com.aurora.music.core.designsystem.montage

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MONTAGE DESIGN SYSTEM — Design Tokens
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * A premium, custom design language inspired by ColorOS, Apple Music,
 * and Nothing OS. Every value is a deliberate token — no magic numbers.
 */

// ──────────────────────────────────────────────────────────────────────────
// COLOR TOKENS
// ──────────────────────────────────────────────────────────────────────────

/** Light palette — soft, warm, premium. */
@Immutable
data class MontageLightColors(
    val background: Color = Color(0xFFFFFFFF),
    val backgroundSecondary: Color = Color(0xFFF8F8FA),
    val surface: Color = Color(0xFFFFFFFF),
    val surfaceVariant: Color = Color(0xFFF2F1F6),
    val surfaceElevated: Color = Color(0xFFFFFFFF),

    val accent: Color = Color(0xFF7C6BFF),
    val accentDeep: Color = Color(0xFF4B3CD9),
    val accentContainer: Color = Color(0xFFEDEAFF),
    val onAccent: Color = Color.White,

    val textPrimary: Color = Color(0xFF1A1A2E),
    val textSecondary: Color = Color(0xFF6E6E80),
    val textTertiary: Color = Color(0xFF9E9EAF),
    val textOnAccent: Color = Color.White,

    val border: Color = Color(0xFFE8E8EE),
    val borderSubtle: Color = Color(0xFFF0F0F5),

    val error: Color = Color(0xFFE5484D),
    val success: Color = Color(0xFF30A46C),

    val shadow: Color = Color(0x1A000000),
    val shadowStrong: Color = Color(0x2E000000),
    val overlay: Color = Color(0x52000000),
    val scrim: Color = Color(0x7A000000),

    val favourite: Color = Color(0xFFE5467C),
)

/** Dark palette — deep, rich, OLED-friendly. */
@Immutable
data class MontageDarkColors(
    val background: Color = Color(0xFF0F0F17),
    val backgroundSecondary: Color = Color(0xFF16161F),
    val surface: Color = Color(0xFF1C1C28),
    val surfaceVariant: Color = Color(0xFF232330),
    val surfaceElevated: Color = Color(0xFF262635),

    val accent: Color = Color(0xFF9B8AFF),
    val accentDeep: Color = Color(0xFF7C6BFF),
    val accentContainer: Color = Color(0xFF2A2650),
    val onAccent: Color = Color.White,

    val textPrimary: Color = Color(0xFFEAEAF2),
    val textSecondary: Color = Color(0xFF9E9EAF),
    val textTertiary: Color = Color(0xFF6E6E80),
    val textOnAccent: Color = Color.White,

    val border: Color = Color(0xFF2E2E3E),
    val borderSubtle: Color = Color(0xFF222230),

    val error: Color = Color(0xFFFF6369),
    val success: Color = Color(0xFF4CD964),

    val shadow: Color = Color(0x2E000000),
    val shadowStrong: Color = Color(0x4D000000),
    val overlay: Color = Color(0x7A000000),
    val scrim: Color = Color(0x9A000000),

    val favourite: Color = Color(0xFFFF6B9D),
)

/** AMOLED palette — true black. */
@Immutable
data class MontageAmoledColors(
    val background: Color = Color(0xFF000000),
    val backgroundSecondary: Color = Color(0xFF0A0A0F),
    val surface: Color = Color(0xFF121218),
    val surfaceVariant: Color = Color(0xFF1A1A22),
    val surfaceElevated: Color = Color(0xFF1E1E28),

    val accent: Color = Color(0xFF9B8AFF),
    val accentDeep: Color = Color(0xFF7C6BFF),
    val accentContainer: Color = Color(0xFF2A2650),
    val onAccent: Color = Color.White,

    val textPrimary: Color = Color(0xFFEAEAF2),
    val textSecondary: Color = Color(0xFF9E9EAF),
    val textTertiary: Color = Color(0xFF6E6E80),
    val textOnAccent: Color = Color.White,

    val border: Color = Color(0xFF1E1E28),
    val borderSubtle: Color = Color(0xFF141418),

    val error: Color = Color(0xFFFF6369),
    val success: Color = Color(0xFF4CD964),

    val shadow: Color = Color(0x2E000000),
    val shadowStrong: Color = Color(0x4D000000),
    val overlay: Color = Color(0x7A000000),
    val scrim: Color = Color(0x9A000000),

    val favourite: Color = Color(0xFFFF6B9D),
)

// ──────────────────────────────────────────────────────────────────────────
// TYPOGRAPHY TOKENS
// ──────────────────────────────────────────────────────────────────────────

@Immutable
data class MontageTypography(
    val display: MontageTextStyle = MontageTextStyle(size = 32, lineHeight = 40, weight = 700),
    val title: MontageTextStyle = MontageTextStyle(size = 28, lineHeight = 36, weight = 600),
    val heading: MontageTextStyle = MontageTextStyle(size = 22, lineHeight = 30, weight = 600),
    val body: MontageTextStyle = MontageTextStyle(size = 17, lineHeight = 24, weight = 400),
    val caption: MontageTextStyle = MontageTextStyle(size = 14, lineHeight = 20, weight = 400),
    val label: MontageTextStyle = MontageTextStyle(size = 13, lineHeight = 18, weight = 500),
    val labelLarge: MontageTextStyle = MontageTextStyle(size = 15, lineHeight = 22, weight = 600),
    val mini: MontageTextStyle = MontageTextStyle(size = 11, lineHeight = 16, weight = 500),
)

@Immutable
data class MontageTextStyle(
    val size: Int,
    val lineHeight: Int,
    val weight: Int,
    val letterSpacing: Double = 0.0,
)

// ──────────────────────────────────────────────────────────────────────────
// SHAPE TOKENS
// ──────────────────────────────────────────────────────────────────────────

object MontageShapes {
    val small = 16.dp
    val medium = 24.dp
    val large = 32.dp
    val hero = 30.dp
    val pill = 50  // percent for full pill
    val miniPlayer = 28.dp
    val button = 50  // pill
    val card = 24.dp
    val dialog = 28.dp
    val bottomSheet = 32.dp
    val textField = 20.dp
    val icon = 14.dp
    val badge = 8.dp
}

// ──────────────────────────────────────────────────────────────────────────
// SPACING TOKENS
// ──────────────────────────────────────────────────────────────────────────

object MontageSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val base = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    val giant = 48.dp

    // Semantic
    val screenHorizontal = 20.dp
    val sectionGap = 24.dp
    val cardPadding = 16.dp
    val listGap = 6.dp
    val touchTarget = 48.dp
}

// ──────────────────────────────────────────────────────────────────────────
// ELEVATION / SHADOW TOKENS
// ──────────────────────────────────────────────────────────────────────────

object MontageElevation {
    val none = 0.dp
    val subtle = 2.dp
    val low = 4.dp
    val medium = 8.dp
    val high = 16.dp
    val floating = 24.dp
}

// ──────────────────────────────────────────────────────────────────────────
// ANIMATION TOKENS
// ──────────────────────────────────────────────────────────────────────────

object MontageMotion {
    const val instant = 0
    const val fast = 150
    const val normal = 250
    const val slow = 400
    const val scenic = 600

    const val springDamping = 0.78f
    const val springStiffness = 400f

    const val fadeDuration = 200
    const val scaleDuration = 300
    const val slideDuration = 350
}

// ──────────────────────────────────────────────────────────────────────────
// ICON SIZING
// ──────────────────────────────────────────────────────────────────────────

object MontageIcons {
    val tiny = 12.dp
    val small = 16.dp
    val medium = 20.dp
    val large = 24.dp
    val xl = 28.dp
    val xxl = 32.dp
    val hero = 48.dp
}

// ──────────────────────────────────────────────────────────────────────────
// STROKE TOKENS
// ──────────────────────────────────────────────────────────────────────────

object MontageStrokes {
    val hairline = 0.5.dp
    val thin = 1.dp
    val regular = 1.5.dp
    val medium = 2.dp
    val thick = 3.dp
}

// ──────────────────────────────────────────────────────────────────────────
// SLIDER TOKENS
// ──────────────────────────────────────────────────────────────────────────

object MontageSlider {
    val trackHeight = 6.dp
    val thumbSize = 20.dp
    val thumbPressScale = 1.3f
}
