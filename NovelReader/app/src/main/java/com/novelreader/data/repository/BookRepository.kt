package com.novelreader.data.repository

import com.novelreader.data.local.BookDao
import com.novelreader.data.local.ChapterDao
import com.novelreader.data.model.Book
import com.novelreader.data.model.Chapter
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao
) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()

    suspend fun getBookById(bookId: String): Book? {
        return bookDao.getBookById(bookId)
    }

    suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book)
    }

    suspend fun updateBook(book: Book) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }

    suspend fun deleteBookById(bookId: String) {
        bookDao.deleteBookById(bookId)
    }

    // Chapter operations
    fun getChaptersByBookId(bookId: String): Flow<List<Chapter>> {
        return chapterDao.getChaptersByBookId(bookId)
    }

    suspend fun getChapterByIndex(bookId: String, chapterIndex: Int): Chapter? {
        return chapterDao.getChapterByIndex(bookId, chapterIndex)
    }

    suspend fun getChapterById(chapterId: String): Chapter? {
        return chapterDao.getChapterById(chapterId)
    }

    suspend fun insertChapters(chapters: List<Chapter>) {
        chapterDao.insertChapters(chapters)
    }

    suspend fun deleteChaptersByBookId(bookId: String) {
        chapterDao.deleteChaptersByBookId(bookId)
    }
}
