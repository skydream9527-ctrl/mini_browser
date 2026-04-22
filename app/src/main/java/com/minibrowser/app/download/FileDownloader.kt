package com.minibrowser.app.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast

class FileDownloader(private val context: Context) {

    fun download(url: String, filename: String? = null) {
        val uri = Uri.parse(url)
        val guessedFilename = filename ?: guessFilename(url)

        val request = DownloadManager.Request(uri).apply {
            setTitle(guessedFilename)
            setDescription("MiniBrowser 下载")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, guessedFilename)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)

            val mimeType = guessMimeType(url)
            if (mimeType != null) {
                setMimeType(mimeType)
            }
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "开始下载: $guessedFilename", Toast.LENGTH_SHORT).show()
    }

    private fun guessFilename(url: String): String {
        val path = Uri.parse(url).lastPathSegment ?: "download"
        val cleaned = path.substringBefore('?').substringBefore('#')
        return if (cleaned.contains('.')) cleaned else "$cleaned.bin"
    }

    private fun guessMimeType(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return if (extension != null) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } else null
    }

    companion object {
        val DOWNLOAD_EXTENSIONS = setOf(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "zip", "rar", "7z", "tar", "gz",
            "apk", "exe", "dmg",
            "jpg", "jpeg", "png", "gif", "webp", "svg",
            "mp3", "wav", "flac", "aac", "ogg",
            "mp4", "mkv", "avi", "mov", "webm", "flv",
            "txt", "csv", "json", "xml"
        )

        fun shouldDownload(url: String): Boolean {
            val ext = url.substringAfterLast('.').substringBefore('?').lowercase()
            return ext in DOWNLOAD_EXTENSIONS
        }
    }
}
