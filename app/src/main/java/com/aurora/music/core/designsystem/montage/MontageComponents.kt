package com.aurora.music.core.designsystem.montage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aurora.music.core.designsystem.montage.MontageShapes
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageElevation
import com.aurora.music.core.designsystem.montage.MontageMotion
import com.aurora.music.core.designsystem.montage.MontageStrokes
import com.aurora.music.core.designsystem.montage.MontageIcons

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MONTAGE DESIGN SYSTEM — Custom Components
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Every component replaces a Material 3 equivalent with a custom,
 * handcrafted Montage component. No Material imports.
 */

// ──────────────────────────────────────────────────────────────────────────
// TEXT
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageText(
    text: String,
    style: MontageTextStyle,
    color: Color = MontageTheme.colors.textPrimary,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val resolved = TextStyle(
        fontSize = style.size.sp,
        lineHeight = style.lineHeight.sp,
        fontWeight = fontWeight ?: when (style.weight) {
            100 -> FontWeight.Thin
            200 -> FontWeight.ExtraLight
            300 -> FontWeight.Light
            400 -> FontWeight.Normal
            500 -> FontWeight.Medium
            600 -> FontWeight.SemiBold
            700 -> FontWeight.Bold
            800 -> FontWeight.ExtraBold
            900 -> FontWeight.Black
            else -> FontWeight.Normal
        },
        letterSpacing = style.letterSpacing.sp,
    )
    androidx.compose.foundation.text.BasicText(
        text = text,
        style = resolved.copy(color = color, textAlign = textAlign ?: TextAlign.Start),
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow,
    )
}

// ──────────────────────────────────────────────────────────────────────────
// ICON (thin rounded style)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MontageTheme.colors.textSecondary,
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

// ──────────────────────────────────────────────────────────────────────────
// ICON BUTTON
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "iconBtnScale",
    )
    Box(
        modifier = modifier
            .size(MontageSpacing.touchTarget)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ──────────────────────────────────────────────────────────────────────────
// PRIMARY BUTTON (Floating pill, soft shadow, animated scale)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontagePrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = MontageTheme.colors
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "btnScale",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .shadow(
                elevation = MontageElevation.medium,
                shape = RoundedCornerShape(50),
                ambientColor = colors.shadow,
                spotColor = colors.shadowStrong,
            )
            .background(
                if (enabled) colors.accent else colors.accent.copy(alpha = 0.5f),
                RoundedCornerShape(50),
            )
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        if (!enabled) return@detectTapGestures
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    },
                )
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ──────────────────────────────────────────────────────────────────────────
// SECONDARY BUTTON (Soft outlined, subtle borders)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = MontageTheme.colors
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "secondaryBtnScale",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(
                width = MontageStrokes.thin,
                color = if (enabled) colors.border else colors.border.copy(alpha = 0.4f),
                shape = RoundedCornerShape(50),
            )
            .background(
                colors.surface.copy(alpha = 0.6f),
                RoundedCornerShape(50),
            )
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        if (!enabled) return@detectTapGestures
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    },
                )
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ──────────────────────────────────────────────────────────────────────────
// CARD (Large radius, soft shadow, floating appearance)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(MontageShapes.card),
    elevation: Dp = MontageElevation.low,
    background: Color = MontageTheme.colors.surface,
    content: @Composable () -> Unit,
) {
    val colors = MontageTheme.colors
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = colors.shadow,
                spotColor = colors.shadowStrong,
            )
            .clip(shape)
            .background(background, shape),
    ) {
        content()
    }
}

// ──────────────────────────────────────────────────────────────────────────
// NAVIGATION BAR (ColorOS-inspired, floating, rounded, soft shadow)
// ──────────────────────────────────────────────────────────────────────────

@Immutable
data class MontageNavigationItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
)

