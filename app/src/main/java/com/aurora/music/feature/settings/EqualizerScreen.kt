package com.aurora.music.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.domain.model.BuiltInPresets
import com.aurora.music.domain.model.EQ_BANDS_HZ
import com.aurora.music.domain.model.formatBandLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val eq by viewModel.equalizer.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Equalizer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = eq.enabled,
                        onCheckedChange = viewModel::setEqualizerEnabled,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item(key = "presets") {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(BuiltInPresets.all, key = { it.name }) { preset ->
                        val selected = preset.name == eq.presetName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .then(
                                    if (selected) {
                                        Modifier.background(MaterialTheme.colorScheme.primary)
                                    } else {
                                        Modifier.glassSurface(
                                            shape = RoundedCornerShape(50),
                                            alpha = 0.5f,
                                        )
                                    },
                                )
                                .clickable { viewModel.applyPreset(preset) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }

            item(key = "bands") {
                Text(
                    text = "10-band equalizer",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 8.dp),
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
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
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
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Limiter", modifier = Modifier.weight(1f))
                        Switch(checked = eq.limiterEnabled, onCheckedChange = viewModel::setLimiter)
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(32.dp),
    ) {
        Text(
            text = "${gain.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .width(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Slider(
                value = gain,
                onValueChange = onGainChange,
                valueRange = -12f..12f,
                enabled = enabled,
                modifier = Modifier
                    .graphicsLayer { rotationZ = 270f }
                    .layout { measurable, constraints ->
                        // Rotate the slider into a vertical fader.
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
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(
                text = String.format(java.util.Locale.US, "%.1f", value),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
        )
    }
}
