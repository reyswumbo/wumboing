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
import org.jsoup.nodes.Element

/**
 * WZ source — wraps https://wurmz.net/
 * The site is a server-rendered Next.js app; all data is present in the HTML.
 */
class WzSource(
    private val fetcher: HttpFetcher
) : ComicSource {

    override val sourceId: String = Source.WZ.id

    override suspend fun getHomeComics(): List<Comic> {
        val doc = Jsoup.parse(load("/"))
        return parseCards(doc)
    }

    override suspend fun getLatestComics(page: Int): List<Comic> {
        val doc = Jsoup.parse(load("/semua-komik?sort=new&page=$page"))
        return parseCards(doc)
    }

    override suspend fun search(query: String): List<Comic> {
        val doc = Jsoup.parse(load("/search?q=${encode(query)}"))
        return parseCards(doc)
    }

    override suspend fun getDetail(slug: String): ComicDetail {
        val html = load("/detail/$slug")
        val doc = Jsoup.parse(html)

        val ld = doc.selectFirst("script[type=application/ld+json]")
            ?.data()
            ?.takeIf { it.contains("\"ComicSeries\"") }

        // Fall back to ComicSeries via raw substring since there are multiple ld+json blocks.
        val comicLd = runCatching {
            val idx = html.indexOf("\"ComicSeries\"")
            if (idx >= 0) {
                val start = html.lastIndexOf("{", idx)
                val end = html.indexOf("}", idx + 20)
                html.substring(start, end + 1)
            } else null
        }.getOrNull()

        val title = comicJsonValue(comicLd, "name") ?: doc.selectFirst("h2.comic-title")?.text()
            ?: slug.substringAfterLast("/")

        val imageRaw = comicJsonValue(comicLd, "image") ?: doc.selectFirst("img[src*=covers]")?.attr("abs:src")
        val coverUrl = imageRaw?.let { absolutize(it) } ?: ""

        val altTitle = comicJsonValue(comicLd, "alternateName") ?: ""
        val author = comicJsonValue(comicLd, "author")?.substringAfter(":\"")?.substringBefore("\"") ?: ""
        val synopsis = comicJsonValue(comicLd, "description") ?: ""

        val genres = runCatching {
            val m = Regex("\"genre\":\\[([^\\]]*)\\]").find(html ?: "")
            m?.groupValues?.getOrNull(1)?.let { seg ->
                Regex("\"([^\"]+)\"").findAll(seg).map { it.groupValues[1] }.toList()
            } ?: emptyList()
        }.getOrDefault(emptyList())

        val type = slug.substringBefore("/", missingDelimiterValue = "")
        val typeLabel = type.replaceFirstChar { it.uppercase() }

        val chapters = runCatching {
            val m = Regex("\"sourceSlug\":\"([^\"]+)\"[^\\]]*?\"chapters\":(\\[.*?\\])").find(html ?: "")
                ?: Regex("\"chapters\":(\\[.*?\\])(?:,\"activeChapter\"|\")").find(html ?: "")
            val list = m?.groupValues?.getOrNull(2)
                ?: Regex("\"chapters\":(\\[[^]]*\\])").find(html ?: "")?.groupValues?.getOrNull(1)
            if (list == null) emptyList() else {
                Regex("\"chapter_label\":\"([^\"]+)\"").findAll(list)
                    .map { it.groupValues[1] }
                    .map { label ->
                        Chapter(
                            comicSlug = slug,
                            label = label,
                            url = "$BASE/detail/$slug/chapter/$label"
                        )
                    }
                    .toList()
            }
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

    private fun comicJsonValue(ldJson: String?, key: String): String? {
        if (ldJson == null) return null
        val m = Regex("\"$key\":\"((?:\\\\.|[^\"\\\\])*)\"").find(ldJson)
        return m?.groupValues?.getOrNull(1)?.replace("\\/", "/")
    }

    companion object {
        const val BASE = "https://wurmz.net"
    }
}
