package com.wumboing.app.data.source.wz

import com.wumboing.app.data.model.Chapter
import com.wumboing.app.data.model.ChapterPages
import com.wumboing.app.data.model.Comic
import com.wumboing.app.data.model.ComicDetail
import com.wumboing.app.data.model.Source
import com.wumboing.app.data.source.ComicSource
import com.wumboing.app.data.source.HttpFetcher
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.parseToJsonElement

/**
 * WZ source — wraps https://wurmz.net/
 * The site is a server-rendered Next.js app; all data is present in the HTML.
 */
class WzSource(
    private val fetcher: HttpFetcher
) : ComicSource {

    override val sourceId: String = Source.WZ.id

    override suspend fun getHomeComics(): List<Comic> {
        val doc = Jsoup.parse(load("/"), BASE)
        return parseCards(doc)
    }

    override suspend fun getLatestComics(page: Int): List<Comic> {
        val doc = Jsoup.parse(load("/semua-komik?sort=new&page=$page"), BASE)
        return parseCards(doc)
    }

    override suspend fun search(query: String): List<Comic> {
        val doc = Jsoup.parse(load("/search?q=${encode(query)}"), BASE)
        return parseCards(doc)
    }

    override suspend fun getDetail(slug: String): ComicDetail {
        val html = load("/detail/$slug")
        val doc = Jsoup.parse(html, BASE)

        val comicLd = parseComicSeries(html)

        fun str(key: String): String? =
            comicLd?.get(key)?.jsonPrimitive?.contentOrNull

        val title = str("name") ?: doc.selectFirst("h2.comic-title")?.text()
            ?: slug.substringAfterLast("/")

        val imageRaw = str("image") ?: doc.selectFirst("img[src*=covers]")?.attr("abs:src")
        val coverUrl = imageRaw?.let { absolutize(it) } ?: ""

        val altTitle = str("alternateName") ?: ""
        val author = comicLd?.get("author")?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull ?: ""
        val synopsis = str("description") ?: ""

        val genres = comicLd?.get("genre")?.jsonArray
            ?.mapNotNull { it.jsonPrimitive.contentOrNull }
            ?.toList()
            ?: emptyList()

        val type = slug.substringBefore("/", missingDelimiterValue = "")
        val typeLabel = type.replaceFirstChar { it.uppercase() }

        val chapters = runCatching {
            doc.select("a[href$=/chapter/], a[href*=/chapter/]")
                .mapNotNull { a ->
                    val href = a.attr("href")
                    if (!href.contains("/detail/$slug/chapter/")) return@mapNotNull null
                    val label = href.substringAfter("/chapter/").trim()
                    if (label.isBlank()) return@mapNotNull null
                    Chapter(comicSlug = slug, label = label, url = "$BASE$href")
                }
                .distinctBy { it.label }
                .toList()
        }.getOrDefault(emptyList())

        return ComicDetail(
            source = Source.WZ,
            slug = slug,
            title = title,
            coverUrl = coverUrl,
            altTitle = altTitle,
            author = author,
            type = typeLabel,
            status = "",
            genres = genres,
            synopsis = synopsis,
            chapters = chapters
        )
    }

    override suspend fun getChapterPages(slug: String, chapterLabel: String): ChapterPages {
        val html = load("/detail/$slug/chapter/$chapterLabel")
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
        for (card in doc.select("article.comic-card")) {
            val link = card.selectFirst("a[href^=/detail/]") ?: continue
            val slug = link.attr("href").removePrefix("/detail/")
            val title = card.selectFirst(".comic-title")?.text() ?: continue
            val cover = card.selectFirst(".cover-frame img")?.attr("abs:src")?.let { absolutize(it) } ?: ""
            val type = card.selectFirst(".type-badge")?.text() ?: ""
            val latest = card.selectFirst(".ch-num")?.text() ?: ""
            out += Comic(Source.WZ, slug, title, cover, type, latest)
        }
        return out
    }

    private suspend fun load(path: String): String {
        val url = if (path.startsWith("http")) path else BASE + path
        return fetcher.fetch(url)
    }

    private fun encode(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")

    private fun absolutize(u: String): String =
        if (u.startsWith("http")) u else BASE + u

    private fun parseComicSeries(html: String): JsonObject? {
        val json = Json { ignoreUnknownKeys = true }
        val scriptRegex = Regex("<script\\s+type=\"application/ld\\+json\"[^>]*>(.*?)</script>", RegexOption.DOT_MATCHES_ALL)
        for (match in scriptRegex.findAll(html)) {
            val element = runCatching { json.parseToJsonElement(match.groupValues[1].trim()) }.getOrNull() ?: continue
            val obj = element.jsonObject
            if (obj["@type"]?.jsonPrimitive?.contentOrNull == "ComicSeries") return obj
        }
        return null
    }

    companion object {
        const val BASE = "https://wurmz.net"
    }
}
