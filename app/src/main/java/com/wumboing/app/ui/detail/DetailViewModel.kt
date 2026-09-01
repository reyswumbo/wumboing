package com.wumboing.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wumboing.app.data.local.Bookmark
import com.wumboing.app.data.local.LocalStore
import com.wumboing.app.data.model.ComicDetail
import com.wumboing.app.data.model.Source
import com.wumboing.app.data.repository.ComicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val detail: ComicDetail? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val bookmarked: Boolean = false
)

class DetailViewModel(
    private val repository: ComicRepository,
    private val store: LocalStore
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state

    private lateinit var source: Source
    private lateinit var slug: String

    fun start(s: Source, sl: String) {
        if (this::source.isInitialized && source == s && slug == sl) return
        source = s
        slug = sl
        load()
    }

    private fun load() {
        _state.value = DetailUiState(loading = true)
        viewModelScope.launch {
            val bm = store.isBookmarked("${source.id}:$slug")
            runCatching { repository.getDetail(source, slug) }
                .onSuccess { d ->
                    _state.value = _state.value.copy(detail = d, loading = false, bookmarked = bm)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "Gagal memuat detail komik. Periksa koneksi Anda. (${e.message})"
                    )
                }
        }
    }

    fun toggleBookmark() {
        val d = _state.value.detail ?: return
        viewModelScope.launch {
            val nowBm = store.toggleBookmark(
                Bookmark(
                    key = "${source.id}:$slug",
                    sourceId = source.id,
                    slug = slug,
                    title = d.title,
                    coverUrl = d.coverUrl,
                    type = d.type
                )
            )
            _state.value = _state.value.copy(bookmarked = nowBm)
        }
    }
}
