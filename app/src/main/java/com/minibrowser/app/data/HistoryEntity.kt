package com.minibrowser.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitCount: Int = 1,
    val lastVisitAt: Long = System.currentTimeMillis(),
    val favicon: String? = null
)
