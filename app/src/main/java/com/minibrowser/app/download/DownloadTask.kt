package com.minibrowser.app.download

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_tasks")
data class DownloadTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val thumbnailUrl: String? = null,
    val type: VideoType = VideoType.MP4,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val totalSegments: Int = 0,
    val completedSegments: Int = 0,
    val filePath: String? = null,
    val resolution: String? = null,
    val duration: Long? = null,
    val sourcePageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = when {
            type == VideoType.M3U8 && totalSegments > 0 ->
                completedSegments.toFloat() / totalSegments
            totalBytes > 0 ->
                downloadedBytes.toFloat() / totalBytes
            else -> 0f
        }

    val isActive: Boolean
        get() = status == DownloadStatus.DOWNLOADING || status == DownloadStatus.MERGING
}
