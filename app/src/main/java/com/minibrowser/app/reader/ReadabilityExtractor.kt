package com.minibrowser.app.reader

data class ArticleContent(
    val title: String,
    val content: String,
    val textContent: String,
    val siteName: String = "",
    val excerpt: String = ""
)

object ReadabilityExtractor {

    val extractionScript = """
        (function() {
            function getMetaContent(name) {
                var el = document.querySelector('meta[property="' + name + '"]') ||
                         document.querySelector('meta[name="' + name + '"]');
                return el ? el.getAttribute('content') || '' : '';
            }

            var title = document.title || '';
            var ogTitle = getMetaContent('og:title');
            if (ogTitle) title = ogTitle;

            var siteName = getMetaContent('og:site_name');
            var excerpt = getMetaContent('description') || getMetaContent('og:description');

            var article = document.querySelector('article') ||
                         document.querySelector('[role="main"]') ||
                         document.querySelector('.post-content') ||
                         document.querySelector('.article-content') ||
                         document.querySelector('.entry-content') ||
                         document.querySelector('#content') ||
                         document.querySelector('main');

            var content = '';
            if (article) {
                var clone = article.cloneNode(true);
                var removes = clone.querySelectorAll('script,style,nav,header,footer,aside,.ad,.ads,.advertisement,.social-share,.comments,iframe');
                for (var i = 0; i < removes.length; i++) removes[i].remove();
                content = clone.innerHTML;
            } else {
                var ps = document.querySelectorAll('p');
                var texts = [];
                for (var j = 0; j < ps.length; j++) {
                    var t = ps[j].textContent.trim();
                    if (t.length > 40) texts.push('<p>' + ps[j].innerHTML + '</p>');
                }
                content = texts.join('');
            }

            return JSON.stringify({
                title: title,
                content: content,
                siteName: siteName,
                excerpt: excerpt
            });
        })();
    """.trimIndent()
}
