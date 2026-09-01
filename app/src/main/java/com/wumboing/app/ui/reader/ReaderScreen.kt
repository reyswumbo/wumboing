package com.wumboing.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wumboing.app.data.model.Source
import com.wumboing.app.ui.common.ErrorState
import com.wumboing.app.ui.common.LoadingState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    source: Source,
    slug: String,
    label: String,
    title: String,
    cover: String,
    onBack: () -> Unit,
    onChapterChanged: (String) -> Unit,
    vm: ReaderViewModel = koinViewModel()
) {
    LaunchedEffect(source, slug, label) {
        vm.start(source, slug, label, title, cover)
    }
    val state by vm.state.collectAsStateWithLifecycle()

    var fullscreen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(state.imageUrls.size) {
        if (state.imageUrls.isNotEmpty() && !state.loading) {
            vm.saveProgress(1)
        }
    }

    Box(Modifier.fillMaxSize()) {
        val error = state.error
        when {
            state.loading -> LoadingState()
            error != null -> ErrorState(error)
            state.imageUrls.isEmpty() -> ErrorState("Chapter ini belum tersedia.")
            else -> {
                Column(Modifier.fillMaxSize()) {
                    androidx.compose.material3.Surface(color = MaterialTheme.colorScheme.surface) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    state.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Chapter $label • ${state.imageUrls.size} halaman",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { fullscreen = !fullscreen }) {
                                Text(if (fullscreen) "⬇" else "⛶", fontSize = 18.sp)
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                    ) {
                        itemsIndexed(state.imageUrls) { index, url ->
                            PageImage(url = url, pageIndex = index + 1)
                        }
                        item {
                            ChapterNavFooter(
                                prevLabel = state.prevLabel,
                                nextLabel = state.nextLabel,
                                onPrev = { state.prevLabel?.let { onChapterChanged(it) } },
                                onNext = { state.nextLabel?.let { onChapterChanged(it) } }
                            )
                        }
                    }
                }
            }
        }

        if (!state.loading && state.imageUrls.isNotEmpty()) {
            Text(
                "Chapter $label",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(Color(0x88000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun PageImage(url: String, pageIndex: Int) {
    AsyncImage(
        model = url,
        contentDescription = "Halaman $pageIndex",
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A)),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
private fun ChapterNavFooter(
    prevLabel: String?,
    nextLabel: String?,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "— Akhir Chapter —",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onPrev,
                enabled = prevLabel != null,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text("Sebelumnya", modifier = Modifier.padding(start = 8.dp))
            }
            Button(
                onClick = onNext,
                enabled = nextLabel != null,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text("Berikutnya", modifier = Modifier.padding(end = 8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
