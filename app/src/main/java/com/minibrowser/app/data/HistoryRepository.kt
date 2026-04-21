package com.minibrowser.app.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: HistoryDao) {

    val allHistory: Flow<List<HistoryEntity>> = dao.getAll()

    fun search(query: String): Flow<List<HistoryEntity>> = dao.search(query)

    suspend fun recordVisit(url: String, title: String, favicon: String? = null) {
        val existing = dao.findByUrl(url)
        if (existing != null) {
            dao.update(existing.copy(
                title = if (title.isNotBlank()) title else existing.title,
                visitCount = existing.visitCount + 1,
                lastVisitAt = System.currentTimeMillis(),
                favicon = favicon ?: existing.favicon
            ))
        } else {
            dao.insert(HistoryEntity(
                title = title,
                url = url,
                favicon = favicon
            ))
        }
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}
