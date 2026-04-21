package com.minibrowser.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.minibrowser.app.MiniBrowserApp
import com.minibrowser.app.ui.screens.BookmarkScreen
import com.minibrowser.app.ui.screens.BrowserScreen
import com.minibrowser.app.ui.screens.HistoryScreen
import com.minibrowser.app.ui.screens.HomeScreen
import com.minibrowser.app.ui.screens.VideoLibraryScreen
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val HOME = "home"
    const val BROWSER = "browser/{input}"
    const val VIDEO_LIBRARY = "video_library"
    const val VIDEO_PLAYER = "video_player/{path}"
    const val BOOKMARKS = "bookmarks"
    const val HISTORY = "history"

    fun browser(input: String): String {
        val encoded = URLEncoder.encode(input, "UTF-8")
        return "browser/$encoded"
    }

    fun videoPlayer(path: String): String {
        val encoded = URLEncoder.encode(path, "UTF-8")
        return "video_player/$encoded"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as MiniBrowserApp
    val scope = rememberCoroutineScope()

    val selectedEngineId by app.preferencesRepository.selectedEngineId
        .collectAsState(initial = "google")

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                selectedEngineId = selectedEngineId,
                onNavigate = { input ->
                    navController.navigate(Routes.browser(input))
                },
                onEngineSelected = { engine ->
                    scope.launch {
                        app.preferencesRepository.setSearchEngine(engine.id)
                    }
                },
                onOpenVideoLibrary = {
                    navController.navigate(Routes.VIDEO_LIBRARY)
                },
                onOpenBookmarks = {
                    navController.navigate(Routes.BOOKMARKS)
                },
                onOpenHistory = {
                    navController.navigate(Routes.HISTORY)
                }
            )
        }
        composable(
            route = Routes.BROWSER,
            arguments = listOf(navArgument("input") { type = NavType.StringType })
        ) { backStackEntry ->
            val input = URLDecoder.decode(
                backStackEntry.arguments?.getString("input") ?: "",
                "UTF-8"
            )
            BrowserScreen(
                initialInput = input,
                selectedEngineId = selectedEngineId,
                onEngineSelected = { engine ->
                    scope.launch {
                        app.preferencesRepository.setSearchEngine(engine.id)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.VIDEO_LIBRARY) {
            VideoLibraryScreen(
                downloadManager = app.downloadManager,
                onBack = { navController.popBackStack() },
                onPlayVideo = { path ->
                    navController.navigate(Routes.videoPlayer(path))
                }
            )
        }
        composable(
            route = Routes.VIDEO_PLAYER,
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = URLDecoder.decode(
                backStackEntry.arguments?.getString("path") ?: "",
                "UTF-8"
            )
            com.minibrowser.app.ui.components.VideoPlayer(
                videoUrl = path,
                onDismiss = { navController.popBackStack() },
                modifier = androidx.compose.ui.Modifier.fillMaxSize()
            )
        }
        composable(Routes.BOOKMARKS) {
            BookmarkScreen(
                bookmarkRepository = app.bookmarkRepository,
                onNavigate = { url ->
                    navController.navigate(Routes.browser(url))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                historyRepository = app.historyRepository,
                onNavigate = { url ->
                    navController.navigate(Routes.browser(url))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
