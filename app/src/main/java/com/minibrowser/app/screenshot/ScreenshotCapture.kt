package com.minibrowser.app.screenshot

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenshotCapture(private val context: Context) {

    suspend fun captureFullPage(session: GeckoSession): Uri? = withContext(Dispatchers.IO) {
        try {
            val display = context.resources.displayMetrics
            val width = display.widthPixels
            val height = display.heightPixels
            val result = session.capturePixels(android.graphics.Rect(0, 0, width, height))
            val bitmap = result.poll(10000) ?: return@withContext null
            saveBitmapToGallery(bitmap)
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "MiniBrowser_$timestamp.png"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MiniBrowser")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null

        resolver.openOutputStream(uri)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        return uri
    }

    fun shareScreenshot(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享截图").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun showSaved(uri: Uri) {
        Toast.makeText(context, "截图已保存到相册", Toast.LENGTH_SHORT).show()
    }
}
