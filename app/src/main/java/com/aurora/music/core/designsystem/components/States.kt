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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aurora.music.core.designsystem.glassSurface

/**
 * Every feature needs loading / empty / error states with a retry path
 * (spec Section 10). These are the shared implementations.
 */
@Composable
fun AuroraEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    primaryAction: (() -> Unit)? = null,
    primaryActionLabel: String? = null,
    secondaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .glassSurface(shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (primaryAction != null && primaryActionLabel != null) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = primaryAction) { Text(primaryActionLabel) }
        }
        if (secondaryAction != null && secondaryActionLabel != null) {
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = secondaryAction) { Text(secondaryActionLabel) }
        }
    }
}

@Composable
fun EmptyLibraryState(onScan: () -> Unit, modifier: Modifier = Modifier) {
    AuroraEmptyState(
        icon = Icons.Rounded.LibraryMusic,
        title = "Let's find your music",
        message = "No music found yet. Scan your device to build your library.",
        primaryAction = onScan,
        primaryActionLabel = "Scan device",
        modifier = modifier,
    )
}

/** Distinct from the empty-library state — this one is recoverable via Settings. */
@Composable
fun PermissionDeniedState(
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuroraEmptyState(
        icon = Icons.Rounded.Lock,
        title = "Music access is required",
        message = "Aurora needs permission to read audio files on this device to show your library.",
        primaryAction = onRequest,
        primaryActionLabel = "Grant permission",
        secondaryAction = onOpenSettings,
        secondaryActionLabel = "Open app settings",
        modifier = modifier,
    )
}

@Composable
fun NoResultsState(query: String, modifier: Modifier = Modifier) {
    AuroraEmptyState(
        icon = Icons.Rounded.SearchOff,
        title = "No results for \"$query\"",
        message = "Try a different spelling or a shorter search term.",
        modifier = modifier,
    )
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    AuroraEmptyState(
        icon = Icons.Rounded.ErrorOutline,
        title = "Something went wrong",
        message = message,
        primaryAction = onRetry,
        primaryActionLabel = onRetry?.let { "Retry" },
        modifier = modifier,
    )
}

@Composable
fun LoadingState(modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues(0.dp)) {
    Box(
        modifier = modifier.fillMaxSize().padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/** Real progress for first-scan of a large library, not just a spinner. */
@Composable
fun ScanProgressState(
    scanned: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Building your library",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(12.dp))
        if (total > 0) {
            LinearProgressIndicator(
                progress = { scanned.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$scanned of $total tracks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Scanning…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
