package com.minibrowser.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.minibrowser.app.MiniBrowserApp
import com.minibrowser.app.ui.components.BottomNavBar
import com.minibrowser.app.ui.components.NavTab
import com.minibrowser.app.ui.screens.BookmarkScreen
import com.minibrowser.app.ui.screens.BrowserScreen
import com.minibrowser.app.ui.screens.HistoryScreen
import com.minibrowser.app.ui.screens.HomeScreen
import com.minibrowser.app.ui.screens.ReaderScreen
import com.minibrowser.app.ui.screens.TabSwitcherScreen
import com.minibrowser.app.ui.screens.VideoLibraryScreen
import com.minibrowser.app.ui.theme.Black
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
    const val TAB_SWITCHER = "tab_switcher"
    const val READER = "reader/{title}/{content}"
    const val MENU = "menu"

    fun browser(input: String): String {
        val encoded = URLEncoder.encode(input, "UTF-8")
        return "browser/$encoded"
    }

    fun videoPlayer(path: String): String {
        val encoded = URLEncoder.encode(path, "UTF-8")
        return "video_player/$encoded"
    }

    fun reader(title: String, content: String): String {
        val t = URLEncoder.encode(title, "UTF-8")
        val c = URLEncoder.encode(content, "UTF-8")
        return "reader/$t/$c"
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

    var currentTab by remember { mutableStateOf(NavTab.HOME) }

    val showBottomBar = remember(navController) {
        mutableStateOf(true)
    }

    navController.addOnDestinationChangedListener { _, destination, _ ->
        showBottomBar.value = when (destination.route) {
            Routes.HOME, Routes.BOOKMARKS, Routes.VIDEO_LIBRARY, Routes.MENU, Routes.HISTORY -> true
            else -> false
        }
        currentTab = when (destination.route) {
            Routes.HOME -> NavTab.HOME
            Routes.BOOKMARKS -> NavTab.BOOKMARKS
            Routes.VIDEO_LIBRARY -> NavTab.VIDEOS
            Routes.TAB_SWITCHER -> NavTab.TABS
            Routes.HISTORY, Routes.MENU -> NavTab.MENU
            else -> currentTab
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Black)) {
        Box(modifier = Modifier.weight(1f)) {
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
                        onBack = { navController.popBackStack() },
                        onOpenTabSwitcher = {
                            navController.navigate(Routes.TAB_SWITCHER)
                        }
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
                        modifier = Modifier.fillMaxSize()
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
                composable(Routes.TAB_SWITCHER) {
                    TabSwitcherScreen(
                        tabManager = app.tabManager,
                        onSelectTab = { tabId ->
                            app.tabManager.switchTo(tabId)
                            val tab = app.tabManager.activeTab
                            if (tab != null && tab.url.isNotBlank()) {
                                navController.popBackStack()
                                navController.navigate(Routes.browser(tab.url))
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onNewTab = { isIncognito ->
                            app.tabManager.createTab(isIncognito)
                            navController.popBackStack()
                            navController.navigate(Routes.HOME)
                        },
                        onClose = { navController.popBackStack() }
                    )
                }
                composable(
                    route = Routes.READER,
                    arguments = listOf(
                        navArgument("title") { type = NavType.StringType },
                        navArgument("content") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
                    val content = URLDecoder.decode(backStackEntry.arguments?.getString("content") ?: "", "UTF-8")
                    ReaderScreen(
                        title = title,
                        content = content,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        if (showBottomBar.value) {
            BottomNavBar(
                currentTab = currentTab,
                tabCount = app.tabManager.tabCount.coerceAtLeast(1),
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.HOME -> navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                        NavTab.BOOKMARKS -> navController.navigate(Routes.BOOKMARKS) {
                            popUpTo(Routes.HOME)
                        }
                        NavTab.TABS -> navController.navigate(Routes.TAB_SWITCHER) {
                            popUpTo(Routes.HOME)
                        }
                        NavTab.VIDEOS -> navController.navigate(Routes.VIDEO_LIBRARY) {
                            popUpTo(Routes.HOME)
                        }
                        NavTab.MENU -> navController.navigate(Routes.HISTORY) {
                            popUpTo(Routes.HOME)
                        }
                    }
                }
            )
        }
    }
}
