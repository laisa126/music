package com.aurora.music.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.aurora.music.core.designsystem.montage.MontageTheme

fun Modifier.contourGlow(
    shape: Shape = RoundedCornerShape(20.dp),
    width: Dp = 1.dp,
    intensity: Float = 1f,
    accent: Color? = null,
): Modifier = composed {
    val colors = MontageTheme.colors
    val glow = accent ?: colors.accent
    val topAlpha = (if (colors.isDark) 0.55f else 0.40f) * intensity
    val bottomAlpha = (if (colors.isDark) 0.10f else 0.06f) * intensity
    this.border(
        width = width,
        brush = Brush.linearGradient(
            colors = listOf(
                glow.copy(alpha = topAlpha),
                colors.textPrimary.copy(alpha = bottomAlpha * 0.6f),
                glow.copy(alpha = bottomAlpha),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
        shape = shape,
    )
}

fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(20.dp),
    alpha: Float = 0.72f,
    glow: Boolean = true,
): Modifier = composed {
    val colors = MontageTheme.colors
    val base = this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = if (colors.isDark) {
                    listOf(colors.surfaceElevated.copy(alpha = alpha), colors.surface.copy(alpha = alpha * 0.92f))
                } else {
                    listOf(colors.surface.copy(alpha = alpha + 0.15f), colors.backgroundSecondary.copy(alpha = alpha))
                },
            ),
            shape = shape,
        )
    if (glow) base.contourGlow(shape = shape) else base
}

@Composable
fun artworkScrim(topAlpha: Float = 0.0f, bottomAlpha: Float = 0.85f): Brush {
    val colors = MontageTheme.colors
    return Brush.verticalGradient(
        colors = listOf(
            colors.background.copy(alpha = topAlpha),
            colors.background.copy(alpha = bottomAlpha * 0.5f),
            colors.background.copy(alpha = bottomAlpha),
        ),
    )
}
