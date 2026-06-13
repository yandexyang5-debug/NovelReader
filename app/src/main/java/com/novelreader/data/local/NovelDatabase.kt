package com.novelreader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.novelreader.data.model.Book
import com.novelreader.data.model.Chapter

@Database(
    entities = [Book::class, Chapter::class],
    version = 1,
    exportSchema = false
)
abstract class NovelDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao

    companion object {
        @Volatile
        private var INSTANCE: NovelDatabase? = null

        fun getDatabase(context: Context): NovelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovelDatabase::class.java,
                    "novel_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
