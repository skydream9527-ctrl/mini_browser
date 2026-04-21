package com.minibrowser.app.download

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File

class DownloadManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = DownloadDatabase.getInstance(context)
    private val dao = database.downloadDao()
    private val engine = DownloadEngine()
    private val m3u8Engine = M3u8DownloadEngine(context)

    private val pausedTasks = mutableSetOf<Long>()

    val activeTasks: Flow<List<DownloadTask>> = dao.getActiveTasks()
    val completedTasks: Flow<List<DownloadTask>> = dao.getCompletedTasks()

    private val videosDir: File
        get() = File(context.getExternalFilesDir(null), "Videos").also { it.mkdirs() }

    private val tempDir: File
        get() = File(context.cacheDir, "m3u8_temp").also { it.mkdirs() }

    suspend fun enqueue(
        url: String,
        title: String,
        type: VideoType,
        pageUrl: String = "",
        resolution: String? = null
    ): Long {
        val existing = dao.getTaskByUrl(url)
        if (existing != null) return existing.id

        val task = DownloadTask(
            url = url,
            title = title,
            type = type,
            sourcePageUrl = pageUrl,
            resolution = resolution
        )
        val id = dao.insert(task)
        startDownload(id)
        return id
    }

    private fun startDownload(taskId: Long) {
        scope.launch {
            val activeCount = dao.getActiveDownloadCount()
            if (activeCount >= MAX_CONCURRENT) return@launch

            val task = dao.getTaskById(taskId) ?: return@launch
            dao.updateStatus(taskId, DownloadStatus.DOWNLOADING)
            pausedTasks.remove(taskId)

            when (task.type) {
                VideoType.M3U8 -> downloadM3u8(task)
                else -> downloadDirect(task)
            }
        }
    }

    private suspend fun downloadDirect(task: DownloadTask) {
        val outputFile = File(videosDir, "${task.id}_${sanitizeFilename(task.title)}.${task.type.name.lowercase()}")

        val success = engine.download(
            url = task.url,
            outputFile = outputFile,
            onProgress = { downloaded, total ->
                dao.updateProgress(task.id, downloaded)
                if (task.totalBytes == 0L && total > 0) {
                    dao.update(task.copy(totalBytes = total))
                }
            },
            isPaused = { pausedTasks.contains(task.id) }
        )

        if (pausedTasks.contains(task.id)) {
            dao.updateStatus(task.id, DownloadStatus.PAUSED)
        } else if (success) {
            dao.updateFilePath(task.id, outputFile.absolutePath)
            dao.updateStatus(task.id, DownloadStatus.COMPLETED)
        } else {
            dao.updateStatus(task.id, DownloadStatus.FAILED)
        }

        processQueue()
    }

    private suspend fun downloadM3u8(task: DownloadTask) {
        val workDir = File(tempDir, "task_${task.id}")
        val outputFile = File(videosDir, "${task.id}_${sanitizeFilename(task.title)}.ts")

        val parseResult = m3u8Engine.parsePlaylist(task.url)
        val info = when (val data = parseResult.getOrNull()) {
            is M3u8DownloadEngine.M3u8Info -> data
            is List<*> -> {
                dao.updateStatus(task.id, DownloadStatus.FAILED)
                return
            }
            else -> {
                dao.updateStatus(task.id, DownloadStatus.FAILED)
                return
            }
        }

        dao.update(task.copy(totalSegments = info.segments.size))

        val segmentsOk = m3u8Engine.downloadSegments(
            info = info,
            workDir = workDir,
            onSegmentComplete = { completed, total ->
                dao.updateSegmentProgress(task.id, completed)
            },
            isPaused = { pausedTasks.contains(task.id) },
            startFrom = task.completedSegments
        )

        if (pausedTasks.contains(task.id)) {
            dao.updateStatus(task.id, DownloadStatus.PAUSED)
            return
        }

        if (!segmentsOk) {
            dao.updateStatus(task.id, DownloadStatus.FAILED)
            processQueue()
            return
        }

        dao.updateStatus(task.id, DownloadStatus.MERGING)
        val merged = m3u8Engine.mergeToMp4(workDir, outputFile)

        if (merged) {
            workDir.deleteRecursively()
            dao.updateFilePath(task.id, outputFile.absolutePath)
            dao.updateStatus(task.id, DownloadStatus.COMPLETED)
        } else {
            dao.updateStatus(task.id, DownloadStatus.FAILED)
        }

        processQueue()
    }

    suspend fun pause(taskId: Long) {
        pausedTasks.add(taskId)
    }

    suspend fun resume(taskId: Long) {
        pausedTasks.remove(taskId)
        dao.updateStatus(taskId, DownloadStatus.PENDING)
        startDownload(taskId)
    }

    suspend fun retry(taskId: Long) {
        dao.updateStatus(taskId, DownloadStatus.PENDING)
        startDownload(taskId)
    }

    suspend fun remove(taskId: Long) {
        pausedTasks.add(taskId)
        val task = dao.getTaskById(taskId) ?: return
        task.filePath?.let { File(it).delete() }
        File(tempDir, "task_$taskId").deleteRecursively()
        dao.delete(task)
    }

    private suspend fun processQueue() {
        val activeCount = dao.getActiveDownloadCount()
        if (activeCount >= MAX_CONCURRENT) return

        val pending = dao.getTaskById(0)
        // Queue processing handled by Room Flow observers
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fff._-]"), "_").take(50)
    }

    companion object {
        const val MAX_CONCURRENT = 3
    }
}
