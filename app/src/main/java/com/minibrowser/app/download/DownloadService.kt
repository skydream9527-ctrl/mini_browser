package com.minibrowser.app.download

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.minibrowser.app.MiniBrowserApp
import com.minibrowser.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DownloadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("准备下载..."))
        monitorDownloads()
        return START_STICKY
    }

    private fun monitorDownloads() {
        val db = DownloadDatabase.getInstance(this)
        scope.launch {
            db.downloadDao().getActiveTasks().collect { tasks ->
                if (tasks.isEmpty()) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@collect
                }
                val downloading = tasks.filter { it.status == DownloadStatus.DOWNLOADING }
                val text = if (downloading.isEmpty()) {
                    "等待下载..."
                } else {
                    "${downloading.size} 个视频下载中"
                }
                val notification = createNotification(text)
                getSystemService(android.app.NotificationManager::class.java)
                    ?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, MiniBrowserApp.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("MiniBrowser 下载")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
