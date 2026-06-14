package com.novelreader.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class Chapter(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val index: Int,
    val title: String,
    val startOffset: Int,
    val endOffset: Int,
    val charCount: Int
)
