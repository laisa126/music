package com.aurora.music.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.aurora.music.BuildConfig
import com.aurora.music.core.designsystem.montage.MontageAppBar
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageScaffold
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography

@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    MontageScaffold(
        modifier = modifier,
        topBar = {
            MontageAppBar(
                title = "About",
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
        ) {
            item {
                Column {
                    MontageText(
                        text = "Aurora Music",
                        style = typography.title,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    MontageText(
                        text = "Version ${BuildConfig.VERSION_NAME} " +
                            "(build ${BuildConfig.VERSION_CODE})",
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(MontageSpacing.xxl))

                    Section(
                        "Privacy",
                        "Aurora has no user accounts, no login and no cloud sync. Your " +
                            "playlists, favourites, listening history and settings are stored " +
                            "only on this device. Nothing is uploaded and nothing is tracked.",
                    )

                    Section(
                        "Permissions",
                        "• Audio access — to find and play music on this device.\n" +
                            "• Notifications — to show playback controls.\n" +
                            "• Images — only if you pick custom artwork.\n" +
                            "Aurora never requests contacts, location, camera or microphone " +
                            "beyond optional voice search.",
                    )

                    Section(
                        "Supported formats",
                        "MP3, AAC, M4A, FLAC, ALAC, WAV, AIFF, OGG and Opus. Unsupported " +
                            "files show a friendly message instead of crashing.",
                    )

                    Section(
                        "Open-source licenses",
                        "Built with Jetpack Compose, AndroidX Media3 (ExoPlayer), Room, " +
                            "Hilt, Coil, WorkManager, Retrofit and OkHttp — all under the " +
                            "Apache License 2.0.",
                    )

                    Section(
                        "What's new",
                        "1.0.0 — First release. Local library scanning, full player with " +
                            "queue, gapless playback, 10-band equalizer, playlists, " +
                            "favourites, lyrics and a ColorOS-inspired Contour Glow theme.",
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(modifier = Modifier.padding(bottom = MontageSpacing.xxl)) {
        MontageText(
            text = title,
            style = typography.labelLarge,
            color = colors.accent,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(MontageSpacing.sm))
        MontageText(
            text = body,
            style = typography.body,
            color = colors.textSecondary,
        )
    }
}
