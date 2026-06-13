package com.novelreader.util

import android.content.Context
import android.net.Uri
import com.novelreader.data.model.Book
import com.novelreader.data.model.Chapter
import java.io.File
import java.io.FileOutputStream

object FileImporter {
    /**
     * 从URI导入TXT文件
     */
    fun importFromUri(context: Context, uri: Uri): Pair<Book, String>? {
        return try {
            // 获取文件名
            val fileName = getFileName(context, uri) ?: "未知书籍"

            // 复制文件到App内部存储
            val internalFile = copyToInternal(context, uri, fileName)
                ?: return null

            // 读取文件内容
            val content = internalFile.readText(Charsets.UTF_8)

            // 提取书名和作者
            val (title, author) = extractMetadata(content, fileName)

            // 创建Book对象
            val book = Book(
                title = title,
                author = author,
                filePath = internalFile.absolutePath,
                fileSize = internalFile.length(),
                chapterCount = 0,
                totalCharCount = content.length
            )

            Pair(book, content)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取文件名
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
        }

        if (fileName == null) {
            fileName = uri.path?.split("/")?.lastOrNull()
        }

        // 移除.txt扩展名
        return fileName?.removeSuffix(".txt")
    }

    /**
     * 复制文件到内部存储
     */
    private fun copyToInternal(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val internalDir = File(context.filesDir, "novels")
            if (!internalDir.exists()) {
                internalDir.mkdirs()
            }

            val internalFile = File(internalDir, "$fileName.txt")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(internalFile).use { output ->
                    input.copyTo(output)
                }
            }

            internalFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从内容中提取元数据
     */
    private fun extractMetadata(content: String, fileName: String): Pair<String, String> {
        var title = fileName
        var author = ""

        // 尝试从前几行提取信息
        val lines = content.split("\n").take(20)

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("书名") || trimmed.startsWith("《")) {
                title = trimmed
                    .removePrefix("书名")
                    .removePrefix("：")
                    .removePrefix(":")
                    .trim()
                    .removeSurrounding("《", "》")
            } else if (trimmed.startsWith("作者") || trimmed.startsWith("著")) {
                author = trimmed
                    .removePrefix("作者")
                    .removePrefix("著")
                    .removePrefix("：")
                    .removePrefix(":")
                    .trim()
            }
        }

        return Pair(title, author)
    }

    /**
     * 解析章节
     */
    fun parseChapters(content: String, bookId: String): List<Chapter> {
        return ChapterParser.parseChapters(content, bookId)
    }

    /**
     * 删除书籍文件
     */
    fun deleteBookFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
