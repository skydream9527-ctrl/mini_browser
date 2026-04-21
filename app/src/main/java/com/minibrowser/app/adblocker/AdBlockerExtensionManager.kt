package com.minibrowser.app.adblocker

import android.util.Log
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.WebExtension

class AdBlockerExtensionManager(
    private val runtime: GeckoRuntime,
    private val adBlocker: AdBlocker
) {
    companion object {
        private const val TAG = "AdBlockExt"
        private const val EXTENSION_ID = "adblocker@minibrowser.app"
        private const val EXTENSION_URL = "resource://android/assets/adblocker-extension/"
    }

    fun install() {
        runtime.webExtensionController
            .ensureBuiltIn(EXTENSION_URL, EXTENSION_ID)
            .then<Void> { extension ->
                if (extension != null) {
                    setupMessageDelegate(extension)
                    Log.d(TAG, "Ad blocker extension installed")
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

                    if (json.optString("type") == "ad_blocked") {
                        val url = json.optString("url", "")
                        val count = json.optInt("count", 0)
                        adBlocker.onAdBlocked(url, count)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing ad block message", e)
                }
                return null
            }
        }

        extension.setMessageDelegate(messageDelegate, "browser")
    }
}
