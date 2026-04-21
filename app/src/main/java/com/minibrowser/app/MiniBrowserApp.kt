package com.minibrowser.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.minibrowser.app.data.PreferencesRepository
import com.minibrowser.app.download.DownloadManager
import com.minibrowser.app.engine.GeckoEngineManager
import com.minibrowser.app.sniffer.VideoSniffer
import com.minibrowser.app.sniffer.WebExtensionManager

class MiniBrowserApp : Application() {

    lateinit var geckoEngineManager: GeckoEngineManager
        private set
    lateinit var preferencesRepository: PreferencesRepository
        private set
    lateinit var videoSniffer: VideoSniffer
        private set
    lateinit var downloadManager: DownloadManager
        private set

    override fun onCreate() {
        super.onCreate()
        geckoEngineManager = GeckoEngineManager(this)
        preferencesRepository = PreferencesRepository(this)
        videoSniffer = VideoSniffer()
        downloadManager = DownloadManager(this)
        createNotificationChannels()
        WebExtensionManager(geckoEngineManager.runtime, videoSniffer).install()
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
        const val DOWNLOAD_CHANNEL_ID = "download_progress"
    }
}
