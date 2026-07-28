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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.BuildConfig
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.domain.model.CrossfadeDuration
import com.aurora.music.domain.repository.AccentSource
import com.aurora.music.domain.repository.AnimationLevel
import com.aurora.music.domain.repository.ThemeMode

@Composable
fun SettingsScreen(
    onOpenEqualizer: () -> Unit,
    onOpenAbout: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item(key = "title") {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
            )
        }

        // ---- Appearance ---------------------------------------------------
        item(key = "appearance") { SettingsCategory("Appearance", Icons.Rounded.Palette) }

        item(key = "theme") {
            ChipRow(
                label = "Theme",
                options = ThemeMode.entries.map { it to it.label() },
                selected = settings.themeMode,
                onSelect = viewModel::setThemeMode,
            )
        }
        item(key = "accent") {
            ChipRow(
                label = "Accent colour",
                options = AccentSource.entries.map { it to it.label() },
                selected = settings.accentSource,
                onSelect = viewModel::setAccentSource,
            )
        }
        item(key = "animations") {
            ChipRow(
                label = "Animations",
                options = AnimationLevel.entries.map { it to it.label() },
                selected = settings.animationLevel,
                onSelect = viewModel::setAnimationLevel,
            )
        }
        item(key = "glow") {
            SwitchRow(
                title = "Contour Glow",
                subtitle = "Soft edge lighting on cards and panels",
                checked = settings.contourGlowEnabled,
                onCheckedChange = viewModel::setContourGlow,
                icon = Icons.Rounded.AutoAwesome,
            )
        }
        item(key = "blur") {
            SwitchRow(
                title = "Blurred player background",
                checked = settings.blurredPlayerBackground,
                onCheckedChange = viewModel::setBlurredBackground,
                icon = Icons.Rounded.Animation,
            )
        }
        item(key = "rotate") {
            SwitchRow(
                title = "Rotating artwork",
                subtitle = "Spin album art while playing",
                checked = settings.rotatingArtwork,
                onCheckedChange = viewModel::setRotatingArtwork,
                icon = Icons.Rounded.Animation,
            )
        }

        // ---- Playback -----------------------------------------------------
        item(key = "playbackHeader") { SettingsCategory("Playback", Icons.Rounded.PlayCircle) }

        item(key = "gapless") {
            SwitchRow(
                title = "Gapless playback",
                subtitle = "No silence between continuous albums",
                checked = settings.gaplessPlayback,
                onCheckedChange = viewModel::setGapless,
            )
        }
        item(key = "crossfade") {
            ChipRow(
                label = "Crossfade",
                options = CrossfadeDuration.entries.map { it to it.label },
                selected = settings.crossfade,
                onSelect = viewModel::setCrossfade,
            )
        }
        item(key = "skipSilence") {
            SwitchRow(
                title = "Skip silence",
                checked = settings.skipSilence,
                onCheckedChange = viewModel::setSkipSilence,
            )
        }
        item(key = "normalize") {
            SwitchRow(
                title = "Volume normalization",
                checked = settings.volumeNormalization,
                onCheckedChange = viewModel::setVolumeNormalization,
            )
        }
        item(key = "rememberQueue") {
            SwitchRow(
                title = "Remember queue",
                subtitle = "Restore the queue after a restart",
                checked = settings.rememberQueue,
                onCheckedChange = viewModel::setRememberQueue,
            )
        }
        item(key = "resume") {
            SwitchRow(
                title = "Resume on launch",
                checked = settings.resumeOnLaunch,
                onCheckedChange = viewModel::setResumeOnLaunch,
            )
        }
        item(key = "eq") {
            NavigationRow(
                title = "Equalizer",
                subtitle = "10-band EQ, bass boost, presets",
                icon = Icons.Rounded.Equalizer,
                onClick = onOpenEqualizer,
            )
        }

        // ---- Library ------------------------------------------------------
        item(key = "libraryHeader") { SettingsCategory("Library & storage", Icons.Rounded.Storage) }

        item(key = "autoScan") {
            SwitchRow(
                title = "Automatic scanning",
                subtitle = "Re-index in the background every 12 hours",
                checked = settings.automaticScanning,
                onCheckedChange = viewModel::setAutomaticScanning,
            )
        }
        item(key = "rescan") {
            NavigationRow(
                title = "Rescan library now",
                subtitle = "Detect new, moved and deleted files",
                icon = Icons.Rounded.Refresh,
                onClick = viewModel::rescan,
            )
        }
        item(key = "qualityBadge") {
            SwitchRow(
                title = "Show quality badge",
                checked = settings.showQualityBadge,
                onCheckedChange = viewModel::setShowQualityBadge,
            )
        }
        item(key = "showDuration") {
            SwitchRow(
                title = "Show duration",
                checked = settings.showDuration,
                onCheckedChange = viewModel::setShowDuration,
            )
        }

        // ---- Notifications ------------------------------------------------
        item(key = "notifHeader") {
            SettingsCategory("Notifications", Icons.Rounded.Notifications)
        }
        item(key = "notifArtwork") {
            SwitchRow(
                title = "Show artwork",
                checked = settings.showNotificationArtwork,
                onCheckedChange = viewModel::setNotificationArtwork,
            )
        }
        item(key = "notifLike") {
            SwitchRow(
                title = "Show like button",
                checked = settings.showLikeInNotification,
                onCheckedChange = viewModel::setNotificationLike,
            )
        }
        item(key = "notifCompact") {
            SwitchRow(
                title = "Compact notification",
                checked = settings.compactNotification,
                onCheckedChange = viewModel::setCompactNotification,
            )
        }

        // ---- Privacy ------------------------------------------------------
        item(key = "privacyHeader") { SettingsCategory("Privacy", Icons.Rounded.Lock) }

        item(key = "appLock") {
            SwitchRow(
                title = "App lock",
                subtitle = "Require biometrics or device PIN to open Aurora",
                checked = settings.appLockEnabled,
                onCheckedChange = viewModel::setAppLock,
            )
        }
        item(key = "history") {
            SwitchRow(
                title = "Record listening history",
                subtitle = "Used only on-device for recommendations",
                checked = settings.historyRecordingEnabled,
                onCheckedChange = viewModel::setHistoryRecording,
            )
        }
        item(key = "analytics") {
            SwitchRow(
                title = "Local statistics",
                subtitle = "Never leaves your device. No cloud analytics.",
                checked = settings.localAnalyticsEnabled,
                onCheckedChange = viewModel::setAnalytics,
            )
        }
        item(key = "clearHistory") {
            NavigationRow(
                title = "Clear listening history",
                onClick = viewModel::clearHistory,
            )
        }
        item(key = "clearSearches") {
            NavigationRow(
                title = "Clear search history",
                onClick = viewModel::clearSearchHistory,
            )
        }

        // ---- About --------------------------------------------------------
        item(key = "aboutHeader") { SettingsCategory("About", Icons.Rounded.Info) }
        item(key = "about") {
            NavigationRow(
                title = "About Aurora Music",
                subtitle = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                icon = Icons.Rounded.Info,
                onClick = onOpenAbout,
            )
        }
        item(key = "privacyNote") {
            Text(
                text = "Aurora has no accounts, no login and no cloud sync. Everything — " +
                    "playlists, favourites, history and settings — stays on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
        item(key = "spacer") { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SettingsCategory(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun NavigationRow(
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun <T> ChipRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(options, key = { it.second }) { (value, text) ->
                val isSelected = value == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .then(
                            if (isSelected) {
                                Modifier.background(MaterialTheme.colorScheme.primary)
                            } else {
                                Modifier.glassSurface(shape = RoundedCornerShape(50), alpha = 0.5f)
                            },
                        )
                        .clickable { onSelect(value) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.AMOLED -> "AMOLED"
}

private fun AccentSource.label(): String = when (this) {
    AccentSource.DYNAMIC -> "Material You"
    AccentSource.ARTWORK -> "From artwork"
    AccentSource.CUSTOM -> "Custom"
}

private fun AnimationLevel.label(): String = when (this) {
    AnimationLevel.FULL -> "Full"
    AnimationLevel.REDUCED -> "Reduced"
    AnimationLevel.NONE -> "None"
}
