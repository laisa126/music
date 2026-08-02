package com.aurora.music.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.contourGlow
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageLinearProgress
import com.aurora.music.core.designsystem.montage.MontagePrimaryButton
import com.aurora.music.core.designsystem.montage.MontageSecondaryButton
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.domain.repository.ScanState

/**
 * First-launch flow: Welcome -> permission rationale -> folder choice ->
 * first-scan progress -> Home (spec Section 11 item 1 / Section 12).
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var step by remember { mutableIntStateOf(0) }
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        viewModel.refreshPermission()
        // Advance regardless: the app must still work with reduced functionality.
        step = 2
    }

    // Kick off the first scan when we reach the progress step with permission.
    LaunchedEffect(step, hasPermission) {
        if (step == 3 && hasPermission) viewModel.startScan()
    }

    LaunchedEffect(scanState) {
        if (step == 3 && scanState is ScanState.Complete) {
            viewModel.completeOnboarding()
            onFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboardingStep",
        ) { current ->
            when (current) {
                0 -> OnboardingPage(
                    icon = Icons.Rounded.GraphicEq,
                    title = "Welcome to Aurora",
                    message = "A premium, private music player for the music already on your " +
                        "device. No accounts. No tracking. Ever.",
                    primaryLabel = "Get started",
                    onPrimary = { step = 1 },
                )

                1 -> OnboardingPage(
                    icon = Icons.Rounded.Lock,
                    title = "Access your music",
                    message = "Aurora needs the audio permission to find music on this device. " +
                        "Notifications let you control playback from the lock screen. " +
                        "Nothing is ever uploaded.",
                    primaryLabel = "Continue",
                    onPrimary = { permissionLauncher.launch(requiredPermissions()) },
                    secondaryLabel = "Not now",
                    onSecondary = { step = 2 },
                )

                2 -> OnboardingPage(
                    icon = Icons.Rounded.Folder,
                    title = "Choose your folders",
                    message = if (hasPermission) {
                        "Aurora will scan your whole device. You can exclude folders later " +
                            "in Settings."
                    } else {
                        "Permission was declined, so Aurora can't read your library yet. " +
                            "You can grant it any time from Settings."
                    },
                    primaryLabel = if (hasPermission) "Scan my device" else "Continue anyway",
                    onPrimary = {
                        if (hasPermission) {
                            step = 3
                        } else {
                            viewModel.completeOnboarding()
                            onFinished()
                        }
                    },
                )

                else -> ScanningPage(scanState)
            }
        }

        if (step < 3) {
            StepIndicator(
                current = step,
                total = 3,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = MontageSpacing.xl),
            )
        }
    }
}

private fun requiredPermissions(): Array<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.READ_MEDIA_AUDIO)
        add(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        @Suppress("DEPRECATION")
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}.toTypedArray()

@Composable
private fun OnboardingPage(
    icon: ImageVector,
    title: String,
    message: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MontageSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .glassSurface(shape = CircleShape)
                .contourGlow(shape = CircleShape, intensity = 1.2f),
            contentAlignment = Alignment.Center,
        ) {
            MontageIcon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(MontageIcons.hero),
            )
        }
        Spacer(Modifier.height(MontageSpacing.xxl))
        MontageText(
            text = title,
            style = typography.title,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MontageSpacing.md))
        MontageText(
            text = message,
            style = typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MontageSpacing.xxxl))
        MontagePrimaryButton(
            onClick = onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            MontageText(
                text = primaryLabel,
                style = typography.label,
                color = colors.textOnAccent,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (secondaryLabel != null && onSecondary != null) {
            Spacer(Modifier.height(MontageSpacing.xs))
            MontageSecondaryButton(
                onClick = onSecondary,
            ) {
                MontageText(
                    text = secondaryLabel,
                    style = typography.label,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ScanningPage(scanState: ScanState) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MontageSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MontageIcon(
            imageVector = Icons.Rounded.LibraryMusic,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(MontageSpacing.xl))
        MontageText(
            text = "Building your library",
            style = typography.heading,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MontageSpacing.xl))

        when (val state = scanState) {
            is ScanState.Scanning -> {
                if (state.total > 0) {
                    MontageLinearProgress(
                        progress = state.progress,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(MontageSpacing.sm))
                    MontageText(
                        text = "${state.scanned} of ${state.total} tracks",
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                } else {
                    MontageLinearProgress(
                        progress = 0.1f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            is ScanState.Failed -> MontageText(
                text = state.message,
                style = typography.body,
                color = colors.error,
                textAlign = TextAlign.Center,
            )

            else -> MontageLinearProgress(
                progress = 0.1f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = MontageTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MontageSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val active = index == current
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (active) 22.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            colors.accent
                        } else {
                            colors.textTertiary.copy(alpha = 0.3f)
                        },
                    ),
            )
        }
    }
}
