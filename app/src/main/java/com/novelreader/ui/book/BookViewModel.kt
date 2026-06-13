package com.novelreader.ui.book

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.data.local.NovelDatabase
import com.novelreader.data.model.Book
import com.novelreader.data.repository.BookRepository
import com.novelreader.util.FileImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NovelDatabase.getDatabase(application)
    private val repository = BookRepository(database.bookDao(), database.chapterDao())

    val allBooks: StateFlow<List<Book>> = repository.allBooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    fun importBook(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val context = getApplication<Application>()
                val result = FileImporter.importFromUri(context, uri)

                if (result != null) {
                    val (book, content) = result

                    // 插入书籍
                    val bookId = repository.insertBook(book).toString()

                    // 解析并插入章节
                    val chapters = FileImporter.parseChapters(content, bookId)
                    repository.insertChapters(chapters)

                    // 更新书籍章节数
                    repository.updateBook(book.copy(
                        id = bookId,
                        chapterCount = chapters.size
                    ))

                    _importResult.value = ImportResult.Success(bookId)
                } else {
                    _importResult.value = ImportResult.Error("导入失败")
                }
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "未知错误")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            FileImporter.deleteBookFile(context, book.filePath)
            repository.deleteBook(book)
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}

sealed class ImportResult {
    data class Success(val bookId: String) : ImportResult()
    data class Error(val message: String) : ImportResult()
}
