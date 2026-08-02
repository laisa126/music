package com.aurora.music.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.core.designsystem.montage.MontageCircularProgress
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageLinearProgress
import com.aurora.music.core.designsystem.montage.MontagePrimaryButton
import com.aurora.music.core.designsystem.montage.MontageSecondaryButton
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons

@Composable
fun AuroraEmptyState(
    icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier,
    primaryAction: (() -> Unit)? = null, primaryActionLabel: String? = null,
    secondaryAction: (() -> Unit)? = null, secondaryActionLabel: String? = null,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = MontageSpacing.xxl, vertical = MontageSpacing.xxxl), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(88.dp).clip(CircleShape).glassSurface(shape = CircleShape), contentAlignment = Alignment.Center) {
            MontageIcon(imageVector = icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(MontageIcons.hero - 10.dp))
        }
        Spacer(Modifier.height(MontageSpacing.lg))
        MontageText(text = title, style = typography.heading, color = colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(MontageSpacing.sm))
        MontageText(text = message, style = typography.caption, color = colors.textSecondary, textAlign = TextAlign.Center)
        if (primaryAction != null && primaryActionLabel != null) {
            Spacer(Modifier.height(MontageSpacing.xxl))
            MontagePrimaryButton(onClick = primaryAction) { MontageText(text = primaryActionLabel, style = typography.label, color = colors.textOnAccent, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) }
        }
        if (secondaryAction != null && secondaryActionLabel != null) {
            Spacer(Modifier.height(MontageSpacing.xs))
            MontageSecondaryButton(onClick = secondaryAction) { MontageText(text = secondaryActionLabel, style = typography.label, color = colors.textSecondary) }
        }
    }
}

@Composable fun EmptyLibraryState(onScan: () -> Unit, modifier: Modifier = Modifier) = AuroraEmptyState(icon = Icons.Rounded.LibraryMusic, title = "Let's find your music", message = "No music found yet. Scan your device to build your library.", primaryAction = onScan, primaryActionLabel = "Scan device", modifier = modifier)

@Composable fun PermissionDeniedState(onRequest: () -> Unit, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) = AuroraEmptyState(icon = Icons.Rounded.Lock, title = "Music access is required", message = "Aurora needs permission to read audio files on this device.", primaryAction = onRequest, primaryActionLabel = "Grant permission", secondaryAction = onOpenSettings, secondaryActionLabel = "Open app settings", modifier = modifier)

@Composable fun NoResultsState(query: String, modifier: Modifier = Modifier) = AuroraEmptyState(icon = Icons.Rounded.SearchOff, title = "No results for \"$query\"", message = "Try a different spelling or a shorter search term.", modifier = modifier)

@Composable fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) = AuroraEmptyState(icon = Icons.Rounded.ErrorOutline, title = "Something went wrong", message = message, primaryAction = onRetry, primaryActionLabel = onRetry?.let { "Retry" }, modifier = modifier)

@Composable fun LoadingState(modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues(0.dp)) {
    Box(modifier = modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) { MontageCircularProgress() }
}

@Composable
fun ScanProgressState(scanned: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = MontageSpacing.xxl, vertical = MontageSpacing.xxl), horizontalAlignment = Alignment.CenterHorizontally) {
        MontageText(text = "Building your library", style = typography.body, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Spacer(Modifier.height(MontageSpacing.md))
        if (total > 0) {
            MontageLinearProgress(progress = scanned.toFloat() / total, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(MontageSpacing.sm))
            MontageText(text = "$scanned of $total tracks", style = typography.mini, color = colors.textSecondary)
        } else {
            MontageLinearProgress(progress = 0.3f, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(MontageSpacing.sm))
            MontageText(text = "Scanning…", style = typography.mini, color = colors.textSecondary)
        }
    }
}
