package com.novelreader.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novelreader.data.model.ReadingSettings
import com.novelreader.ui.search.SearchScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    onBackClick: () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    val book by viewModel.currentBook.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsState()
    val currentContent by viewModel.currentChapterContent.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isMenuVisible by viewModel.isMenuVisible.collectAsState()
    val isTOCVisible by viewModel.isTOCVisible.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()
    val isSettingsVisible by viewModel.isSettingsVisible.collectAsState()

    // 加载书籍
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(settings.backgroundColor))
    ) {
        // 内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    viewModel.toggleMenu()
                }
        ) {
            // 章节标题
            if (chapters.isNotEmpty()) {
                Text(
                    text = chapters[currentChapterIndex].title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(ReadingSettings.PRIMARY),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textAlign = TextAlign.Center
                )

                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(ReadingSettings.PRIMARY)
                )
            }

            // 正文内容 - 使用key强制章节切换时重置滚动位置
            key(currentChapterIndex) {
                Text(
                    text = currentContent,
                    fontSize = settings.fontSize.sp,
                    lineHeight = (settings.fontSize * settings.lineHeight).sp,
                    letterSpacing = settings.letterSpacing.sp,
                    color = Color(settings.textColor),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                )
            }
        }

        // 顶部菜单栏
        AnimatedVisibility(
            visible = isMenuVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = { Text(book?.displayTitle ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }

        // 底部菜单栏
        AnimatedVisibility(
            visible = isMenuVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomAppBar(
                containerColor = Color.Black.copy(alpha = 0.7f),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 目录按钮
                    IconButton(onClick = { viewModel.showTOC() }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "目录"
                            )
                            Text("目录", fontSize = 12.sp)
                        }
                    }

                    // 搜索按钮
                    IconButton(onClick = { viewModel.showSearch() }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索"
                            )
                            Text("搜索", fontSize = 12.sp)
                        }
                    }

                    // 夜间模式按钮
                    IconButton(onClick = { viewModel.toggleNightMode() }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (settings.isNightMode) "☀️" else "🌙", fontSize = 20.sp)
                            Text("夜间", fontSize = 12.sp)
                        }
                    }

                    // 设置按钮
                    IconButton(onClick = { viewModel.showSettings() }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置"
                            )
                            Text("设置", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 章节导航
        AnimatedVisibility(
            visible = isMenuVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 进度条滑块
                if (chapters.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = currentChapterIndex.toFloat(),
                            onValueChange = { viewModel.goToChapter(it.toInt()) },
                            valueRange = 0f..(chapters.size - 1).coerceAtLeast(1).toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(ReadingSettings.PRIMARY),
                                inactiveTrackColor = Color.Gray
                            )
                        )
                        Text(
                            text = "${chapters.size}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                // 上一章/下一章
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.goToPreviousChapter() },
                        enabled = currentChapterIndex > 0
                    ) {
                        Text("‹ 上一章", color = Color.White)
                    }

                    Text(
                        text = "${currentChapterIndex + 1}/${chapters.size}",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    TextButton(
                        onClick = { viewModel.goToNextChapter() },
                        enabled = currentChapterIndex < chapters.size - 1
                    ) {
                        Text("下一章 ›", color = Color.White)
                    }
                }
            }
        }
    }

    // 目录弹窗
    if (isTOCVisible) {
        TOCDialog(
            chapters = chapters,
            currentIndex = currentChapterIndex,
            onChapterClick = { index ->
                viewModel.goToChapter(index)
                viewModel.hideTOC()
            },
            onDismiss = { viewModel.hideTOC() }
        )
    }

    // 搜索界面
    if (isSearchVisible) {
        SearchScreen(
            chapters = chapters,
            fullContent = viewModel.fullContent,
            onBackClick = { viewModel.hideSearch() },
            onResultClick = { index ->
                viewModel.goToChapter(index)
                viewModel.hideSearch()
            }
        )
    }

    // 设置弹窗
    if (isSettingsVisible) {
        SettingsDialog(
            settings = settings,
            onSettingsChange = { viewModel.updateSettings(it) },
            onDismiss = { viewModel.hideSettings() }
        )
    }
}

