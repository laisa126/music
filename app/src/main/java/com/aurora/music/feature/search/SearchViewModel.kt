package com.aurora.music.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.music.domain.repository.MusicRepository
import com.aurora.music.domain.repository.SearchResults
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchFilter(val label: String) {
    ALL("All"),
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists"),
    FOLDERS("Folders"),
}

data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
)

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel @Inject constructor(
    private val repository: MusicRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(SearchFilter.ALL)
    val filter: StateFlow<SearchFilter> = _filter.asStateFlow()

    val recentSearches: StateFlow<List<String>> = repository.observeRecentSearches(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Instant, debounced, real-time filtering (Section 6). */
    val uiState: StateFlow<SearchUiState> = _query
        .debounce { if (it.isBlank()) 0L else 220L }
        .distinctUntilChanged()
        .flatMapLatest { q ->
            flow {
                if (q.isBlank()) {
                    emit(SearchUiState(query = q))
                    return@flow
                }
                emit(SearchUiState(query = q, isSearching = true))
                val results = repository.search(q)
                emit(
                    SearchUiState(
                        query = q,
                        results = results,
                        isSearching = false,
                        hasSearched = true,
                    ),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun setFilter(filter: SearchFilter) {
        _filter.value = filter
    }

    /** Called when the user commits a query (submit / taps a result). */
    fun commitQuery(value: String = _query.value) {
        if (value.isBlank()) return
        viewModelScope.launch { repository.addRecentSearch(value) }
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun clearRecentSearches() {
        viewModelScope.launch { repository.clearRecentSearches() }
    }
}
