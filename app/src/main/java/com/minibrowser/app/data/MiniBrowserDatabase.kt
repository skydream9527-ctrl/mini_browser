package com.minibrowser.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.minibrowser.app.download.DownloadDao
import com.minibrowser.app.download.DownloadStatus
import com.minibrowser.app.download.DownloadTask
import com.minibrowser.app.download.VideoType

class MiniBrowserConverters {
    @TypeConverter
    fun fromVideoType(value: VideoType): String = value.name

    @TypeConverter
    fun toVideoType(value: String): VideoType = VideoType.valueOf(value)

    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String = value.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)
}

@Database(
    entities = [DownloadTask::class, BookmarkEntity::class, HistoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(MiniBrowserConverters::class)
abstract class MiniBrowserDatabase : RoomDatabase() {

    abstract fun downloadDao(): DownloadDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: MiniBrowserDatabase? = null

        fun getInstance(context: Context): MiniBrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MiniBrowserDatabase::class.java,
                    "minibrowser.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
