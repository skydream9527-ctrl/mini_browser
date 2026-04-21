package com.minibrowser.app.data

import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val dao: BookmarkDao) {

    val allBookmarks: Flow<List<BookmarkEntity>> = dao.getAll()

    fun search(query: String): Flow<List<BookmarkEntity>> = dao.search(query)

    fun isBookmarked(url: String): Flow<Boolean> = dao.isBookmarked(url)

    suspend fun add(title: String, url: String, favicon: String? = null): Boolean {
        val existing = dao.findByUrl(url)
        if (existing != null) return false
        dao.insert(BookmarkEntity(title = title, url = url, favicon = favicon))
        return true
    }

    suspend fun remove(url: String) {
        val bookmark = dao.findByUrl(url) ?: return
        dao.delete(bookmark)
    }

    suspend fun update(bookmark: BookmarkEntity) {
        dao.update(bookmark)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}
