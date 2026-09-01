package com.wumboing.app.data.model

/** Enum of supported content sources, using their abbreviated in-app names. */
enum class Source(
    val id: String,
    val displayName: String,
    val baseUrl: String
) {
    WZ("wz", "WZ", "https://wurmz.net"),
    AW("aw", "AW", "https://alawale.net");

    companion object {
        fun fromId(id: String): Source = entries.firstOrNull { it.id == id } ?: WZ
    }
}

/** A comic/manga listed in a source. */
data class Comic(
    val source: Source,
    val slug: String,
    val title: String,
    val coverUrl: String,
    val type: String = "",
    val latestChapter: String = ""
) {
    val key: String get() = "${source.id}:$slug"
}

/** A single chapter of a comic, as listed on the detail page. */
data class Chapter(
    val comicSlug: String,
    val label: String,
    val url: String
)

/** Full detail of a comic including chapters, genres and synopsis. */
data class ComicDetail(
    val source: Source,
    val slug: String,
    val title: String,
    val coverUrl: String,
    val altTitle: String = "",
    val author: String = "",
    val type: String = "",
    val status: String = "",
    val year: String = "",
    val genres: List<String> = emptyList(),
    val synopsis: String = "",
    val chapters: List<Chapter> = emptyList()
)

/** Chapter reader contents: list of image URLs for each page. */
data class ChapterPages(
    val comicSlug: String,
    val chapterLabel: String,
    val imageUrls: List<String>
)
