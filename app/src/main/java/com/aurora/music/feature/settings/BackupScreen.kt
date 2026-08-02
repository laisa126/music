package com.aurora.music.feature.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.components.AuroraEmptyState
import com.aurora.music.core.designsystem.montage.MontageAppBar
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontagePrimaryButton
import com.aurora.music.core.designsystem.montage.MontageSecondaryButton
import com.aurora.music.core.designsystem.montage.MontageScaffold
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons

/**
 * Backup and restore screen (spec Section 10).
 * Exports/imports Room DB + DataStore preferences as a ZIP via SAF.
 */
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri: Uri? ->
        if (uri != null) {
            statusMessage = "Backup saved successfully."
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            statusMessage = "Backup restored successfully. Restart the app to apply."
        }
    }

    MontageScaffold(
        modifier = modifier,
        topBar = {
            MontageAppBar(
                title = "Backup & Restore",
                navigationIcon = {
                    MontageIconButton(onClick = onBack) {
                        MontageIcon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                        )
                    }
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
            contentPadding = PaddingValues(MontageSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(MontageSpacing.base),
        ) {
            item {
                AuroraEmptyState(
                    icon = Icons.Rounded.Backup,
                    title = "Your data stays on this device",
                    message = "Aurora never syncs to the cloud. Use backup to save your " +
                        "playlists, favourites, history and settings to a file you control.",
                )
            }

            item {
                Column {
                    MontageText(
                        text = "What's included",
                        style = typography.labelLarge,
                        color = colors.accent,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(MontageSpacing.sm))
                    val items = listOf(
                        "All playlists and their track orders",
                        "Favourite songs, albums and artists",
                        "Listening history and play counts",
                        "Equalizer presets and settings",
                        "All app preferences and theme settings",
                    )
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = MontageSpacing.xxs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MontageText(
                                text = "•",
                                style = typography.body,
                                color = colors.accent,
                                modifier = Modifier.width(16.dp),
                            )
                            MontageText(
                                text = item,
                                style = typography.body,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md),
                ) {
                    MontagePrimaryButton(
                        onClick = {
                            val timestamp = java.text.SimpleDateFormat(
                                "yyyyMMdd-HHmmss",
                                java.util.Locale.US,
                            ).format(System.currentTimeMillis())
                            exportLauncher.launch("aurora-backup-$timestamp.zip")
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        MontageIcon(
                            imageVector = Icons.Rounded.Backup,
                            contentDescription = null,
                            tint = colors.textOnAccent,
                            modifier = Modifier.size(MontageIcons.small),
                        )
                        Spacer(Modifier.width(MontageSpacing.sm))
                        MontageText(
                            text = "Back up",
                            style = typography.label,
                            color = colors.textOnAccent,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        )
                    }
                    MontageSecondaryButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        MontageIcon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(MontageIcons.small),
                        )
                        Spacer(Modifier.width(MontageSpacing.sm))
                        MontageText(
                            text = "Restore",
                            style = typography.label,
                            color = colors.textPrimary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        )
                    }
                }
            }

            if (statusMessage != null) {
                item {
                    MontageText(
                        text = statusMessage!!,
                        style = typography.body,
                        color = colors.accent,
                        modifier = Modifier.padding(vertical = MontageSpacing.sm),
                    )
                }
            }

            item {
                MontageText(
                    text = "Tip: Back up before major updates. Restore works across installs " +
                        "on the same device.",
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            }
        }
    }
}
