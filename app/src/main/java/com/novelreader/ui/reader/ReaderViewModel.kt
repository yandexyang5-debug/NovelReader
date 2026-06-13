package com.novelreader.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.data.local.NovelDatabase
import com.novelreader.data.model.Book
import com.novelreader.data.model.Chapter
import com.novelreader.data.model.ReadingSettings
import com.novelreader.data.repository.BookRepository
import com.novelreader.util.ChapterParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ReaderViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NovelDatabase.getDatabase(application)
    private val repository = BookRepository(database.bookDao(), database.chapterDao())

    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()

    private val _currentChapterContent = MutableStateFlow("")
    val currentChapterContent: StateFlow<String> = _currentChapterContent.asStateFlow()

    private val _settings = MutableStateFlow(ReadingSettings())
    val settings: StateFlow<ReadingSettings> = _settings.asStateFlow()

    private val _isMenuVisible = MutableStateFlow(false)
    val isMenuVisible: StateFlow<Boolean> = _isMenuVisible.asStateFlow()

    private val _isTOCVisible = MutableStateFlow(false)
    val isTOCVisible: StateFlow<Boolean> = _isTOCVisible.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible: StateFlow<Boolean> = _isSettingsVisible.asStateFlow()

    private var fullContent: String = ""

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            _currentBook.value = book

            if (book != null) {
                // 加载章节列表
                repository.getChaptersByBookId(bookId).collect { chapterList ->
                    _chapters.value = chapterList

                    // 加载上次阅读位置
                    val lastIndex = book.lastChapterIndex
                    if (lastIndex in chapterList.indices) {
                        _currentChapterIndex.value = lastIndex
                        loadChapterContent(chapterList[lastIndex])
                    }
                }
            }
        }
    }

    fun loadChapterContent(chapter: Chapter) {
        viewModelScope.launch {
            try {
                val book = _currentBook.value ?: return@launch
                val file = File(book.filePath)
                if (file.exists()) {
                    fullContent = file.readText(Charsets.UTF_8)
                    val content = ChapterParser.getChapterContent(fullContent, chapter)
                    _currentChapterContent.value = content
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _currentChapterContent.value = "加载章节内容失败"
            }
        }
    }

    fun goToChapter(chapterIndex: Int) {
        val chapterList = _chapters.value
        if (chapterIndex in chapterList.indices) {
            _currentChapterIndex.value = chapterIndex
            loadChapterContent(chapterList[chapterIndex])
            saveProgress()
        }
    }

    fun goToNextChapter() {
        val nextIndex = _currentChapterIndex.value + 1
        if (nextIndex < _chapters.value.size) {
            goToChapter(nextIndex)
        }
    }

    fun goToPreviousChapter() {
        val prevIndex = _currentChapterIndex.value - 1
        if (prevIndex >= 0) {
            goToChapter(prevIndex)
        }
    }

    fun toggleMenu() {
        _isMenuVisible.value = !_isMenuVisible.value
    }

    fun hideMenu() {
        _isMenuVisible.value = false
    }

    fun showTOC() {
        _isTOCVisible.value = true
        _isMenuVisible.value = false
    }

    fun hideTOC() {
        _isTOCVisible.value = false
    }

    fun showSearch() {
        _isSearchVisible.value = true
        _isMenuVisible.value = false
    }

    fun hideSearch() {
        _isSearchVisible.value = false
    }

    fun showSettings() {
        _isSettingsVisible.value = true
        _isMenuVisible.value = false
    }

    fun hideSettings() {
        _isSettingsVisible.value = false
    }

    fun updateSettings(newSettings: ReadingSettings) {
        _settings.value = newSettings
    }

    fun toggleNightMode() {
        val current = _settings.value
        _settings.value = current.copy(
            isNightMode = !current.isNightMode,
            backgroundColor = if (current.isNightMode) ReadingSettings.BG_WHITE else ReadingSettings.NIGHT_BG,
            textColor = if (current.isNightMode) 0xFF333333 else ReadingSettings.NIGHT_TEXT
        )
    }

    private fun saveProgress() {
        viewModelScope.launch {
            val book = _currentBook.value ?: return@launch
            repository.updateBook(
                book.copy(
                    lastChapterIndex = _currentChapterIndex.value,
                    lastReadAt = System.currentTimeMillis()
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveProgress()
    }
}
