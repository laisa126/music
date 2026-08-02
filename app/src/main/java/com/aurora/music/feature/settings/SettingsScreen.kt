package com.aurora.music.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.BuildConfig
import com.aurora.music.core.designsystem.montage.MontageChip
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageSwitch
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.domain.model.CrossfadeDuration
import com.aurora.music.domain.repository.AccentSource
import com.aurora.music.domain.repository.AnimationLevel
import com.aurora.music.domain.repository.ThemeMode

@Composable
fun SettingsScreen(
    onOpenEqualizer: () -> Unit, onOpenAbout: () -> Unit,
    contentPadding: PaddingValues, modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = contentPadding) {
        item(key = "title") { MontageText(text = "Settings", style = typography.title, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.padding(start = MontageSpacing.screenHorizontal, top = MontageSpacing.lg, bottom = MontageSpacing.sm)) }
        item(key = "appearance") { SettingsCategory("Appearance") }
        item(key = "theme") { ChipRow(label = "Theme", options = ThemeMode.entries.map { it to it.label() }, selected = settings.themeMode, onSelect = viewModel::setThemeMode) }
        item(key = "accent") { ChipRow(label = "Accent colour", options = AccentSource.entries.map { it to it.label() }, selected = settings.accentSource, onSelect = viewModel::setAccentSource) }
        item(key = "animations") { ChipRow(label = "Animations", options = AnimationLevel.entries.map { it to it.label() }, selected = settings.animationLevel, onSelect = viewModel::setAnimationLevel) }
        item(key = "glow") { SwitchRow(title = "Contour Glow", subtitle = "Soft edge lighting on cards and panels", checked = settings.contourGlowEnabled, onCheckedChange = viewModel::setContourGlow) }
        item(key = "blur") { SwitchRow(title = "Blurred player background", checked = settings.blurredPlayerBackground, onCheckedChange = viewModel::setBlurredBackground) }
        item(key = "rotate") { SwitchRow(title = "Rotating artwork", subtitle = "Spin album art while playing", checked = settings.rotatingArtwork, onCheckedChange = viewModel::setRotatingArtwork) }
        item(key = "playbackHeader") { SettingsCategory("Playback") }
        item(key = "gapless") { SwitchRow(title = "Gapless playback", subtitle = "No silence between continuous albums", checked = settings.gaplessPlayback, onCheckedChange = viewModel::setGapless) }
        item(key = "crossfade") { ChipRow(label = "Crossfade", options = CrossfadeDuration.entries.map { it to it.label }, selected = settings.crossfade, onSelect = viewModel::setCrossfade) }
        item(key = "skipSilence") { SwitchRow(title = "Skip silence", checked = settings.skipSilence, onCheckedChange = viewModel::setSkipSilence) }
        item(key = "normalize") { SwitchRow(title = "Volume normalization", checked = settings.volumeNormalization, onCheckedChange = viewModel::setVolumeNormalization) }
        item(key = "rememberQueue") { SwitchRow(title = "Remember queue", subtitle = "Restore the queue after a restart", checked = settings.rememberQueue, onCheckedChange = viewModel::setRememberQueue) }
        item(key = "resume") { SwitchRow(title = "Resume on launch", checked = settings.resumeOnLaunch, onCheckedChange = viewModel::setResumeOnLaunch) }
        item(key = "eq") { NavigationRow(title = "Equalizer", subtitle = "10-band EQ, bass boost, presets", onClick = onOpenEqualizer) }
        item(key = "libraryHeader") { SettingsCategory("Library & storage") }
        item(key = "autoScan") { SwitchRow(title = "Automatic scanning", subtitle = "Re-index in the background every 12 hours", checked = settings.automaticScanning, onCheckedChange = viewModel::setAutomaticScanning) }
        item(key = "rescan") { NavigationRow(title = "Rescan library now", subtitle = "Detect new, moved and deleted files", onClick = viewModel::rescan) }
        item(key = "qualityBadge") { SwitchRow(title = "Show quality badge", checked = settings.showQualityBadge, onCheckedChange = viewModel::setShowQualityBadge) }
        item(key = "showDuration") { SwitchRow(title = "Show duration", checked = settings.showDuration, onCheckedChange = viewModel::setShowDuration) }
        item(key = "notifHeader") { SettingsCategory("Notifications") }
        item(key = "notifArtwork") { SwitchRow(title = "Show artwork", checked = settings.showNotificationArtwork, onCheckedChange = viewModel::setNotificationArtwork) }
        item(key = "notifLike") { SwitchRow(title = "Show like button", checked = settings.showLikeInNotification, onCheckedChange = viewModel::setNotificationLike) }
        item(key = "notifCompact") { SwitchRow(title = "Compact notification", checked = settings.compactNotification, onCheckedChange = viewModel::setCompactNotification) }
        item(key = "privacyHeader") { SettingsCategory("Privacy") }
        item(key = "appLock") { SwitchRow(title = "App lock", subtitle = "Require biometrics or device PIN", checked = settings.appLockEnabled, onCheckedChange = viewModel::setAppLock) }
        item(key = "history") { SwitchRow(title = "Record listening history", subtitle = "Used only on-device for recommendations", checked = settings.historyRecordingEnabled, onCheckedChange = viewModel::setHistoryRecording) }
        item(key = "analytics") { SwitchRow(title = "Local statistics", subtitle = "Never leaves your device. No cloud analytics.", checked = settings.localAnalyticsEnabled, onCheckedChange = viewModel::setAnalytics) }
        item(key = "clearHistory") { NavigationRow(title = "Clear listening history", onClick = viewModel::clearHistory) }
        item(key = "clearSearches") { NavigationRow(title = "Clear search history", onClick = viewModel::clearSearchHistory) }
        item(key = "aboutHeader") { SettingsCategory("About") }
        item(key = "about") { NavigationRow(title = "About Aurora Music", subtitle = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", onClick = onOpenAbout) }
        item(key = "privacyNote") { MontageText(text = "Aurora has no accounts, no login and no cloud sync. Everything stays on this device.", style = typography.mini, color = colors.textSecondary, modifier = Modifier.padding(horizontal = MontageSpacing.screenHorizontal, vertical = MontageSpacing.lg)) }
        item(key = "spacer") { Spacer(Modifier.height(MontageSpacing.xxl)) }
    }
}

@Composable private fun SettingsCategory(title: String) { val colors = MontageTheme.colors; val typography = MontageTheme.typography; Row(modifier = Modifier.padding(start = MontageSpacing.screenHorizontal, top = MontageSpacing.xxl, bottom = MontageSpacing.sm), verticalAlignment = Alignment.CenterVertically) { MontageText(text = title, style = typography.caption, color = colors.accent, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) } }

@Composable
private fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, subtitle: String? = null, icon: ImageVector? = null) {
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = MontageSpacing.screenHorizontal, vertical = MontageSpacing.md), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) { MontageIcon(imageVector = icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(MontageSpacing.md)) }
        Column(Modifier.weight(1f)) { MontageText(title, style = typography.body, color = colors.textPrimary); if (subtitle != null) MontageText(text = subtitle, style = typography.mini, color = colors.textSecondary) }
        MontageSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun NavigationRow(title: String, onClick: () -> Unit, subtitle: String? = null, icon: ImageVector? = null) {
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = MontageSpacing.screenHorizontal, vertical = MontageSpacing.md), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) { MontageIcon(imageVector = icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(MontageSpacing.md)) }
        Column(Modifier.weight(1f)) { MontageText(title, style = typography.body, color = colors.textPrimary); if (subtitle != null) MontageText(text = subtitle, style = typography.mini, color = colors.textSecondary) }
    }
}

@Composable
private fun <T> ChipRow(label: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    Column(modifier = Modifier.padding(vertical = MontageSpacing.sm)) {
        MontageText(text = label, style = typography.body, color = colors.textPrimary, modifier = Modifier.padding(horizontal = MontageSpacing.screenHorizontal, vertical = MontageSpacing.xs))
        LazyRow(contentPadding = PaddingValues(horizontal = MontageSpacing.screenHorizontal), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.sm)) { items(options, key = { it.second }) { (value, text) -> MontageChip(label = text, selected = value == selected, onClick = { onSelect(value) }) } }
    }
}

private fun ThemeMode.label(): String = when (this) { ThemeMode.SYSTEM -> "System"; ThemeMode.LIGHT -> "Light"; ThemeMode.DARK -> "Dark"; ThemeMode.AMOLED -> "AMOLED" }
private fun AccentSource.label(): String = when (this) { AccentSource.DYNAMIC -> "Material You"; AccentSource.ARTWORK -> "From artwork"; AccentSource.CUSTOM -> "Custom" }
private fun AnimationLevel.label(): String = when (this) { AnimationLevel.FULL -> "Full"; AnimationLevel.REDUCED -> "Reduced"; AnimationLevel.NONE -> "None" }
