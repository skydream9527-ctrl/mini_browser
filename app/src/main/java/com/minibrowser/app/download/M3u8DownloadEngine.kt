package com.minibrowser.app.download

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class M3u8DownloadEngine(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    data class M3u8Info(
        val segments: List<Segment>,
        val encryptionKey: ByteArray? = null,
        val iv: ByteArray? = null
    )

    data class Segment(val url: String, val duration: Float = 0f)

    data class Resolution(val label: String, val url: String, val bandwidth: Long = 0)

    suspend fun parsePlaylist(url: String): Result<Any> = withContext(Dispatchers.IO) {
        val content = fetchText(url) ?: return@withContext Result.failure(Exception("Failed to fetch playlist"))

        if (content.contains("#EXT-X-STREAM-INF")) {
            val resolutions = parseMasterPlaylist(content, url)
            Result.success(resolutions)
        } else {
            val info = parseMediaPlaylist(content, url)
            Result.success(info)
        }
    }

    private fun parseMasterPlaylist(content: String, baseUrl: String): List<Resolution> {
        val resolutions = mutableListOf<Resolution>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (line.startsWith("#EXT-X-STREAM-INF")) {
                val bandwidth = Regex("BANDWIDTH=(\\d+)").find(line)
                    ?.groupValues?.get(1)?.toLongOrNull() ?: 0
                val resolution = Regex("RESOLUTION=(\\S+)").find(line)
                    ?.groupValues?.get(1) ?: "${bandwidth / 1000}k"
                val nextLine = if (i + 1 < lines.size) lines[i + 1].trim() else ""
                if (nextLine.isNotBlank() && !nextLine.startsWith("#")) {
                    val segUrl = resolveUrl(baseUrl, nextLine)
                    resolutions.add(Resolution(resolution, segUrl, bandwidth))
                }
            }
            i++
        }
        return resolutions.sortedByDescending { it.bandwidth }
    }

    private suspend fun parseMediaPlaylist(content: String, baseUrl: String): M3u8Info {
        val segments = mutableListOf<Segment>()
        var encryptionKey: ByteArray? = null
        var iv: ByteArray? = null

        val lines = content.lines()
        var currentDuration = 0f

        for (line in lines) {
            when {
                line.startsWith("#EXTINF:") -> {
                    currentDuration = line.substringAfter(":").substringBefore(",").toFloatOrNull() ?: 0f
                }
                line.startsWith("#EXT-X-KEY") -> {
                    val keyUri = Regex("URI=\"([^\"]+)\"").find(line)?.groupValues?.get(1)
                    val ivHex = Regex("IV=0x([0-9a-fA-F]+)").find(line)?.groupValues?.get(1)

                    if (keyUri != null) {
                        val keyUrl = resolveUrl(baseUrl, keyUri)
                        encryptionKey = withContext(Dispatchers.IO) { fetchBytes(keyUrl) }
                    }
                    if (ivHex != null) {
                        iv = hexToBytes(ivHex)
                    }
                }
                !line.startsWith("#") && line.isNotBlank() -> {
                    val segUrl = resolveUrl(baseUrl, line.trim())
                    segments.add(Segment(segUrl, currentDuration))
                }
            }
        }

        return M3u8Info(segments, encryptionKey, iv)
    }

    suspend fun downloadSegments(
        info: M3u8Info,
        workDir: File,
        onSegmentComplete: suspend (completed: Int, total: Int) -> Unit,
        isPaused: () -> Boolean,
        startFrom: Int = 0
    ): Boolean = withContext(Dispatchers.IO) {
        workDir.mkdirs()

        for (i in startFrom until info.segments.size) {
            if (!isActive || isPaused()) return@withContext false

            val segment = info.segments[i]
            val segFile = File(workDir, "seg_${String.format("%05d", i)}.ts")

            if (segFile.exists() && segFile.length() > 0) {
                onSegmentComplete(i + 1, info.segments.size)
                continue
            }

            var data = fetchBytes(segment.url) ?: return@withContext false

            if (info.encryptionKey != null) {
                data = decryptSegment(data, info.encryptionKey, info.iv, i)
            }

            segFile.writeBytes(data)
            onSegmentComplete(i + 1, info.segments.size)
        }

        true
    }

    fun mergeToMp4(workDir: File, outputFile: File): Boolean {
        val segFiles = workDir.listFiles { f -> f.name.endsWith(".ts") }
            ?.sortedBy { it.name } ?: return false

        if (segFiles.isEmpty()) return false

        outputFile.outputStream().buffered().use { out ->
            for (seg in segFiles) {
                seg.inputStream().buffered().use { input ->
                    input.copyTo(out)
                }
            }
        }

        return outputFile.exists() && outputFile.length() > 0
    }

    private fun decryptSegment(data: ByteArray, key: ByteArray, iv: ByteArray?, index: Int): ByteArray {
        val actualIv = iv ?: ByteArray(16).apply {
            val idx = index.toLong()
            for (i in 15 downTo 12) {
                this[i] = (idx shr ((15 - i) * 8) and 0xFF).toByte()
            }
        }

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(actualIv))
        return cipher.doFinal(data)
    }

    private fun fetchText(url: String): String? {
        val request = Request.Builder().url(url).build()
        return try {
            val response = client.newCall(request).execute()
            val text = response.body?.string()
            response.close()
            text
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchBytes(url: String): ByteArray? {
        val request = Request.Builder().url(url).build()
        return try {
            val response = client.newCall(request).execute()
            val bytes = response.body?.bytes()
            response.close()
            bytes
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveUrl(base: String, relative: String): String {
        if (relative.startsWith("http://") || relative.startsWith("https://")) return relative
        val baseDir = base.substringBeforeLast('/')
        return "$baseDir/$relative"
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }
}
