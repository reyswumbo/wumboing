package com.wumboing.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wumboing.app.data.local.Bookmark
import com.wumboing.app.data.local.LocalStore
import com.wumboing.app.data.local.ReadingRecord
import com.wumboing.app.data.model.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class LibraryUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val history: List<ReadingRecord> = emptyList(),
    val tab: Int = 0
)

class LibraryViewModel(
    private val store: LocalStore
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state

    init {
        viewModelScope.launch { store.bookmarks.collectLatest { b -> _state.value = _state.value.copy(bookmarks = b) } }
        viewModelScope.launch { store.history.collectLatest { h -> _state.value = _state.value.copy(history = h) } }
    }

    fun setTab(tab: Int) {
        _state.value = _state.value.copy(tab = tab)
    }

    fun removeBookmark(key: String) {
        viewModelScope.launch {
            val b = _state.value.bookmarks.firstOrNull { it.key == key } ?: return@launch
            store.toggleBookmark(b)
        }
    }

    fun sourceOf(b: Bookmark): Source = Source.fromId(b.sourceId)

    fun open(slug: String, source: Source, title: String, cover: String, chapter: String, page: Int) {}
}
