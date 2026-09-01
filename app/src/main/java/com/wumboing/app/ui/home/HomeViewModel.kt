package com.wumboing.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wumboing.app.data.model.Comic
import com.wumboing.app.data.model.Source
import com.wumboing.app.data.repository.ComicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedSource: Source = Source.WZ,
    val comics: List<Comic> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val searching: Boolean = false
)

class HomeViewModel(
    private val repository: ComicRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        load()
    }

    fun selectSource(source: Source) {
        if (_state.value.selectedSource == source) return
        _state.value = _state.value.copy(selectedSource = source, searching = false)
        load()
    }

    fun toggleSearch() {
        val searching = !_state.value.searching
        _state.value = _state.value.copy(searching = searching)
        if (!searching) load()
    }

    fun search(query: String) {
        val source = _state.value.selectedSource
        if (query.isBlank()) {
            load()
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.search(source, query.trim()) }
                .onSuccess { comics ->
                    _state.value = _state.value.copy(comics = comics, loading = false)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "Pencarian gagal. Periksa koneksi Anda. (${e.message})"
                    )
                }
        }
    }

    fun load() {
        val source = _state.value.selectedSource
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.getHome(source) }
                .onSuccess { comics ->
                    _state.value = _state.value.copy(comics = comics, loading = false)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "Gagal memuat dari sumber ${source.displayName}. Periksa koneksi Anda. (${e.message})"
                    )
                }
        }
    }
}
