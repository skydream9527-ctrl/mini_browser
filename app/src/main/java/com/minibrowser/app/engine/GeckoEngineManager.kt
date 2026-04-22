package com.minibrowser.app.engine

import android.content.Context
import android.util.Log
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.ContentDelegate
import org.mozilla.geckoview.GeckoSession.NavigationDelegate
import org.mozilla.geckoview.GeckoSession.ProgressDelegate

class GeckoEngineManager private constructor(val runtime: GeckoRuntime) {

    private var session: GeckoSession? = null

    var onTitleChanged: ((String) -> Unit)? = null
    var onUrlChanged: ((String) -> Unit)? = null
    var onProgressChanged: ((Int) -> Unit)? = null
    var onCanGoBackChanged: ((Boolean) -> Unit)? = null
    var onCanGoForwardChanged: ((Boolean) -> Unit)? = null
    var onFullScreenRequest: ((Boolean) -> Unit)? = null

    fun createSession(): GeckoSession {
        session?.close()
        val newSession = GeckoSession()

        newSession.contentDelegate = object : ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                title?.let { onTitleChanged?.invoke(it) }
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                onFullScreenRequest?.invoke(fullScreen)
            }
        }

        newSession.navigationDelegate = object : NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                url?.let { onUrlChanged?.invoke(it) }
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                onCanGoBackChanged?.invoke(canGoBack)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                onCanGoForwardChanged?.invoke(canGoForward)
            }
        }

        newSession.progressDelegate = object : ProgressDelegate {
            override fun onProgressChange(session: GeckoSession, progress: Int) {
                onProgressChanged?.invoke(progress)
            }
        }

        newSession.open(runtime)
        this.session = newSession
        return newSession
    }

    fun loadUrl(url: String) {
        session?.loadUri(url)
    }

    fun goBack() {
        session?.goBack()
    }

    fun goForward() {
        session?.goForward()
    }

    fun reload() {
        session?.reload()
    }

    fun getVideoUrl(callback: (String?) -> Unit) {
        callback(null)
    }

    fun getCurrentSession(): GeckoSession? = session

    fun close() {
        session?.close()
        session = null
    }

    companion object {
        private const val TAG = "GeckoEngine"

        @Volatile
        private var INSTANCE: GeckoEngineManager? = null

        fun getInstance(context: Context): GeckoEngineManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    val settings = GeckoRuntimeSettings.Builder()
                        .javaScriptEnabled(true)
                        .build()
                    val runtime = GeckoRuntime.create(context.applicationContext, settings)
                    GeckoEngineManager(runtime).also { INSTANCE = it }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create GeckoRuntime, retrying with getDefault", e)
                    val runtime = GeckoRuntime.getDefault(context.applicationContext)
                    GeckoEngineManager(runtime).also { INSTANCE = it }
                }
            }
        }
    }
}
