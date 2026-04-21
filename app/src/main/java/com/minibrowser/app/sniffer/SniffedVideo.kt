package com.minibrowser.app.sniffer

data class SniffedVideo(
    val url: String,
    val videoType: VideoType,
    val source: String,
    val pageUrl: String = "",
    val pageTitle: String = "",
    val estimatedSize: Long? = null
) {
    enum class VideoType {
        MP4, M3U8, DASH, WEBM, FLV, TS, OTHER;

        companion object {
            fun fromString(value: String): VideoType =
                entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }

    val displayName: String
        get() {
            if (pageTitle.isNotBlank()) return pageTitle
            return url.substringAfterLast('/').substringBefore('?').take(50)
        }

    val typeLabel: String
        get() = videoType.name

    val isStreamMedia: Boolean
        get() = videoType == VideoType.M3U8 || videoType == VideoType.DASH
}
