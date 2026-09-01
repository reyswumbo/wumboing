package com.wumboing.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wumboing.app.data.model.Chapter
import com.wumboing.app.data.model.Source
import com.wumboing.app.ui.common.EmptyState
import com.wumboing.app.ui.common.ErrorState
import com.wumboing.app.ui.common.LoadingState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    source: Source,
    slug: String,
    onBack: () -> Unit,
    onOpenReader: (Source, String, String) -> Unit,
    vm: DetailViewModel = koinViewModel()
) {
    LaunchedEffect(source, slug) { vm.start(source, slug) }
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Komik") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (state.detail != null) {
                        IconButton(onClick = { vm.toggleBookmark() }) {
                            Icon(
                                if (state.bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (state.bookmarked) "Hapus bookmark" else "Tambah bookmark"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val detail = state.detail
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null -> ErrorState(state.error!!, Modifier.padding(padding))
            detail == null -> EmptyState("Detail tidak ditemukan.", Modifier.padding(padding))
            else -> DetailContent(
                detail = detail,
                onOpenReader = onOpenReader,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: com.wumboing.app.data.model.ComicDetail,
    onOpenReader: (Source, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Row(Modifier.padding(16.dp)) {
                AsyncImage(
                    model = detail.coverUrl,
                    contentDescription = detail.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(120.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column(Modifier.padding(start = 16.dp)) {
                    Text(detail.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (detail.author.isNotBlank()) {
                        Text(
                            "Oleh ${detail.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (detail.status.isNotBlank()) {
                        Text(detail.status, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                    }
                    if (detail.genres.isNotEmpty()) {
                        Text(
                            detail.genres.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        if (detail.synopsis.isNotBlank()) {
            item {
                Text("Sinopsis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Text(
                    detail.synopsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        item {
            Text(
                "Daftar Chapter (${detail.chapters.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        if (detail.chapters.isEmpty()) {
            item { EmptyState("Belum ada chapter.") }
        } else {
            items(detail.chapters.size) { i ->
                val ch = detail.chapters[i]
                ChapterRow(ch) {
                    onOpenReader(detail.source, detail.slug, ch.label)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ChapterRow(chapter: Chapter, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Chapter ${chapter.label}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
