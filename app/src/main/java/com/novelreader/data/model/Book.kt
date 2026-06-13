package com.novelreader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "books")
data class Book(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String = "",
    val filePath: String,
    val fileSize: Long,
    val importedAt: Long = System.currentTimeMillis(),
    var lastReadAt: Long? = null,
    var chapterCount: Int = 0,
    var totalCharCount: Int = 0,
    var lastChapterIndex: Int = 0,
    var lastParagraphIndex: Int = 0
) {
    val displayTitle: String
        get() = if (title.isEmpty()) "未知书籍" else title

    val displayAuthor: String
        get() = if (author.isEmpty()) "未知作者" else author
}
