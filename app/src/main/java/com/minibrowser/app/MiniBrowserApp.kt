package com.minibrowser.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import com.minibrowser.app.adblocker.AdBlocker
import com.minibrowser.app.adblocker.AdBlockerExtensionManager
import com.minibrowser.app.data.BookmarkRepository
import com.minibrowser.app.data.HistoryRepository
import com.minibrowser.app.data.MiniBrowserDatabase
import com.minibrowser.app.data.PreferencesRepository
import com.minibrowser.app.data.ShortcutDao
import com.minibrowser.app.download.DownloadManager
import com.minibrowser.app.download.FileDownloader
import com.minibrowser.app.engine.GeckoEngineManager
import com.minibrowser.app.screenshot.ScreenshotCapture
import com.minibrowser.app.sniffer.VideoSniffer
import com.minibrowser.app.sniffer.WebExtensionManager
import com.minibrowser.app.tab.TabManager

class MiniBrowserApp : Application() {

    lateinit var geckoEngineManager: GeckoEngineManager
        private set
    lateinit var preferencesRepository: PreferencesRepository
        private set
    lateinit var videoSniffer: VideoSniffer
        private set
    lateinit var downloadManager: DownloadManager
        private set
    lateinit var bookmarkRepository: BookmarkRepository
        private set
    lateinit var historyRepository: HistoryRepository
        private set
    lateinit var tabManager: TabManager
        private set
    lateinit var adBlocker: AdBlocker
        private set
    lateinit var shortcutDao: ShortcutDao
        private set
    lateinit var screenshotCapture: ScreenshotCapture
        private set
    lateinit var fileDownloader: FileDownloader
        private set

    var initFailed = false
        private set

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        val database = MiniBrowserDatabase.getInstance(this)
        preferencesRepository = PreferencesRepository(this)
        videoSniffer = VideoSniffer()
        adBlocker = AdBlocker()
        screenshotCapture = ScreenshotCapture(this)
        fileDownloader = FileDownloader(this)
        downloadManager = DownloadManager(this)
        bookmarkRepository = BookmarkRepository(database.bookmarkDao())
        historyRepository = HistoryRepository(database.historyDao())
        shortcutDao = database.shortcutDao()

        try {
            geckoEngineManager = GeckoEngineManager.getInstance(this)
            tabManager = TabManager(geckoEngineManager.runtime)
            WebExtensionManager(geckoEngineManager.runtime, videoSniffer).install()
            AdBlockerExtensionManager(geckoEngineManager.runtime, adBlocker).install()
        } catch (e: Exception) {
            Log.e(TAG, "GeckoView initialization failed", e)
            initFailed = true
        }
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            DOWNLOAD_CHANNEL_ID,
            "下载进度",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "显示视频下载进度"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "MiniBrowserApp"
        const val DOWNLOAD_CHANNEL_ID = "download_progress"
    }
}
