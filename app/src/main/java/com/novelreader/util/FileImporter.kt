package com.novelreader.util

import android.content.Context
import android.net.Uri
import com.novelreader.data.model.Book
import com.novelreader.data.model.Chapter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset

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

            // 读取文件内容（自动检测编码）
            val content = readTextWithEncoding(internalFile)

            // 如果不是UTF-8编码，转换为UTF-8保存
            val bytes = internalFile.readBytes()
            val encoding = detectEncoding(bytes)
            if (encoding != Charsets.UTF_8) {
                // 转换为UTF-8并覆盖保存
                internalFile.writeText(content, Charsets.UTF_8)
            }

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
     * 自动检测编码并读取文本
     */
    private fun readTextWithEncoding(file: File): String {
        val bytes = file.readBytes()

        // 检测BOM（字节顺序标记）
        val encoding = detectEncoding(bytes)

        return String(bytes, encoding)
    }

    /**
     * 检测文件编码
     */
    private fun detectEncoding(bytes: ByteArray): Charset {
        // UTF-8 BOM: EF BB BF
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            return Charsets.UTF_8
        }

        // UTF-16 LE BOM: FF FE
        if (bytes.size >= 2 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xFE.toByte()
        ) {
            return Charsets.UTF_16LE
        }

        // UTF-16 BE BOM: FE FF
        if (bytes.size >= 2 &&
            bytes[0] == 0xFE.toByte() &&
            bytes[1] == 0xFF.toByte()
        ) {
            return Charsets.UTF_16BE
        }

        // 尝试UTF-8解码，检查是否有效
        if (isValidUtf8(bytes)) {
            return Charsets.UTF_8
        }

        // 默认使用GBK（中文系统常见编码）
        return Charset.forName("GBK")
    }

    /**
     * 检查是否是有效的UTF-8编码
     */
    private fun isValidUtf8(bytes: ByteArray): Boolean {
        var i = 0
        var totalBytes = 0
        var validBytes = 0

        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xFF

            when {
                // 单字节 (0xxxxxxx)
                b <= 0x7F -> {
                    totalBytes++
                    validBytes++
                    i++
                }
                // 双字节 (110xxxxx 10xxxxxx)
                b in 0xC0..0xDF -> {
                    if (i + 1 >= bytes.size) return false
                    val b2 = bytes[i + 1].toInt() and 0xFF
                    if (b2 in 0x80..0xBF) {
                        totalBytes += 2
                        validBytes += 2
                        i += 2
                    } else {
                        return false
                    }
                }
                // 三字节 (1110xxxx 10xxxxxx 10xxxxxx) - 中文常用
                b in 0xE0..0xEF -> {
                    if (i + 2 >= bytes.size) return false
                    val b2 = bytes[i + 1].toInt() and 0xFF
                    val b3 = bytes[i + 2].toInt() and 0xFF
                    if (b2 in 0x80..0xBF && b3 in 0x80..0xBF) {
                        totalBytes += 3
                        validBytes += 3
                        i += 3
                    } else {
                        return false
                    }
                }
                // 四字节 (11110xxx 10xxxxxx 10xxxxxx 10xxxxxx)
                b in 0xF0..0xF7 -> {
                    if (i + 3 >= bytes.size) return false
                    val b2 = bytes[i + 1].toInt() and 0xFF
                    val b3 = bytes[i + 2].toInt() and 0xFF
                    val b4 = bytes[i + 3].toInt() and 0xFF
                    if (b2 in 0x80..0xBF && b3 in 0x80..0xBF && b4 in 0x80..0xBF) {
                        totalBytes += 4
                        validBytes += 4
                        i += 4
                    } else {
                        return false
                    }
                }
                else -> {
                    // 无效的UTF-8字节
                    return false
                }
            }
        }

        // 如果大部分字节是有效的UTF-8，则认为是UTF-8
        // 允许少量单字节ASCII混杂
        return totalBytes == validBytes
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
