package com.minibrowser.app.sniffer

import android.util.Log
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.WebExtension

class WebExtensionManager(
    private val runtime: GeckoRuntime,
    private val videoSniffer: VideoSniffer
) {
    companion object {
        private const val TAG = "WebExtManager"
        private const val EXTENSION_ID = "sniffer@minibrowser.app"
        private const val EXTENSION_URL = "resource://android/assets/sniffer-extension/"
    }

    fun install() {
        runtime.webExtensionController
            .ensureBuiltIn(EXTENSION_URL, EXTENSION_ID)
            .then { extension ->
                if (extension != null) {
                    setupMessageDelegate(extension)
                    Log.d(TAG, "Sniffer extension installed")
                }
                null
            }
    }

    private fun setupMessageDelegate(extension: WebExtension) {
        val messageDelegate = object : WebExtension.MessageDelegate {
            override fun onMessage(
                nativeApp: String,
                message: Any,
                sender: WebExtension.MessageSender
            ): org.mozilla.geckoview.GeckoResult<Any>? {
                try {
                    val json = when (message) {
                        is JSONObject -> message
                        is String -> JSONObject(message)
                        else -> return null
                    }

                    if (json.optString("type") == "video_sniffed") {
                        val video = SniffedVideo(
                            url = json.getString("url"),
                            videoType = SniffedVideo.VideoType.fromString(
                                json.optString("videoType", "OTHER")
                            ),
                            source = json.optString("source", "unknown"),
                            pageUrl = json.optString("pageUrl", ""),
                            pageTitle = json.optString("pageTitle", "")
                        )
                        videoSniffer.addVideo(video)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing message", e)
                }
                return null
            }
        }

        extension.setMessageDelegate(messageDelegate, "browser")
    }
}
