package com.minibrowser.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {

    @Query("SELECT * FROM shortcuts ORDER BY position ASC")
    fun getAll(): Flow<List<ShortcutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shortcut: ShortcutEntity): Long

    @Update
    suspend fun update(shortcut: ShortcutEntity)

    @Delete
    suspend fun delete(shortcut: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM shortcuts")
    suspend fun count(): Int
}
