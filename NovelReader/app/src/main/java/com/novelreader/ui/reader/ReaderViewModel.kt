package com.novelreader.ui.reader

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novelreader.data.local.NovelDatabase
import com.novelreader.data.model.Book
import com.novelreader.data.model.Chapter
import com.novelreader.data.model.PageMode
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

    // SharedPreferences 用于保存阅读设置
    private val prefs: SharedPreferences = application.getSharedPreferences("reading_settings", Context.MODE_PRIVATE)

    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()

    private val _currentChapterContent = MutableStateFlow("")
    val currentChapterContent: StateFlow<String> = _currentChapterContent.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ReadingSettings> = _settings.asStateFlow()

    private val _isMenuVisible = MutableStateFlow(false)
    val isMenuVisible: StateFlow<Boolean> = _isMenuVisible.asStateFlow()

    private val _isTOCVisible = MutableStateFlow(false)
    val isTOCVisible: StateFlow<Boolean> = _isTOCVisible.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible: StateFlow<Boolean> = _isSettingsVisible.asStateFlow()

    // 章节内阅读位置（页码或段落索引）
    private val _lastReadPosition = MutableStateFlow(0)
    val lastReadPosition: StateFlow<Int> = _lastReadPosition.asStateFlow()

    // 搜索跳转目标段落（-1表示不需要跳转）
    private val _scrollToParagraph = MutableStateFlow(-1)
    val scrollToParagraph: StateFlow<Int> = _scrollToParagraph.asStateFlow()

    var fullContent: String = ""
        private set

    /**
     * 从SharedPreferences加载阅读设置
     */
    private fun loadSettings(): ReadingSettings {
        val pageModeOrdinal = prefs.getInt("pageMode", 0)
        val pageMode = PageMode.entries.getOrElse(pageModeOrdinal) { PageMode.VERTICAL_SCROLL }

        return ReadingSettings(
            fontSize = prefs.getFloat("fontSize", 18f),
            lineHeight = prefs.getFloat("lineHeight", 1.8f),
            letterSpacing = prefs.getFloat("letterSpacing", 0f),
            paragraphSpacing = prefs.getFloat("paragraphSpacing", 12f),
            backgroundColor = prefs.getLong("backgroundColor", 0xFFFFFFFF),
            textColor = prefs.getLong("textColor", 0xFF333333),
            isNightMode = prefs.getBoolean("isNightMode", false),
            pageMode = pageMode
        )
    }

    /**
     * 保存阅读设置到SharedPreferences
     */
    private fun saveSettings(settings: ReadingSettings) {
        prefs.edit().apply {
            putFloat("fontSize", settings.fontSize)
            putFloat("lineHeight", settings.lineHeight)
            putFloat("letterSpacing", settings.letterSpacing)
            putFloat("paragraphSpacing", settings.paragraphSpacing)
            putLong("backgroundColor", settings.backgroundColor)
            putLong("textColor", settings.textColor)
            putBoolean("isNightMode", settings.isNightMode)
            putInt("pageMode", settings.pageMode.ordinal)
            apply()
        }
    }

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
                        _lastReadPosition.value = book.lastParagraphIndex
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
            _lastReadPosition.value = 0
            _currentChapterIndex.value = chapterIndex
            loadChapterContent(chapterList[chapterIndex])
            saveProgress()
        }
    }

    /**
     * 跳转到指定章节的指定段落（用于搜索结果跳转）
     */
    fun goToChapterAndParagraph(chapterIndex: Int, paragraphIndex: Int) {
        val chapterList = _chapters.value
        if (chapterIndex in chapterList.indices) {
            _scrollToParagraph.value = paragraphIndex
            _currentChapterIndex.value = chapterIndex
            loadChapterContent(chapterList[chapterIndex])
            saveProgress()
        }
    }

    /**
     * 清除段落跳转标记（跳转完成后调用）
     */
    fun clearScrollToParagraph() {
        _scrollToParagraph.value = -1
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
        val oldSettings = _settings.value
        _settings.value = newSettings
        // 只有设置真正改变时才保存
        if (oldSettings.fontSize != newSettings.fontSize ||
            oldSettings.lineHeight != newSettings.lineHeight ||
            oldSettings.letterSpacing != newSettings.letterSpacing ||
            oldSettings.paragraphSpacing != newSettings.paragraphSpacing ||
            oldSettings.backgroundColor != newSettings.backgroundColor ||
            oldSettings.textColor != newSettings.textColor ||
            oldSettings.isNightMode != newSettings.isNightMode ||
            oldSettings.pageMode != newSettings.pageMode
        ) {
            saveSettings(newSettings)
        }
    }

    /**
     * 更新章节内阅读位置（防抖，避免频繁写入）
     */
    private var lastSaveTime = 0L
    fun updateReadPosition(position: Int) {
        _lastReadPosition.value = position
        // 防抖：至少间隔1秒才保存一次
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSaveTime > 1000) {
            lastSaveTime = currentTime
            saveProgress()
        }
    }

    /**
     * 强制保存阅读位置（退出时调用）
     */
    fun forceSavePosition() {
        lastSaveTime = System.currentTimeMillis()
        saveProgress()
    }

    fun toggleNightMode() {
        val current = _settings.value
        val newSettings = current.copy(
            isNightMode = !current.isNightMode,
            backgroundColor = if (current.isNightMode) ReadingSettings.BG_WHITE else ReadingSettings.NIGHT_BG,
            textColor = if (current.isNightMode) 0xFF333333 else ReadingSettings.NIGHT_TEXT
        )
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    private fun saveProgress() {
        viewModelScope.launch {
            val book = _currentBook.value ?: return@launch
            repository.updateBook(
                book.copy(
                    lastChapterIndex = _currentChapterIndex.value,
                    lastParagraphIndex = _lastReadPosition.value,
                    lastReadAt = System.currentTimeMillis()
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        forceSavePosition()
    }
}
