package com.minibrowser.app.download

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromVideoType(value: VideoType): String = value.name

    @TypeConverter
    fun toVideoType(value: String): VideoType = VideoType.valueOf(value)

    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String = value.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)
}

@Database(entities = [DownloadTask::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DownloadDatabase : RoomDatabase() {

    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: DownloadDatabase? = null

        fun getInstance(context: Context): DownloadDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DownloadDatabase::class.java,
                    "minibrowser_downloads.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
