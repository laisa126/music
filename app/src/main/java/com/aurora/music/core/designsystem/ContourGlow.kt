package com.aurora.music.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aurora.music.core.designsystem.theme.LocalAuroraTokens

/**
 * ColorOS-inspired **Contour Glow**: soft edge lighting on a panel's border
 * rather than a flat frosted fill (spec Section 3).
 *
 * Honours the user's "disable Contour Glow" toggle — when off, this degrades to
 * a plain hairline outline, which is also cheaper to draw.
 */
fun Modifier.contourGlow(
    shape: Shape = RoundedCornerShape(20.dp),
    width: Dp = 1.dp,
    intensity: Float = 1f,
    accent: Color? = null,
): Modifier = composed {
    val tokens = LocalAuroraTokens.current
    val scheme = MaterialTheme.colorScheme
    val glow = accent ?: tokens.glowColor

    if (!tokens.contourGlowEnabled) {
        return@composed this.border(width, scheme.outlineVariant.copy(alpha = 0.5f), shape)
    }

    val topAlpha = (if (tokens.isDark) 0.55f else 0.40f) * intensity
    val bottomAlpha = (if (tokens.isDark) 0.10f else 0.06f) * intensity

    this.border(
        width = width,
        brush = Brush.linearGradient(
            colors = listOf(
                glow.copy(alpha = topAlpha),
                scheme.onSurface.copy(alpha = bottomAlpha * 0.6f),
                glow.copy(alpha = bottomAlpha),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
        shape = shape,
    )
}

/**
 * A translucent, glass-like panel surface: the base treatment for cards,
 * sheets and the mini player.
 */
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(20.dp),
    alpha: Float = 0.72f,
    glow: Boolean = true,
): Modifier = composed {
    val scheme = MaterialTheme.colorScheme
    val tokens = LocalAuroraTokens.current

    val base = this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = if (tokens.isDark) {
                    listOf(
                        scheme.surfaceContainerHigh.copy(alpha = alpha),
                        scheme.surfaceContainer.copy(alpha = alpha * 0.92f),
                    )
                } else {
                    listOf(
                        scheme.surfaceContainerHighest.copy(alpha = alpha + 0.15f),
                        scheme.surfaceContainer.copy(alpha = alpha),
                    )
                },
            ),
            shape = shape,
        )

    if (glow) base.contourGlow(shape = shape) else base
}

/** Vertical scrim so text stays legible over full-bleed artwork. */
@Composable
fun artworkScrim(topAlpha: Float = 0.0f, bottomAlpha: Float = 0.85f): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.verticalGradient(
        colors = listOf(
            scheme.surface.copy(alpha = topAlpha),
            scheme.surface.copy(alpha = bottomAlpha * 0.5f),
            scheme.surface.copy(alpha = bottomAlpha),
        ),
    )
}
