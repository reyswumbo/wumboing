package com.wumboing.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.wumboing.app.data.model.Source
import com.wumboing.app.ui.common.ComicGrid
import com.wumboing.app.ui.common.EmptyState
import com.wumboing.app.ui.common.ErrorState
import com.wumboing.app.ui.common.LoadingState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel = koinViewModel(),
    onOpenDetail: (Source, String) -> Unit,
    onOpenLibrary: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val state by vm.state.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selectedSource = state.selectedSource
    var query by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(16.dp)) {
                    Text("☰ Wumboing", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Pilih sumber konten",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Source.entries.forEach { source ->
                    NavigationDrawerItem(
                        label = { Text(source.displayName) },
                        selected = source == selectedSource,
                        onClick = {
                            vm.selectSource(source)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 2.dp)),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                NavigationDrawerItem(
                    label = { Text("Perpustakaan") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenLibrary()
                    },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                    modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 2.dp))
                )
                NavigationDrawerItem(
                    label = { Text(if (darkTheme) "Mode Terang" else "Mode Gelap") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onToggleTheme()
                    },
                    icon = { Icon(if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = null) },
                    modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 2.dp))
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("${selectedSource.displayName} — Wumboing") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Buka menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (state.searching) query = ""
                            vm.toggleSearch()
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Cari komik")
                        }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                if (state.searching) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Cari komik…") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { vm.search(query) }),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                when {
                    state.loading && state.comics.isEmpty() -> LoadingState()
                    state.error != null && state.comics.isEmpty() -> ErrorState(state.error!!)
                    state.comics.isEmpty() -> EmptyState("Belum ada komik. Coba sumber lain.")
                    else -> ComicGrid(
                        comics = state.comics,
                        onComicClick = { onOpenDetail(selectedSource, it.slug) },
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    )
                }
            }
        }
    }
}
