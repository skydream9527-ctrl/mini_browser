package com.minibrowser.app.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

@OptIn(UnstableApi::class)
class VideoPlayerManager(context: Context) {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("MiniBrowser/1.0")

    fun play(url: String) {
        val mediaSource = buildMediaSource(url)
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    private fun buildMediaSource(url: String): MediaSource {
        val mediaItem = MediaItem.fromUri(url)
        return when {
            url.contains(".m3u8", ignoreCase = true) ->
                HlsMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            url.contains(".mpd", ignoreCase = true) ->
                DashMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            else ->
                ProgressiveMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
        }
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    fun release() {
        player.release()
    }
}
