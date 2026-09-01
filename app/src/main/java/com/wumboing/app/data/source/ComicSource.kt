package com.wumboing.app.data.source

import com.wumboing.app.data.model.ChapterPages
import com.wumboing.app.data.model.Comic
import com.wumboing.app.data.model.ComicDetail

/**
 * Abstraction over a single content source (WZ or AW).
 * Each source is fully independent so that a failure in one never
 * affects the other.
 */
interface ComicSource {
    val sourceId: String
    suspend fun getHomeComics(): List<Comic>
    suspend fun getLatestComics(page: Int): List<Comic>
    suspend fun search(query: String): List<Comic>
    suspend fun getDetail(slug: String): ComicDetail
    suspend fun getChapterPages(slug: String, chapterLabel: String): ChapterPages
}
