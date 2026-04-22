package com.minibrowser.app.translate

import org.mozilla.geckoview.GeckoSession

object PageTranslator {

    val LANGUAGES = listOf(
        "zh-CN" to "中文",
        "en" to "English",
        "ja" to "日本語",
        "ko" to "한국어",
        "fr" to "Français",
        "de" to "Deutsch",
        "es" to "Español",
        "ru" to "Русский"
    )

    fun translatePage(session: GeckoSession, targetLang: String) {
        val js = """
            (function() {
                var url = 'https://translate.google.com/translate?sl=auto&tl=$targetLang&u=' + encodeURIComponent(window.location.href);
                window.location.href = url;
            })();
        """.trimIndent()
        session.loadUri("javascript:$js")
    }

    fun translateViaRedirect(currentUrl: String, targetLang: String): String {
        return "https://translate.google.com/translate?sl=auto&tl=$targetLang&u=${java.net.URLEncoder.encode(currentUrl, "UTF-8")}"
    }
}
