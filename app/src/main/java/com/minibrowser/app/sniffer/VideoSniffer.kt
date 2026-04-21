package com.minibrowser.app.sniffer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VideoSniffer {

    private val _sniffedVideos = MutableStateFlow<List<SniffedVideo>>(emptyList())
    val sniffedVideos: StateFlow<List<SniffedVideo>> = _sniffedVideos.asStateFlow()

    private val seenUrls = mutableSetOf<String>()

    fun addVideo(video: SniffedVideo) {
        val normalizedUrl = normalizeUrl(video.url)
        if (seenUrls.contains(normalizedUrl)) return
        if (video.videoType == SniffedVideo.VideoType.TS) return

        seenUrls.add(normalizedUrl)
        _sniffedVideos.value = _sniffedVideos.value + video
    }

    fun clear() {
        seenUrls.clear()
        _sniffedVideos.value = emptyList()
    }

    val videoCount: Int
        get() = _sniffedVideos.value.size

    private fun normalizeUrl(url: String): String {
        return url.substringBefore('?').substringBefore('#').lowercase()
    }
}
