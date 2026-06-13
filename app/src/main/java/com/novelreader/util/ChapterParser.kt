package com.novelreader.util

import com.novelreader.data.model.Chapter

object ChapterParser {
    // 章节标题正则模式
    private val chapterPatterns = listOf(
        "^第[一二三四五六七八九十百千万零壹贰叁肆伍陆柒捌玖拾佰仟\\d]+章\\s*.+".toRegex(),
        "^第[一二三四五六七八九十百千万零壹贰叁肆伍陆柒捌玖拾佰仟\\d]+回\\s*.+".toRegex(),
        "^第[一二三四五六七八九十百千万零壹贰叁肆伍陆柒捌玖拾佰仟\\d]+节\\s*.+".toRegex(),
        "^卷[一二三四五六七八九十百千万零壹贰叁肆伍陆柒捌玖拾佰仟\\d]+\\s*.*".toRegex(),
        "^楔子$".toRegex(),
        "^正文$".toRegex(),
        "^序$".toRegex(),
        "^序章$".toRegex(),
        "^番外$".toRegex(),
        "^尾声$".toRegex(),
        "^后记$".toRegex(),
        "^引子$".toRegex()
    )

    /**
     * 解析文本中的章节
     * @param text 完整的小说文本
     * @param bookId 书籍ID
     * @return 章节列表
     */
    fun parseChapters(text: String, bookId: String): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val lines = text.split("\n")

        var currentOffset = 0

        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (isChapterTitle(trimmed)) {
                val chapter = Chapter(
                    bookId = bookId,
                    index = chapters.size,
                    title = trimmed,
                    startOffset = currentOffset,
                    endOffset = 0, // 稍后计算
                    charCount = 0   // 稍后计算
                )
                chapters.add(chapter)
            }
            currentOffset += line.length + 1 // 字符数 + 换行符
        }

        // 计算每个章节的结束偏移和字符数
        calculateChapterBounds(chapters, text)

        return chapters
    }

    /**
     * 判断是否是章节标题
     */
    private fun isChapterTitle(text: String): Boolean {
        if (text.isEmpty()) return false

        for (pattern in chapterPatterns) {
            if (pattern.matches(text)) {
                return true
            }
        }

        return false
    }

    /**
     * 计算每个章节的结束偏移和字符数
     */
    private fun calculateChapterBounds(chapters: MutableList<Chapter>, text: String) {
        for (i in chapters.indices) {
            val chapter = chapters[i]
            val nextStartOffset = if (i < chapters.size - 1) {
                chapters[i + 1].startOffset
            } else {
                text.length
            }

            chapters[i] = chapter.copy(
                endOffset = nextStartOffset,
                charCount = nextStartOffset - chapter.startOffset
            )
        }
    }

    /**
     * 获取章节内容
     */
    fun getChapterContent(text: String, chapter: Chapter): String {
        val startIndex = chapter.startOffset.coerceAtMost(text.length)
        val endIndex = chapter.endOffset.coerceAtMost(text.length)

        return if (startIndex < endIndex) {
            text.substring(startIndex, endIndex)
        } else {
            ""
        }
    }
}
