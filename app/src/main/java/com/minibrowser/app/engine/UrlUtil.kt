package com.minibrowser.app.engine

object UrlUtil {

    private val urlPattern = Regex(
        "^(https?://)?[\\w\\-]+(\\.[\\w\\-]+)+(/\\S*)?$",
        RegexOption.IGNORE_CASE
    )

    fun isUrl(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.contains(" ")) return false
        return urlPattern.matches(trimmed)
    }

    fun smartUrl(input: String): String {
        val trimmed = input.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    fun resolveInput(input: String, engine: SearchEngine): String {
        val trimmed = input.trim()
        return if (isUrl(trimmed)) {
            smartUrl(trimmed)
        } else {
            SearchEngineConfig.buildSearchUrl(engine, trimmed)
        }
    }
}
