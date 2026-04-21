package com.minibrowser.app.engine

import org.junit.Assert.*
import org.junit.Test

class SearchEngineConfigTest {

    @Test
    fun `builtInEngines contains 8 engines`() {
        assertEquals(8, SearchEngineConfig.builtInEngines.size)
    }

    @Test
    fun `default engine is google`() {
        assertEquals("google", SearchEngineConfig.defaultEngineId)
    }

    @Test
    fun `google search url contains query placeholder`() {
        val google = SearchEngineConfig.builtInEngines.first { it.id == "google" }
        assertTrue(google.searchUrl.contains("{query}"))
    }

    @Test
    fun `buildSearchUrl replaces query placeholder`() {
        val google = SearchEngineConfig.builtInEngines.first { it.id == "google" }
        val url = SearchEngineConfig.buildSearchUrl(google, "hello world")
        assertEquals("https://www.google.com/search?q=hello+world", url)
    }

    @Test
    fun `findById returns correct engine`() {
        val engine = SearchEngineConfig.findById("baidu")
        assertNotNull(engine)
        assertEquals("百度", engine!!.name)
    }

    @Test
    fun `findById returns null for unknown id`() {
        assertNull(SearchEngineConfig.findById("nonexistent"))
    }
}
