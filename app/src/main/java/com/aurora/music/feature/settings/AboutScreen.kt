package com.aurora.music.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aurora.music.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
        ) {
            item {
                Column {
                    Text("Aurora Music", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME} " +
                            "(build ${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(24.dp))

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
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
