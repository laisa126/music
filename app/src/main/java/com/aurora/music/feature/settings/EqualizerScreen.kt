package com.aurora.music.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.core.designsystem.montage.MontageAppBar
import com.aurora.music.core.designsystem.montage.MontageChip
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageScaffold
import com.aurora.music.core.designsystem.montage.MontageSlider
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageSwitch
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.domain.model.BuiltInPresets
import com.aurora.music.domain.model.EQ_BANDS_HZ
import com.aurora.music.domain.model.formatBandLabel

@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val eq by viewModel.equalizer.collectAsStateWithLifecycle()
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    MontageScaffold(
        modifier = modifier,
        topBar = {
            MontageAppBar(
                title = "Equalizer",
                navigationIcon = {
                    MontageIconButton(onClick = onBack) {
                        MontageIcon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                        )
                    }
                },
                actions = {
                    MontageSwitch(
                        checked = eq.enabled,
                        onCheckedChange = viewModel::setEqualizerEnabled,
                        modifier = Modifier.padding(end = MontageSpacing.md),
                    )
                },
            )
        },
        containerColor = colors.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = MontageSpacing.xxxl),
        ) {
            item(key = "presets") {
                MontageText(
                    text = "Presets",
                    style = typography.labelLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        start = MontageSpacing.screenHorizontal,
                        top = MontageSpacing.md,
                        bottom = MontageSpacing.sm,
                    ),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = MontageSpacing.screenHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(MontageSpacing.sm),
                ) {
                    items(BuiltInPresets.all, key = { it.name }) { preset ->
                        MontageChip(
                            label = preset.name,
                            selected = preset.name == eq.presetName,
                            onClick = { viewModel.applyPreset(preset) },
                        )
                    }
                }
            }

            item(key = "bands") {
                MontageText(
                    text = "10-band equalizer",
                    style = typography.labelLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        start = MontageSpacing.screenHorizontal,
                        top = MontageSpacing.xxl,
                        bottom = MontageSpacing.md,
                    ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = MontageSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    EQ_BANDS_HZ.forEachIndexed { index, hz ->
                        BandSlider(
                            label = formatBandLabel(hz),
                            gain = eq.gains.getOrElse(index) { 0f },
                            enabled = eq.enabled,
                            onGainChange = { viewModel.setBandGain(index, it) },
                        )
                    }
                }
            }

            item(key = "effects") {
                Column(
                    modifier = Modifier.padding(
                        horizontal = MontageSpacing.screenHorizontal,
                        vertical = MontageSpacing.md,
                    ),
                ) {
                    EffectSlider(
                        "Bass boost",
                        eq.bassBoost,
                        0f..1f,
                        eq.enabled,
                        viewModel::setBassBoost,
                    )
                    EffectSlider(
                        "Treble boost",
                        eq.trebleBoost,
                        0f..1f,
                        eq.enabled,
                        viewModel::setTrebleBoost,
                    )
                    EffectSlider(
                        "Virtualizer",
                        eq.virtualizer,
                        0f..1f,
                        eq.enabled,
                        viewModel::setVirtualizer,
                    )
                    EffectSlider(
                        "Balance",
                        eq.balance,
                        -1f..1f,
                        eq.enabled,
                        viewModel::setBalance,
                    )
                    EffectSlider(
                        "Preamp",
                        eq.preampDb,
                        -12f..12f,
                        eq.enabled,
                        viewModel::setPreamp,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MontageSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MontageText(
                            text = "Limiter",
                            style = typography.body,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        MontageSwitch(
                            checked = eq.limiterEnabled,
                            onCheckedChange = viewModel::setLimiter,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BandSlider(
    label: String,
    gain: Float,
    enabled: Boolean,
    onGainChange: (Float) -> Unit,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(32.dp)
            .fillMaxHeight(),
    ) {
        MontageText(
            text = "${gain.toInt()}",
            style = typography.mini,
            color = colors.textTertiary,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .width(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            MontageSlider(
                value = gain,
                onValueChange = onGainChange,
                valueRange = -12f..12f,
                enabled = enabled,
                modifier = Modifier
                    .graphicsLayer { rotationZ = 270f }
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            constraints.copy(
                                minWidth = constraints.minHeight,
                                maxWidth = constraints.maxHeight,
                                minHeight = constraints.minWidth,
                                maxHeight = constraints.maxWidth,
                            ),
                        )
                        layout(placeable.height, placeable.width) {
                            placeable.place(
                                -(placeable.width / 2 - placeable.height / 2),
                                -(placeable.height / 2 - placeable.width / 2),
                            )
                        }
                    },
            )
        }
        Spacer(Modifier.height(MontageSpacing.xxs))
        MontageText(
            text = label,
            style = typography.mini,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun EffectSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(modifier = Modifier.padding(vertical = MontageSpacing.xs)) {
        Row {
            MontageText(
                text = label,
                style = typography.body,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            MontageText(
                text = String.format(java.util.Locale.US, "%.1f", value),
                style = typography.label,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
            )
        }
        MontageSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
        )
    }
}