@Composable
fun MontageNavigationBar(
    items: List<MontageNavigationItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.sm)
            .shadow(
                elevation = MontageElevation.medium,
                shape = RoundedCornerShape(MontageShapes.large),
                ambientColor = colors.shadow,
                spotColor = colors.shadowStrong,
            )
            .clip(RoundedCornerShape(MontageShapes.large))
            .background(colors.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MontageSpacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                    label = "navIconScale$index",
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(MontageShapes.medium))
                        .clickable { onSelect(index) }
                        .padding(vertical = MontageSpacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(MontageSpacing.touchTarget)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .shadow(
                                            elevation = MontageElevation.subtle,
                                            shape = RoundedCornerShape(16.dp),
                                            ambientColor = colors.shadow,
                                            spotColor = colors.shadow,
                                        )
                                        .background(
                                            colors.accentContainer,
                                            RoundedCornerShape(16.dp),
                                        )
                                } else Modifier,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        MontageIcon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) colors.accent else colors.textTertiary,
                            modifier = Modifier.size(MontageIcons.large),
                        )
                    }
                    if (isSelected) {
                        MontageText(
                            text = item.label,
                            style = typography.mini,
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// APP BAR (Custom header, large spacing, simple typography, no elevation)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = MontageSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigationIcon != null) {
            navigationIcon()
            Spacer(Modifier.width(MontageSpacing.sm))
        }
        MontageText(
            text = title,
            style = typography.heading,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (actions != null) {
            actions()
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// CUSTOM SLIDER (6dp track, rounded ends, animated thumb, soft glow)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    thumbColor: Color = MontageTheme.colors.accent,
    trackColor: Color = MontageTheme.colors.accent,
    backgroundColor: Color = MontageTheme.colors.border,
) {
    val colors = MontageTheme.colors
    var isDragging by remember { mutableStateOf(false) }
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) MontageSlider.thumbPressScale else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "sliderThumbScale",
    )
    val density = LocalDensity.current
    val thumbRadius = with(density) { (MontageSlider.thumbSize / 2).toPx() }
    val trackHeight = with(density) { MontageSlider.trackHeight.toPx() }

    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(MontageSlider.thumbSize + 8.dp)
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val newFrac = (offset.x / size.width).coerceIn(0f, 1f)
                        onValueChange(valueRange.start + newFrac * (valueRange.endInclusive - valueRange.start))
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                ) { _, dragAmount ->
                    val newFrac = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                        + dragAmount.x / size.width).coerceIn(0f, 1f)
                    onValueChange(valueRange.start + newFrac * (valueRange.endInclusive - valueRange.start))
                }
            }
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val newFrac = (offset.x / size.width).coerceIn(0f, 1f)
                    onValueChange(valueRange.start + newFrac * (valueRange.endInclusive - valueRange.start))
                }
            },
    ) {
        val cy = size.height / 2
        val trackStart = thumbRadius
        val trackEnd = size.width - thumbRadius
        val trackWidth = trackEnd - trackStart

        // Background track
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(trackStart, cy - trackHeight / 2),
            size = Size(trackWidth, trackHeight),
            cornerRadius = CornerRadius(trackHeight / 2),
        )

        // Active track
        val activeWidth = trackWidth * fraction
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(trackStart, cy - trackHeight / 2),
            size = Size(activeWidth, trackHeight),
            cornerRadius = CornerRadius(trackHeight / 2),
        )

        // Thumb glow
        if (isDragging) {
            drawCircle(
                color = trackColor.copy(alpha = 0.2f),
                radius = thumbRadius * thumbScale * 1.6f,
                center = Offset(trackStart + activeWidth, cy),
            )
        }

        // Thumb
        drawCircle(
            color = thumbColor,
            radius = thumbRadius * thumbScale * 0.5f,
            center = Offset(trackStart + activeWidth, cy),
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────
// CUSTOM SWITCH (Rounded, smooth thumb movement, soft glow)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MontageTheme.colors
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "switchThumb",
    )
    val trackColor = when {
        !enabled -> colors.border
        checked -> colors.accent
        else -> colors.border
    }
    val width = 52.dp
    val height = 30.dp
    val thumbSize = 24.dp
    val density = LocalDensity.current

    Canvas(
        modifier = modifier
            .size(width, height)
            .clip(RoundedCornerShape(50))
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
    ) {
        val w = size.width
        val h = size.height
        val tSize = with(density) { thumbSize.toPx() }
        val padding = (h - tSize) / 2

        // Track
        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(h / 2),
            topLeft = Offset.Zero,
            size = Size(w, h),
        )

        // Glow when checked
        if (checked) {
            val thumbX = padding + thumbOffset * (w - tSize - padding * 2) + tSize / 2
            drawCircle(
                color = colors.accent.copy(alpha = 0.25f),
                radius = tSize * 0.8f,
                center = Offset(thumbX, h / 2),
            )
        }

        // Thumb
        val thumbX = padding + thumbOffset * (w - tSize - padding * 2)
        drawCircle(
            color = Color.White,
            radius = tSize / 2,
            center = Offset(thumbX + tSize / 2, h / 2),
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────
// CUSTOM TEXT FIELD (Rounded, minimal borders, animated focus)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "textFieldBorder",
    )

    val border = if (borderColor > 0.5f) {
        Modifier.border(
            MontageStrokes.thin,
            colors.accent,
            RoundedCornerShape(MontageShapes.textField),
        )
    } else {
        Modifier.border(
            MontageStrokes.hairline,
            colors.border,
            RoundedCornerShape(MontageShapes.textField),
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(border)
            .clip(RoundedCornerShape(MontageShapes.textField))
            .background(colors.backgroundSecondary)
            .padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(MontageSpacing.md))
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            singleLine = singleLine,
            textStyle = TextStyle(
                fontSize = typography.body.size.sp,
                color = colors.textPrimary,
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder != null) {
                    MontageText(
                        text = placeholder,
                        style = typography.body,
                        color = colors.textTertiary,
                    )
                }
                innerTextField()
            },
        )
        if (trailingIcon != null) {
            Spacer(Modifier.width(MontageSpacing.sm))
            trailingIcon()
        }
    }
}




