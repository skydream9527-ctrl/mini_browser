package com.minibrowser.app.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

class DownloadEngine(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    suspend fun download(
        url: String,
        outputFile: File,
        onProgress: suspend (downloaded: Long, total: Long) -> Unit,
        isPaused: () -> Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val existingBytes = if (outputFile.exists()) outputFile.length() else 0L

        val request = Request.Builder()
            .url(url)
            .apply {
                if (existingBytes > 0) {
                    addHeader("Range", "bytes=$existingBytes-")
                }
            }
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful && response.code != 206) {
            response.close()
            return@withContext false
        }

        val body = response.body ?: run {
            response.close()
            return@withContext false
        }

        val totalBytes = if (response.code == 206) {
            val contentRange = response.header("Content-Range")
            contentRange?.substringAfter("/")?.toLongOrNull() ?: (body.contentLength() + existingBytes)
        } else {
            body.contentLength()
        }

        val raf = RandomAccessFile(outputFile, "rw")
        raf.seek(existingBytes)

        var downloaded = existingBytes
        val buffer = ByteArray(8192)

        body.byteStream().use { input ->
            while (isActive) {
                if (isPaused()) {
                    raf.close()
                    response.close()
                    return@withContext true
                }

                val read = input.read(buffer)
                if (read == -1) break

                raf.write(buffer, 0, read)
                downloaded += read
                onProgress(downloaded, totalBytes)
            }
        }

        raf.close()
        response.close()
        downloaded >= totalBytes
    }

    fun getContentLength(url: String): Long {
        val request = Request.Builder().url(url).head().build()
        val response = client.newCall(request).execute()
        val length = response.header("Content-Length")?.toLongOrNull() ?: -1
        response.close()
        return length
    }
}
