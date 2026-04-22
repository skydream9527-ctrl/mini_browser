package com.minibrowser.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.MiniBrowserApp
import com.minibrowser.app.download.VideoType
import com.minibrowser.app.engine.SearchEngine
import com.minibrowser.app.engine.SearchEngineConfig
import com.minibrowser.app.engine.UrlUtil
import com.minibrowser.app.sniffer.SniffedVideo
import com.minibrowser.app.ui.components.BrowserView
import com.minibrowser.app.ui.components.SearchEngineSelector
import com.minibrowser.app.ui.components.SniffedVideoSheet
import com.minibrowser.app.ui.components.VideoPlayer
import com.minibrowser.app.ui.components.VideoSnifferFab
import com.minibrowser.app.ui.theme.AccentRed
import com.minibrowser.app.ui.theme.DarkBackground
import com.minibrowser.app.ui.theme.DarkToolbar
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun BrowserScreen(
    initialInput: String,
    selectedEngineId: String,
    onEngineSelected: (SearchEngine) -> Unit,
    onBack: () -> Unit,
    onOpenTabSwitcher: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as MiniBrowserApp
    val engineManager = app.geckoEngineManager
    val videoSniffer = app.videoSniffer
    val downloadManager = app.downloadManager
    val bookmarkRepo = app.bookmarkRepository
    val historyRepo = app.historyRepository
    val scope = rememberCoroutineScope()
    val currentEngine = SearchEngineConfig.findById(selectedEngineId)
        ?: SearchEngineConfig.builtInEngines.first()

    var currentUrl by remember { mutableStateOf("") }
    var urlBarText by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showEngineSelector by remember { mutableStateOf(false) }
    var showFindInPage by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var isDesktopMode by remember { mutableStateOf(false) }
    var showTranslateDialog by remember { mutableStateOf(false) }
    var showVideoSheet by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var currentTitle by remember { mutableStateOf("") }

    val sniffedVideos by videoSniffer.sniffedVideos.collectAsState()
    val isBookmarked by bookmarkRepo.isBookmarked(currentUrl).collectAsState(initial = false)

    val session = remember {
        videoSniffer.clear()
        engineManager.apply {
            onTitleChanged = { title -> currentTitle = title }
            onUrlChanged = { url ->
                currentUrl = url
                if (!isEditing) urlBarText = url
                if (url.startsWith("http") && app.tabManager.activeTab?.isIncognito != true) {
                    scope.launch { historyRepo.recordVisit(url, currentTitle) }
                }
            }
            onProgressChanged = { p -> progress = p }
            onCanGoBackChanged = { canGoBack = it }
            onCanGoForwardChanged = { canGoForward = it }
            onFullScreenRequest = { fullScreen ->
                isFullScreen = fullScreen
                if (fullScreen) {
                    engineManager.getVideoUrl { url ->
                        videoUrl = url
                    }
                } else {
                    videoUrl = null
                }
            }
        }
        engineManager.createSession()
    }

    LaunchedEffect(initialInput) {
        val url = UrlUtil.resolveInput(initialInput, currentEngine)
        engineManager.loadUrl(url)
    }

    DisposableEffect(Unit) {
        onDispose { }
    }

    if (isFullScreen && videoUrl != null) {
        VideoPlayer(
            videoUrl = videoUrl!!,
            onDismiss = {
                isFullScreen = false
                videoUrl = null
            },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkToolbar)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (canGoBack) engineManager.goBack() else onBack()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (canGoBack) TextPrimary else TextSecondary
                    )
                }

                IconButton(
                    onClick = { engineManager.goForward() },
                    enabled = canGoForward
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (canGoForward) TextPrimary else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            DarkBackground.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = urlBarText,
                        onValueChange = {
                            urlBarText = it
                            isEditing = true
                        },
                        singleLine = true,
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(TextPrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                isEditing = false
                                val url = UrlUtil.resolveInput(urlBarText, currentEngine)
                                engineManager.loadUrl(url)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = TextPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isBookmarked) "取消收藏" else "收藏") },
                            leadingIcon = {
                                Icon(
                                    if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                                    null,
                                    tint = if (isBookmarked) com.minibrowser.app.ui.theme.AccentPurple else androidx.compose.material3.LocalContentColor.current
                                )
                            },
                            onClick = {
                                scope.launch {
                                    if (isBookmarked) {
                                        bookmarkRepo.remove(currentUrl)
                                        Toast.makeText(context, "已取消收藏", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val added = bookmarkRepo.add(currentTitle.ifBlank { currentUrl }, currentUrl)
                                        Toast.makeText(context, if (added) "已收藏" else "已存在", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("刷新") },
                            leadingIcon = { Icon(Icons.Default.Refresh, null) },
                            onClick = {
                                engineManager.reload()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("搜索引擎") },
                            onClick = {
                                showEngineSelector = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("标签页 (${app.tabManager.tabCount})") },
                            onClick = {
                                showMenu = false
                                onOpenTabSwitcher()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("视频库") },
                            leadingIcon = { Icon(Icons.Default.VideoLibrary, null) },
                            onClick = {
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("分享") },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, currentUrl)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                val count = app.adBlocker.blockedCount.collectAsState().value
                                Text("已拦截广告 ($count)")
                            },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("阅读模式") },
                            onClick = {
                                showMenu = false
                                engineManager.getCurrentSession()?.loadUri(
                                    "javascript:void(document.title)"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("页面内搜索") },
                            onClick = {
                                showMenu = false
                                showFindInPage = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isDesktopMode) "手机模式" else "桌面模式") },
                            onClick = {
                                showMenu = false
                                isDesktopMode = !isDesktopMode
                                if (currentUrl.isNotBlank()) {
                                    engineManager.loadUrl(currentUrl)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("截图") },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    val activity = context as? android.app.Activity ?: return@launch
                                    val uri = app.screenshotCapture.captureView(activity)
                                    if (uri != null) {
                                        app.screenshotCapture.showSaved(uri)
                                    }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("翻译页面") },
                            onClick = {
                                showMenu = false
                                showTranslateDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("画中画") },
                            onClick = {
                                showMenu = false
                                val activity = context as? android.app.Activity ?: return@DropdownMenuItem
                                com.minibrowser.app.player.PipHelper.enterPip(activity)
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = progress in 1..99,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = AccentRed,
                    trackColor = DarkToolbar
                )
            }

            if (showFindInPage) {
                com.minibrowser.app.ui.components.FindInPageBar(
                    query = findQuery,
                    onQueryChange = { findQuery = it },
                    onNext = {
                        engineManager.getCurrentSession()?.loadUri(
                            "javascript:void(window.find('$findQuery',false,false,true))"
                        )
                    },
                    onPrevious = {
                        engineManager.getCurrentSession()?.loadUri(
                            "javascript:void(window.find('$findQuery',false,true,true))"
                        )
                    },
                    onClose = {
                        showFindInPage = false
                        findQuery = ""
                    }
                )
            }

            com.minibrowser.app.ui.components.GestureNavigationWrapper(
                onSwipeRight = { if (canGoBack) engineManager.goBack() },
                onSwipeLeft = { if (canGoForward) engineManager.goForward() }
            ) {
                BrowserView(
                    session = session,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        VideoSnifferFab(
            videoCount = sniffedVideos.size,
            onClick = { showVideoSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showEngineSelector) {
        SearchEngineSelector(
            selectedEngineId = selectedEngineId,
            onEngineSelected = { engine ->
                onEngineSelected(engine)
                showEngineSelector = false
            },
            onDismiss = { showEngineSelector = false }
        )
    }

    if (showVideoSheet) {
        SniffedVideoSheet(
            videos = sniffedVideos,
            onDownload = { video ->
                scope.launch {
                    val type = when (video.videoType) {
                        SniffedVideo.VideoType.M3U8 -> VideoType.M3U8
                        SniffedVideo.VideoType.DASH -> VideoType.DASH
                        SniffedVideo.VideoType.WEBM -> VideoType.WEBM
                        SniffedVideo.VideoType.FLV -> VideoType.FLV
                        else -> VideoType.MP4
                    }
                    downloadManager.enqueue(
                        url = video.url,
                        title = video.displayName,
                        type = type,
                        pageUrl = video.pageUrl
                    )
                    Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show()
                }
                showVideoSheet = false
            },
            onDismiss = { showVideoSheet = false }
        )
    }

    if (showTranslateDialog) {
        com.minibrowser.app.ui.components.TranslateDialog(
            onSelectLanguage = { lang ->
                showTranslateDialog = false
                val translateUrl = com.minibrowser.app.translate.PageTranslator.translateViaRedirect(currentUrl, lang)
                engineManager.loadUrl(translateUrl)
            },
            onDismiss = { showTranslateDialog = false }
        )
    }
}
