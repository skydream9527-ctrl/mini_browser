package com.minibrowser.app.engine

import java.net.URLEncoder

data class SearchEngine(
    val id: String,
    val name: String,
    val searchUrl: String,
    val homeUrl: String
)

object SearchEngineConfig {

    const val defaultEngineId = "google"

    val builtInEngines = listOf(
        SearchEngine("google", "Google", "https://www.google.com/search?q={query}", "https://www.google.com"),
        SearchEngine("bing", "Bing", "https://www.bing.com/search?q={query}", "https://www.bing.com"),
        SearchEngine("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q={query}", "https://duckduckgo.com"),
        SearchEngine("baidu", "百度", "https://www.baidu.com/s?wd={query}", "https://www.baidu.com"),
        SearchEngine("yahoo", "Yahoo", "https://search.yahoo.com/search?p={query}", "https://search.yahoo.com"),
        SearchEngine("yandex", "Yandex", "https://yandex.com/search/?text={query}", "https://yandex.com"),
        SearchEngine("sogou", "搜狗", "https://www.sogou.com/web?query={query}", "https://www.sogou.com"),
        SearchEngine("so360", "360搜索", "https://www.so.com/s?q={query}", "https://www.so.com")
    )

    fun findById(id: String): SearchEngine? = builtInEngines.find { it.id == id }

    fun buildSearchUrl(engine: SearchEngine, query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8").replace("%20", "+")
        return engine.searchUrl.replace("{query}", encoded)
    }
}
