package com.wumboing.app.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wumboing.app.data.local.LocalStore
import com.wumboing.app.data.local.ReadingRecord
import com.wumboing.app.data.model.Source
import com.wumboing.app.data.repository.ComicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReaderUiState(
    val imageUrls: List<String> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val title: String = "",
    val coverUrl: String = ""
)

class ReaderViewModel(
    private val repository: ComicRepository,
    private val store: LocalStore
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state

    private lateinit var source: Source
    private lateinit var slug: String
    private lateinit var label: String
    private var titleInfo = ""
    private var coverInfo = ""

    fun start(s: Source, sl: String, lb: String, title: String, cover: String) {
        source = s
        slug = sl
        label = lb
        titleInfo = title
        coverInfo = cover
        load()
    }

    private fun load() {
        _state.value = ReaderUiState(loading = true, title = titleInfo, coverUrl = coverInfo)
        viewModelScope.launch {
            runCatching { repository.getChapter(source, slug, label) }
                .onSuccess { pages ->
                    _state.value = _state.value.copy(
                        imageUrls = pages.imageUrls,
                        loading = false,
                        title = titleInfo,
                        coverUrl = coverInfo
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "Gagal memuat chapter. Periksa koneksi Anda. (${e.message})",
                        title = titleInfo,
                        coverUrl = coverInfo
                    )
                }
        }
    }

    fun saveProgress(pageIndex: Int) {
        viewModelScope.launch {
            store.addHistory(
                ReadingRecord(
                    key = "${source.id}:$slug",
                    sourceId = source.id,
                    slug = slug,
                    title = titleInfo,
                    coverUrl = coverInfo,
                    chapterLabel = label,
                    pageIndex = pageIndex
                )
            )
        }
    }
}
