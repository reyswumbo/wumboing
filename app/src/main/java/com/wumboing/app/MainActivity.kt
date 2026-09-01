package com.wumboing.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wumboing.app.data.local.LocalStore
import com.wumboing.app.data.model.Source
import com.wumboing.app.ui.detail.DetailScreen
import com.wumboing.app.ui.home.HomeScreen
import com.wumboing.app.ui.library.LibraryScreen
import com.wumboing.app.ui.navigation.Routes
import com.wumboing.app.ui.reader.ReaderScreen
import com.wumboing.app.ui.theme.WumboingTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    private val store: LocalStore by lazy { get<LocalStore>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WumboingAppRoot(store = store)
        }
    }
}

@Composable
fun WumboingAppRoot(store: LocalStore) {
    val savedDark by store.darkMode.collectAsState(initial = true)
    var darkOverride by remember { mutableStateOf<Boolean?>(null) }
    val darkTheme = darkOverride ?: savedDark
    val scope = rememberCoroutineScope()

    WumboingTheme(darkTheme = darkTheme) {
        val nav = rememberNavController()
        WumboingNavHost(
            nav = nav,
            darkTheme = darkTheme,
            onToggleTheme = {
                val newDark = !darkTheme
                darkOverride = newDark
                scope.launch { store.setDarkMode(newDark) }
            }
        )
    }
}

@Composable
fun WumboingNavHost(
    nav: NavHostController,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenDetail = { source, slug ->
                    nav.navigate(Routes.detail(source.id, slug))
                },
                onOpenLibrary = { nav.navigate(Routes.LIBRARY) },
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("source") { type = NavType.StringType },
                navArgument("slug") { type = NavType.StringType }
            )
        ) { entry ->
            val source = Source.fromId(entry.arguments?.getString("source") ?: "wz")
            val slug = entry.arguments?.getString("slug") ?: ""
            DetailScreen(
                source = source,
                slug = slug,
                onBack = { nav.popBackStack() },
                onOpenReader = { s, sl, label ->
                    val title = "komik"
                    nav.navigate(Routes.reader(s.id, sl, label, title, ""))
                }
            )
        }

        composable(
            route = Routes.READER,
            arguments = listOf(
                navArgument("source") { type = NavType.StringType },
                navArgument("slug") { type = NavType.StringType },
                navArgument("label") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("cover") { type = NavType.StringType }
            )
        ) { entry ->
            val source = Source.fromId(entry.arguments?.getString("source") ?: "wz")
            val slug = entry.arguments?.getString("slug") ?: ""
            val label = entry.arguments?.getString("label") ?: ""
            val title = entry.arguments?.getString("title") ?: "Komik"
            val cover = entry.arguments?.getString("cover") ?: ""
            ReaderScreen(
                source = source,
                slug = slug,
                label = label,
                title = title,
                cover = cover,
                onBack = { nav.popBackStack() },
                onChapterChanged = { newLabel ->
                    nav.navigate(Routes.reader(source.id, slug, newLabel, title, cover)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.LIBRARY) {
            LibraryScreen(
                onBack = { nav.popBackStack() },
                onOpenDetail = { source, slug ->
                    nav.navigate(Routes.detail(source.id, slug))
                },
                onContinue = { source, slug, chapter, page ->
                    nav.navigate(Routes.reader(source.id, slug, chapter, "Komik", "")) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