@Composable
fun TOCDialog(
    chapters: List<com.novelreader.data.model.Chapter>,
    currentIndex: Int,
    onChapterClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val filteredChapters = remember(chapters, searchQuery) {
        if (searchQuery.isBlank()) {
            chapters.mapIndexed { index, chapter -> index to chapter }
        } else {
            chapters.mapIndexed { index, chapter -> index to chapter }
                .filter { it.second.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    // 打开目录时自动滚动到当前章节位置
    LaunchedEffect(currentIndex, searchQuery) {
        if (searchQuery.isBlank()) {
            // 找到当前章节在列表中的位置并滚动
            val targetIndex = filteredChapters.indexOfFirst { it.first == currentIndex }
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("目录") },
        text = {
            Column(
                modifier = Modifier.fillMaxHeight(0.7f)
            ) {
                // 搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("搜索章节...") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "清除"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                )

                // 章节列表（带滚动条）
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(filteredChapters) { _, (originalIndex, chapter) ->
                            Text(
                                text = chapter.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onChapterClick(originalIndex)
                                    }
                                    .background(
                                        if (originalIndex == currentIndex)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            Color.Transparent
                                    )
                                    .padding(12.dp),
                                color = if (originalIndex == currentIndex)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 滚动条指示器
                    val scrollbarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    if (listState.layoutInfo.totalItemsCount > 0) {
                        val firstVisibleIndex = listState.firstVisibleItemIndex
                        val visibleItemsCount = listState.layoutInfo.visibleItemsInfo.size
                        val totalItems = listState.layoutInfo.totalItemsCount

                        if (totalItems > visibleItemsCount) {
                            val scrollbarHeightFraction = visibleItemsCount.toFloat() / totalItems
                            val scrollbarTopFraction = firstVisibleIndex.toFloat() / totalItems

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .background(scrollbarColor.copy(alpha = 0.2f))
                                    .align(Alignment.CenterEnd)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(scrollbarHeightFraction)
                                    .width(4.dp)
                                    .background(scrollbarColor)
                                    .align(Alignment.CenterEnd)
                                    .graphicsLayer {
                                        translationY = size.height * scrollbarTopFraction
                                    }
                            )
                        }
                    }

                    // 返回顶部按钮
                    if (filteredChapters.size > 10) {
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .size(40.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "返回顶部",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
fun SettingsDialog(
    settings: ReadingSettings,
    onSettingsChange: (ReadingSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置") },
        text = {
            Column {
                // 字体大小
                Text("字体大小: ${settings.fontSize.toInt()}sp")
                Slider(
                    value = settings.fontSize,
                    onValueChange = { onSettingsChange(settings.copy(fontSize = it)) },
                    valueRange = 12f..36f,
                    steps = 23
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 行间距
                Text("行间距: ${String.format("%.1f", settings.lineHeight)}")
                Slider(
                    value = settings.lineHeight,
                    onValueChange = { onSettingsChange(settings.copy(lineHeight = it)) },
                    valueRange = 1.0f..4.0f,
                    steps = 29
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 字间距
                Text("字间距: ${String.format("%.1f", settings.letterSpacing)}sp")
                Slider(
                    value = settings.letterSpacing,
                    onValueChange = { onSettingsChange(settings.copy(letterSpacing = it)) },
                    valueRange = 0f..5f,
                    steps = 9
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 段间距
                Text("段间距: ${String.format("%.1f", settings.paragraphSpacing / 12)}")
                Slider(
                    value = settings.paragraphSpacing,
                    onValueChange = { onSettingsChange(settings.copy(paragraphSpacing = it)) },
                    valueRange = 0f..24f,
                    steps = 11
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 背景颜色
                Text("背景颜色")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ColorButton(
                        color = Color(ReadingSettings.BG_WHITE),
                        isSelected = settings.backgroundColor == ReadingSettings.BG_WHITE,
                        onClick = { onSettingsChange(settings.copy(backgroundColor = ReadingSettings.BG_WHITE)) }
                    )
                    ColorButton(
                        color = Color(ReadingSettings.BG_CREAM),
                        isSelected = settings.backgroundColor == ReadingSettings.BG_CREAM,
                        onClick = { onSettingsChange(settings.copy(backgroundColor = ReadingSettings.BG_CREAM)) }
                    )
                    ColorButton(
                        color = Color(ReadingSettings.BG_GREEN),
                        isSelected = settings.backgroundColor == ReadingSettings.BG_GREEN,
                        onClick = { onSettingsChange(settings.copy(backgroundColor = ReadingSettings.BG_GREEN)) }
                    )
                    ColorButton(
                        color = Color(ReadingSettings.BG_DARK),
                        isSelected = settings.backgroundColor == ReadingSettings.BG_DARK,
                        onClick = { onSettingsChange(settings.copy(backgroundColor = ReadingSettings.BG_DARK)) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}

@Composable
fun ColorButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, MaterialTheme.shapes.small)
            .clickable { onClick() }
            .then(
                if (isSelected)
                    Modifier.padding(2.dp)
                else
                    Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Text("✓", color = if (color == Color.Black) Color.White else Color.Black)
        }
    }
}