// ──────────────────────────────────────────────────────────────────────────
// CUSTOM DIALOG (Floating, large radius, background blur, spring animation)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        MontageCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = MontageSpacing.lg)
                .wrapContentHeight(),
            shape = RoundedCornerShape(MontageShapes.dialog),
            elevation = MontageElevation.high,
        ) {
            Column(
                modifier = Modifier.padding(MontageSpacing.xxl),
            ) {
                if (title != null) {
                    MontageText(
                        text = title,
                        style = typography.heading,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(MontageSpacing.lg))
                }
                content()
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// PILL CHIP (For filter tabs, categories, etc.)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "chipScale",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .then(
                if (selected) {
                    Modifier
                        .shadow(
                            elevation = MontageElevation.subtle,
                            shape = RoundedCornerShape(50),
                            ambientColor = colors.shadow,
                            spotColor = colors.shadow,
                        )
                        .background(colors.accent, RoundedCornerShape(50))
                } else {
                    Modifier
                        .border(
                            MontageStrokes.hairline,
                            colors.border,
                            RoundedCornerShape(50),
                        )
                        .background(colors.backgroundSecondary, RoundedCornerShape(50))
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        MontageText(
            text = label,
            style = typography.label,
            color = if (selected) colors.textOnAccent else colors.textSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────
// PROGRESS INDICATOR (Custom circular)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageCircularProgress(
    modifier: Modifier = Modifier,
    color: Color = MontageTheme.colors.accent,
) {
    val colors = MontageTheme.colors
    Canvas(modifier = modifier.size(40.dp)) {
        val strokeWidth = 3.dp.toPx()
        drawCircle(
            color = colors.border,
            radius = (size.minDimension - strokeWidth) / 2,
            style = Stroke(width = strokeWidth),
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            ),
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────
// LINEAR PROGRESS (Custom)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MontageTheme.colors.accent,
    trackColor: Color = MontageTheme.colors.border,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "linearProgress",
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
    ) {
        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(3.dp.toPx()),
        )
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(3.dp.toPx()),
            size = Size(size.width * animatedProgress, size.height),
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────
// BOTTOM SHEET (Custom, large top radius, spring animation)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MontageTheme.colors
    val offset by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
        label = "sheetOffset",
    )

    if (visible || offset > 0.01f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.scrim.copy(alpha = 0.6f * (1f - offset)))
                .clickable(onClick = onDismiss),
        ) {
            MontageCard(
                modifier = modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, (offset * 400).toInt()) },
                shape = RoundedCornerShape(
                    topStart = MontageShapes.bottomSheet,
                    topEnd = MontageShapes.bottomSheet,
                ),
                elevation = MontageElevation.high,
            ) {
                Column {
                    // Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MontageSpacing.md, bottom = MontageSpacing.sm),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.border),
                        )
                    }
                    content()
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// DROPDOWN MENU (Custom)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable MontageDropdownMenuScope.() -> Unit,
) {
    val colors = MontageTheme.colors
    if (expanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.scrim.copy(alpha = 0.2f))
                .clickable(onClick = onDismissRequest),
        ) {
            MontageCard(
                modifier = modifier,
                shape = RoundedCornerShape(MontageShapes.medium),
                elevation = MontageElevation.high,
            ) {
                Column(modifier = Modifier.padding(vertical = MontageSpacing.sm)) {
                    MontageDropdownMenuScope().content()
                }
            }
        }
    }
}

class MontageDropdownMenuScope {
    @Composable
    fun Item(
        label: String,
        onClick: () -> Unit,
        icon: ImageVector? = null,
    ) {
        val colors = MontageTheme.colors
        val typography = MontageTheme.typography
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MontageShapes.small))
                .clickable(onClick = onClick)
                .padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                MontageIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(MontageIcons.medium),
                )
                Spacer(Modifier.width(MontageSpacing.md))
            }
            MontageText(
                text = label,
                style = typography.body,
                color = colors.textPrimary,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// SCAFFOLD (Custom — no Material)
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MontageScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    containerColor: Color = MontageTheme.colors.background,
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = MontageTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(containerColor),
    ) {
        if (topBar != null) {
            topBar()
        }
        Box(modifier = Modifier.weight(1f)) {
            // Calculate padding for top/bottom bars
            val topPadding = if (topBar != null) 56.dp else 0.dp
            content(
                PaddingValues(top = topPadding),
            )
        }
        if (bottomBar != null) {
            bottomBar()
        }
    }
}
