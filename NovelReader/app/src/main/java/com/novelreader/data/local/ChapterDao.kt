package com.novelreader.data.local

import androidx.room.*
import com.novelreader.data.model.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY `index` ASC")
    fun getChaptersByBookId(bookId: String): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND `index` = :chapterIndex")
    suspend fun getChapterByIndex(bookId: String, chapterIndex: Int): Chapter?

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): Chapter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<Chapter>)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBookId(bookId: String)
}
