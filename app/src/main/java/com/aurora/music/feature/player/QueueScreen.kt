package com.aurora.music.feature.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.common.formatDurationLong
import com.aurora.music.core.common.formatTrackCount
import com.aurora.music.core.designsystem.components.AuroraEmptyState
import com.aurora.music.core.designsystem.components.SongRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Queue") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Rounded.SaveAlt, contentDescription = "Save queue as playlist")
                    }
                    TextButton(onClick = viewModel::clearQueue) { Text("Clear") }
                },
            )
        },
    ) { padding ->
        if (state.queue.isEmpty()) {
            AuroraEmptyState(
                icon = Icons.Rounded.Close,
                title = "Nothing queued",
                message = "Play something to build a queue.",
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        SongRow(
            item = item,
            isPlaying = isPlaying,
            onClick = onPlay,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onMoveUp) {
            Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move up")
        }
        IconButton(onClick = onMoveDown) {
            Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move down")
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Rounded.Close, contentDescription = "Remove from queue")
        }
    }
}

@Composable
private fun QueueStats(trackCount: Int, totalDurationMs: Long, remaining: Int) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = "${formatTrackCount(trackCount)} · ${formatDurationLong(totalDurationMs)}",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "$remaining up next",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SaveQueueDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("My queue") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save queue as playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
