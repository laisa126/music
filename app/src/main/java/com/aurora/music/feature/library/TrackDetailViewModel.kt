package com.aurora.music.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the FileInfo and MetadataEditor screens.
 * Loads a single track by its ID from the saved state handle.
 */
@HiltViewModel
class TrackDetailViewModel @Inject constructor(
    private val repository: MusicRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mediaId: String? = savedStateHandle.get<String>("mediaId")

    val track: StateFlow<MediaItem?> = repository.observeSong(mediaId ?: "")
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun updateMetadata(item: MediaItem) {
        viewModelScope.launch { repository.updateMetadata(item) }
    }

    fun toggleFavourite(item: MediaItem) {
        viewModelScope.launch { repository.setFavourite(item.id, !item.isFavourite) }
    }
}
