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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

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

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(ReadingSettings.PRIMARY)
                )
            }

            // 正文内容
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 上一章按钮
                TextButton(
                    onClick = { viewModel.goToPreviousChapter() },
                    enabled = currentChapterIndex > 0
                ) {
                    Text("‹ 上一章", color = Color.White)
                }

                // 章节进度
                Text(
                    text = "${currentChapterIndex + 1}/${chapters.size}",
                    color = Color.White,
                    fontSize = 14.sp
                )

                // 下一章按钮
                TextButton(
                    onClick = { viewModel.goToNextChapter() },
                    enabled = currentChapterIndex < chapters.size - 1
                ) {
                    Text("下一章 ›", color = Color.White)
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("目录") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .verticalScroll(rememberScrollState())
            ) {
                chapters.forEachIndexed { index, chapter ->
                    Text(
                        text = chapter.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChapterClick(index) }
                            .background(
                                if (index == currentIndex)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    Color.Transparent
                            )
                            .padding(12.dp),
                        color = if (index == currentIndex)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
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
