package com.wumboing.app.data.source.aw

import com.wumboing.app.data.model.Chapter
import com.wumboing.app.data.model.ChapterPages
import com.wumboing.app.data.model.Comic
import com.wumboing.app.data.model.ComicDetail
import com.wumboing.app.data.model.Source
import com.wumboing.app.data.source.ComicSource
import com.wumboing.app.data.source.HttpFetcher
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * AW source — wraps https://alawale.net/
 * The site is a server-rendered Next.js app; all data is present in the HTML.
 */
class AwSource(
    private val fetcher: HttpFetcher
) : ComicSource {

    override val sourceId: String = Source.AW.id

    override suspend fun getHomeComics(): List<Comic> {
        val doc = Jsoup.parse(load("/"))
        return parseCards(doc)
    }

    override suspend fun getLatestComics(page: Int): List<Comic> {
        val doc = Jsoup.parse(load("/daftar-komik?sort=update"))
        return parseCards(doc)
    }

    override suspend fun search(query: String): List<Comic> {
        val doc = Jsoup.parse(load("/daftar-komik?q=${encode(query)}"))
        return parseCards(doc)
    }

    override suspend fun getDetail(slug: String): ComicDetail {
        val doc = Jsoup.parse(load("/$slug"))

        val title = doc.selectFirst(".detail-info h1")?.text()
            ?: slug.replace('-', ' ').replaceFirstChar { it.uppercase() }
        val cover = doc.selectFirst(".detail-hero .cover img")?.attr("abs:src")
            ?: doc.selectFirst("img[src*=covers]")?.attr("abs:src")
            ?: ""
        val alt = doc.selectFirst(".detail-info .alt")?.text() ?: ""

        val genres = doc.select(".detail-info .genres a.chip").map { it.text() }
        val synopsis = doc.selectFirst(".syn-body")?.text() ?: ""

        var author = ""
        var type = ""
        var year = ""
        var status = ""
        for (span in doc.select(".detail-info .kv span")) {
            val b = span.selectFirst("b")?.text() ?: continue
            span.select("b").remove()
            val value = span.text().trim()
            when (b.toLowerCase()) {
                "tipe" -> type = value
                "tahun" -> year = value
                "author" -> author = value
                "status" -> status = value
            }
        }

        val chapters = doc.select(".chap-list a.chap-item").mapNotNull { a ->
            val href = a.attr("href")
            if (href.isBlank()) return@mapNotNull null
            val label = a.selectFirst("span")?.text()
                ?.replace("Chapter", "")
                ?.trim()
                ?.let { it.removePrefix("//") }
                ?: href.substringAfterLast('/')
            Chapter(comicSlug = slug, label = label, url = "$BASE$href")
        }

        return ComicDetail(
            source = Source.AW,
            slug = slug,
            title = title,
            coverUrl = cover,
            altTitle = alt,
            author = author,
            type = type,
            status = status,
            year = year,
            genres = genres,
            synopsis = synopsis,
            chapters = chapters
        )
    }

    override suspend fun getChapterPages(slug: String, chapterLabel: String): ChapterPages {
        val html = load("/$slug/ch/$chapterLabel")
        val urls = Regex("https://bmcdn\\.my\\.id/[^\"\\s&]+?\\.(jpg|webp|png)")
            .findAll(html)
            .map { it.value }
            .distinct()
            .sorted()
            .toList()
        return ChapterPages(comicSlug = slug, chapterLabel = chapterLabel, imageUrls = urls)
    }

    private fun parseCards(doc: Document): List<Comic> {
        val out = mutableListOf<Comic>()
        for (card in doc.select("a.card")) {
            val href = card.attr("href")
            val slug = href.removePrefix("/")
            val title = card.selectFirst(".meta .t")?.text() ?: continue
            val cover = card.selectFirst(".thumb img")?.attr("abs:src") ?: ""
            val chapter = card.selectFirst(".meta .sub .ch")?.text() ?: ""
            out += Comic(Source.AW, slug, title, cover, type = "", latestChapter = chapter)
        }
        return out
    }

    private suspend fun load(path: String): String {
        val url = if (path.startsWith("http")) path else BASE + path
        return fetcher.fetch(url)
    }

    private fun encode(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")

    companion object {
        const val BASE = "https://alawale.net"
    }
}
