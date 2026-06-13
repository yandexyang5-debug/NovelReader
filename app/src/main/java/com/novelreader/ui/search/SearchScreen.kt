package com.novelreader.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelreader.data.model.ReadingSettings

data class SearchResult(
    val chapterTitle: String,
    val paragraphText: String,
    val paragraphIndex: Int
)

enum class SearchMode {
    EXACT,      // 精确匹配
    FUZZY,      // 模糊搜索
    MULTI       // 多关键词
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    chapters: List<com.novelreader.data.model.Chapter>,
    fullContent: String,
    onBackClick: () -> Unit,
    onResultClick: (Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(SearchMode.EXACT) }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // 搜索函数
    fun performSearch() {
        if (query.isBlank()) {
            results = emptyList()
            return
        }

        isSearching = true
        results = emptyList()

        val keywords = query.trim().split("\\s+".toRegex())

        for (chapter in chapters) {
            val chapterContent = fullContent.substring(
                chapter.startOffset.coerceAtMost(fullContent.length),
                chapter.endOffset.coerceAtMost(fullContent.length)
            )

            val paragraphs = chapterContent.split("\n")

            for ((index, paragraph) in paragraphs.withIndex()) {
                val trimmedParagraph = paragraph.trim()
                if (trimmedParagraph.isEmpty()) continue

                val isMatch = when (searchMode) {
                    SearchMode.EXACT -> {
                        keywords.all { trimmedParagraph.contains(it, ignoreCase = true) }
                    }
                    SearchMode.FUZZY -> {
                        val pattern = query.trim().lowercase()
                        val text = trimmedParagraph.lowercase()
                        var patternIndex = 0
                        for (char in text) {
                            if (char == pattern[patternIndex]) {
                                patternIndex++
                                if (patternIndex >= pattern.length) break
                            }
                        }
                        patternIndex >= pattern.length
                    }
                    SearchMode.MULTI -> {
                        keywords.all { trimmedParagraph.contains(it, ignoreCase = true) }
                    }
                }

                if (isMatch) {
                    results = results + SearchResult(
                        chapterTitle = chapter.title,
                        paragraphText = trimmedParagraph,
                        paragraphIndex = index
                    )
                }
            }
        }

        isSearching = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("全文搜索") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onBackClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "返回正文",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("输入关键词搜索...") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            // 搜索模式选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = searchMode == SearchMode.EXACT,
                    onClick = {
                        searchMode = SearchMode.EXACT
                        performSearch()
                    },
                    label = { Text("精确匹配") }
                )
                FilterChip(
                    selected = searchMode == SearchMode.FUZZY,
                    onClick = {
                        searchMode = SearchMode.FUZZY
                        performSearch()
                    },
                    label = { Text("模糊搜索") }
                )
                FilterChip(
                    selected = searchMode == SearchMode.MULTI,
                    onClick = {
                        searchMode = SearchMode.MULTI
                        performSearch()
                    },
                    label = { Text("多关键词") }
                )
            }

            // 搜索按钮
            Button(
                onClick = { performSearch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("搜索")
            }

            // 搜索结果统计
            if (results.isNotEmpty()) {
                Text(
                    text = "找到 ${results.size} 条结果",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // 搜索结果列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { result ->
                    SearchResultCard(
                        result = result,
                        query = query,
                        onClick = {
                            // 找到章节索引
                            val chapterIndex = chapters.indexOfFirst { it.title == result.chapterTitle }
                            if (chapterIndex >= 0) {
                                onResultClick(chapterIndex)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    result: SearchResult,
    query: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 章节标题
            Text(
                text = result.chapterTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(ReadingSettings.PRIMARY)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 匹配的文本（高亮显示关键词）
            val annotatedString = buildAnnotatedString {
                val text = result.paragraphText
                val keywords = query.trim().split("\\s+".toRegex())

                var lastIndex = 0
                for (keyword in keywords) {
                    val startIndex = text.indexOf(keyword, ignoreCase = true, startIndex = lastIndex)
                    if (startIndex >= 0) {
                        // 添加关键词前的文本
                        if (startIndex > lastIndex) {
                            append(text.substring(lastIndex, startIndex))
                        }
                        // 添加高亮的关键词
                        withStyle(
                            style = SpanStyle(
                                background = Color.Yellow,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(text.substring(startIndex, startIndex + keyword.length))
                        }
                        lastIndex = startIndex + keyword.length
                    }
                }
                // 添加剩余文本
                if (lastIndex < text.length) {
                    append(text.substring(lastIndex))
                }
            }

            Text(
                text = annotatedString,
                fontSize = 14.sp,
                maxLines = 3,
                color = Color.Gray
            )
        }
    }
}
