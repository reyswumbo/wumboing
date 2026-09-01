package com.wumboing.app.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wumboing.app.data.local.LocalStore
import com.wumboing.app.data.local.ReadingRecord
import com.wumboing.app.data.model.Chapter
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
    val coverUrl: String = "",
    val chapterLabel: String = "",
    val prevLabel: String? = null,
    val nextLabel: String? = null
)

class ReaderViewModel(
    private val repository: ComicRepository,
    private val store: LocalStore
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state

    private lateinit var source: Source
    private lateinit var slug: String
    private var titleInfo = ""
    private var coverInfo = ""
    private var chapterLoad: kotlinx.coroutines.Job? = null

    fun start(s: Source, sl: String, lb: String, title: String, cover: String) {
        source = s
        slug = sl
        titleInfo = title
        coverInfo = cover
        updateChapter(lb)
    }

    private fun updateChapter(lb: String, clearImages: Boolean = true) {
        if (clearImages) {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                imageUrls = emptyList(),
                chapterLabel = lb,
                prevLabel = null,
                nextLabel = null,
                title = titleInfo,
                coverUrl = coverInfo
            )
        }
        chapterLoad?.cancel()
        chapterLoad = viewModelScope.launch {
            val currentSource = source
            val currentSlug = slug
            // Load chapter pages first so reading can start.
            val pagesResult = runCatching { repository.getChapter(currentSource, currentSlug, lb) }
            var prev: String? = null
            var next: String? = null
            val title = titleInfo
            val cover = coverInfo

            if (pagesResult.isSuccess) {
                // Load the chapter list to compute prev/next navigation.
                runCatching { repository.getDetail(currentSource, currentSlug) }
                    .getOrNull()
                    ?.chapters
                    ?.let { chapters -> computeNav(chapters, lb) }
                    ?.also { (p, n) -> prev = p; next = n }
            }

            pagesResult
                .onSuccess { pages ->
                    val validUrls = pages.imageUrls.filter { it.startsWith("http") }
                    _state.value = _state.value.copy(
                        imageUrls = validUrls,
                        loading = false,
                        title = title,
                        coverUrl = cover,
                        chapterLabel = lb,
                        prevLabel = prev,
                        nextLabel = next
                    )
                    if (validUrls.isEmpty()) {
                        _state.value = _state.value.copy(
                            loading = false,
                            error = "Gambar chapter tidak ditemukan.",
                            title = title,
                            coverUrl = cover,
                            chapterLabel = lb,
                            prevLabel = prev,
                            nextLabel = next
                        )
                    }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = "Gagal memuat chapter. Periksa koneksi Anda. (${e.message})",
                        title = title,
                        coverUrl = cover,
                        chapterLabel = lb,
                        prevLabel = prev,
                        nextLabel = next
                    )
                }
        }
    }

    fun openChapter(lb: String) {
        if (lb == _state.value.chapterLabel) return
        updateChapter(lb)
    }

    private fun computeNav(chapters: List<Chapter>, current: String): Pair<String?, String?> {
        val index = chapters.indexOfFirst { it.label == current }
        if (index < 0) return null to null
        // chapters are listed newest-first on both sources; "prev" is the previous (newer),
        // "next" is the next (older). We treat forward navigation as older chapters.
        val next = if (index + 1 < chapters.size) chapters[index + 1].label else null
        val prev = if (index - 1 >= 0) chapters[index - 1].label else null
        return prev to next
    }

    fun saveProgress(pageIndex: Int) {
        if (!::source.isInitialized || !::slug.isInitialized) return
        val s = source
        val sl = slug
        val key = "${s.id}:$sl"
        viewModelScope.launch {
            store.addHistory(
                ReadingRecord(
                    key = key,
                    sourceId = s.id,
                    slug = sl,
                    title = titleInfo,
                    coverUrl = coverInfo,
                    chapterLabel = _state.value.chapterLabel,
                    pageIndex = pageIndex
                )
            )
        }
    }
}
