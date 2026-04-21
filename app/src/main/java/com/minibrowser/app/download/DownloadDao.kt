package com.minibrowser.app.download

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM download_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<DownloadTask>>

    @Query("SELECT * FROM download_tasks WHERE status IN ('PENDING', 'DOWNLOADING', 'PAUSED', 'MERGING') ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<DownloadTask>>

    @Query("SELECT * FROM download_tasks WHERE status = 'COMPLETED' ORDER BY updatedAt DESC")
    fun getCompletedTasks(): Flow<List<DownloadTask>>

    @Query("SELECT * FROM download_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): DownloadTask?

    @Query("SELECT * FROM download_tasks WHERE url = :url LIMIT 1")
    suspend fun getTaskByUrl(url: String): DownloadTask?

    @Insert
    suspend fun insert(task: DownloadTask): Long

    @Update
    suspend fun update(task: DownloadTask)

    @Delete
    suspend fun delete(task: DownloadTask)

    @Query("UPDATE download_tasks SET status = :status, updatedAt = :time WHERE id = :id")
    suspend fun updateStatus(id: Long, status: DownloadStatus, time: Long = System.currentTimeMillis())

    @Query("UPDATE download_tasks SET downloadedBytes = :bytes, updatedAt = :time WHERE id = :id")
    suspend fun updateProgress(id: Long, bytes: Long, time: Long = System.currentTimeMillis())

    @Query("UPDATE download_tasks SET completedSegments = :segments, updatedAt = :time WHERE id = :id")
    suspend fun updateSegmentProgress(id: Long, segments: Int, time: Long = System.currentTimeMillis())

    @Query("UPDATE download_tasks SET filePath = :path, updatedAt = :time WHERE id = :id")
    suspend fun updateFilePath(id: Long, path: String, time: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM download_tasks WHERE status = 'DOWNLOADING'")
    suspend fun getActiveDownloadCount(): Int
}
