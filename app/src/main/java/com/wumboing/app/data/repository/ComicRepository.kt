package com.wumboing.app.data.repository

import com.wumboing.app.data.model.ChapterPages
import com.wumboing.app.data.model.Comic
import com.wumboing.app.data.model.ComicDetail
import com.wumboing.app.data.model.Source
import com.wumboing.app.data.source.ComicSource

/**
 * Orchestrates the built-in sources (WZ and AW).
 * Since each source is independent, a failure in one source will not prevent
 * users from using the other.
 */
class ComicRepository(
    private val sources: Map<String, ComicSource>
) {

    fun availableSources(): List<Source> = Source.entries

    private fun sourceOf(source: Source): ComicSource =
        sources[source.id] ?: error("Source ${source.id} tidak dikenali")

    suspend fun getHome(source: Source): List<Comic> = sourceOf(source).getHomeComics()

    suspend fun getLatest(source: Source, page: Int): List<Comic> = sourceOf(source).getLatestComics(page)

    suspend fun search(source: Source, query: String): List<Comic> = sourceOf(source).search(query)

    suspend fun getDetail(source: Source, slug: String): ComicDetail = sourceOf(source).getDetail(slug)

    suspend fun getChapter(source: Source, slug: String, chapter: String): ChapterPages =
        sourceOf(source).getChapterPages(slug, chapter)
}
