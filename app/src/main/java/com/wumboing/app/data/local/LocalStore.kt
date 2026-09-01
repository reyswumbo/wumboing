package com.wumboing.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "wumboing_prefs")

/** Bookmark entry (favorite comic). */
@Serializable
data class Bookmark(
    val key: String,
    val sourceId: String,
    val slug: String,
    val title: String,
    val coverUrl: String,
    val type: String = ""
)

/** Continue-reading record. */
@Serializable
data class ReadingRecord(
    val key: String,
    val sourceId: String,
    val slug: String,
    val title: String,
    val coverUrl: String,
    val chapterLabel: String,
    val pageIndex: Int
)

class LocalStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val bookmarkKey = stringPreferencesKey("bookmarks")
    private val historyKey = stringPreferencesKey("history")
    private val darkModeKey = booleanPreferencesKey("dark_mode")

    val bookmarks: Flow<List<Bookmark>> = context.dataStore.data.map { prefs ->
        decode(prefs[bookmarkKey])
    }

    val history: Flow<List<ReadingRecord>> = context.dataStore.data.map { prefs ->
        decode(prefs[historyKey])
    }

    suspend fun isBookmarked(key: String): Boolean =
        bookmarks.first().any { it.key == key }

    suspend fun toggleBookmark(bm: Bookmark): Boolean {
        val current = bookmarks.first().toMutableList()
        val exists = current.any { it.key == bm.key }
        if (exists) current.removeAll { it.key == bm.key } else current.add(0, bm)
        context.dataStore.edit { it[bookmarkKey] = json.encodeToString(current) }
        return !exists
    }

    suspend fun addHistory(rec: ReadingRecord) {
        val current = history.first().toMutableList()
        current.removeAll { it.key == rec.key }
        current.add(0, rec)
        context.dataStore.edit { it[historyKey] = json.encodeToString(current) }
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[darkModeKey] ?: true }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[darkModeKey] = enabled }
    }

    private inline fun <reified T> decode(raw: String?): List<T> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<T>>(raw) }.getOrDefault(emptyList())
    }
}
