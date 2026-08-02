package com.aurora.music.feature.player

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.common.formatDurationLong
import com.aurora.music.core.common.formatTrackCount
import com.aurora.music.core.designsystem.components.AuroraEmptyState
import com.aurora.music.core.designsystem.components.SongRow
import com.aurora.music.core.designsystem.montage.MontageAppBar
import com.aurora.music.core.designsystem.montage.MontageDialog
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontagePrimaryButton
import com.aurora.music.core.designsystem.montage.MontageSecondaryButton
import com.aurora.music.core.designsystem.montage.MontageScaffold
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTextField
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons

@Composable
fun QueueScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSaveDialog by remember { mutableStateOf(false) }
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    MontageScaffold(
        modifier = modifier,
        topBar = {
            MontageAppBar(
                title = "Queue",
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
                    MontageIconButton(onClick = { showSaveDialog = true }) {
                        MontageIcon(
                            imageVector = Icons.Rounded.SaveAlt,
                            contentDescription = "Save queue as playlist",
                            tint = colors.textPrimary,
                        )
                    }
                    MontageSecondaryButton(
                        onClick = viewModel::clearQueue,
                    ) {
                        MontageText(
                            text = "Clear",
                            style = typography.label,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                },
            )
        },
        containerColor = colors.background,
    ) { padding ->
        if (state.queue.isEmpty()) {
            AuroraEmptyState(
                icon = Icons.Rounded.Close,
                title = "Nothing queued",
                message = "Play something to build a queue.",
                modifier = Modifier.padding(top = padding.calculateTopPadding()),
            )
            return@MontageScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = MontageSpacing.xl),
        ) {
            item(key = "stats") {
                QueueStats(
                    trackCount = state.queue.size,
                    totalDurationMs = state.queue.sumOf { it.durationMs },
                    remaining = state.upNext.size,
                )
            }

            itemsIndexed(
                items = state.queue,
                key = { index, item -> "${item.id}_$index" },
            ) { index, item ->
                QueueRow(
                    item = item,
                    isPlaying = index == state.queueIndex,
                    onPlay = { viewModel.seekToQueueIndex(index) },
                    onRemove = { viewModel.removeFromQueue(index) },
                    onMoveUp = {
                        viewModel.moveQueueItem(index, (index - 1).coerceAtLeast(0))
                    },
                    onMoveDown = {
                        viewModel.moveQueueItem(
                            index,
                            (index + 1).coerceAtMost(state.queue.lastIndex),
                        )
                    },
                )
            }
        }
    }

    if (showSaveDialog) {
        SaveQueueDialog(
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                viewModel.saveQueueAsPlaylist(name)
                showSaveDialog = false
            },
        )
    }
}

@Composable
private fun QueueRow(
    item: com.aurora.music.domain.model.MediaItem,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val colors = MontageTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        SongRow(
            item = item,
            isPlaying = isPlaying,
            onClick = onPlay,
            modifier = Modifier.weight(1f),
        )
        MontageIconButton(onClick = onMoveUp) {
            MontageIcon(
                imageVector = Icons.Rounded.ArrowUpward,
                contentDescription = "Move up",
                tint = colors.textTertiary,
                modifier = Modifier.size(MontageIcons.medium),
            )
        }
        MontageIconButton(onClick = onMoveDown) {
            MontageIcon(
                imageVector = Icons.Rounded.ArrowDownward,
                contentDescription = "Move down",
                tint = colors.textTertiary,
                modifier = Modifier.size(MontageIcons.medium),
            )
        }
        MontageIconButton(onClick = onRemove) {
            MontageIcon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove from queue",
                tint = colors.textTertiary,
                modifier = Modifier.size(MontageIcons.medium),
            )
        }
    }
}

@Composable
private fun QueueStats(trackCount: Int, totalDurationMs: Long, remaining: Int) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(
        modifier = Modifier.padding(
            horizontal = MontageSpacing.screenHorizontal,
            vertical = MontageSpacing.md,
        ),
    ) {
        MontageText(
            text = "${formatTrackCount(trackCount)} · ${formatDurationLong(totalDurationMs)}",
            style = typography.labelLarge,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        MontageText(
            text = "$remaining up next",
            style = typography.caption,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun SaveQueueDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("My queue") }
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    MontageDialog(
        onDismissRequest = onDismiss,
        title = "Save queue as playlist",
    ) {
        MontageTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Playlist name",
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(MontageSpacing.xl))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md),
        ) {
            MontageSecondaryButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            ) {
                MontageText(
                    text = "Cancel",
                    style = typography.label,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }
            MontagePrimaryButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                modifier = Modifier.weight(1f),
            ) {
                MontageText(
                    text = "Save",
                    style = typography.label,
                    color = colors.textOnAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
