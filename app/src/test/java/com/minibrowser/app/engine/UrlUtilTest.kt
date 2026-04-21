package com.minibrowser.app.engine

import org.junit.Assert.*
import org.junit.Test

class UrlUtilTest {

    @Test
    fun `url with https protocol is detected as URL`() {
        assertTrue(UrlUtil.isUrl("https://example.com"))
    }

    @Test
    fun `url with http protocol is detected as URL`() {
        assertTrue(UrlUtil.isUrl("http://example.com"))
    }

    @Test
    fun `domain with dot and no spaces is detected as URL`() {
        assertTrue(UrlUtil.isUrl("example.com"))
    }

    @Test
    fun `domain with path is detected as URL`() {
        assertTrue(UrlUtil.isUrl("example.com/path"))
    }

    @Test
    fun `plain text without dot is not URL`() {
        assertFalse(UrlUtil.isUrl("hello world"))
    }

    @Test
    fun `text with dot and spaces is not URL`() {
        assertFalse(UrlUtil.isUrl("hello world.foo bar"))
    }

    @Test
    fun `single word is not URL`() {
        assertFalse(UrlUtil.isUrl("hello"))
    }

    @Test
    fun `smartUrl adds https to bare domain`() {
        assertEquals("https://example.com", UrlUtil.smartUrl("example.com"))
    }

    @Test
    fun `smartUrl preserves existing protocol`() {
        assertEquals("https://example.com", UrlUtil.smartUrl("https://example.com"))
    }

    @Test
    fun `smartUrl trims whitespace`() {
        assertEquals("https://example.com", UrlUtil.smartUrl("  example.com  "))
    }

    @Test
    fun `resolveInput returns url for domain input`() {
        val google = SearchEngineConfig.builtInEngines.first { it.id == "google" }
        val result = UrlUtil.resolveInput("example.com", google)
        assertEquals("https://example.com", result)
    }

    @Test
    fun `resolveInput returns search url for query input`() {
        val google = SearchEngineConfig.builtInEngines.first { it.id == "google" }
        val result = UrlUtil.resolveInput("hello world", google)
        assertEquals("https://www.google.com/search?q=hello+world", result)
    }
}
