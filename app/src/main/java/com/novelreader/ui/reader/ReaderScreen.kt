package com.novelreader.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novelreader.data.model.PageMode
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
    val lastReadPosition by viewModel.lastReadPosition.collectAsState()

    // 加载书籍
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(settings.backgroundColor))
    ) {
        // 内容区域 - 根据翻页模式处理点击事件
        val contentModifier = if (settings.pageMode == PageMode.HORIZONTAL_PAGE) {
            // 左右翻页模式：不在此处处理点击，由翻页模块处理
            Modifier.fillMaxSize()
        } else {
            // 上下滚屏模式：点击弹出菜单
            Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    viewModel.toggleMenu()
                }
        }

        Column(
            modifier = contentModifier
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

            // 正文内容 - 根据翻页方式显示
            key(currentChapterIndex) {
                val paragraphs = remember(currentContent) {
                    currentContent.split("\n").filter { it.isNotBlank() }
                }

                when (settings.pageMode) {
                    PageMode.VERTICAL_SCROLL -> {
                        // 上下滚屏模式
                        val listState = rememberLazyListState()

                        // 恢复滚动位置
                        LaunchedEffect(currentChapterIndex, lastReadPosition) {
                            if (lastReadPosition > 0) {
                                listState.scrollToItem(lastReadPosition)
                            }
                        }

                        // 使用 snapshotFlow 监听滚动，防抖保存
                        LaunchedEffect(listState) {
                            snapshotFlow { listState.firstVisibleItemIndex }
                                .collect { index ->
                                    viewModel.updateReadPosition(index)
                                }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            itemsIndexed(
                                items = paragraphs,
                                key = { index, _ -> index }
                            ) { _, paragraph ->
                                Text(
                                    text = paragraph,
                                    fontSize = settings.fontSize.sp,
                                    lineHeight = (settings.fontSize * settings.lineHeight).sp,
                                    letterSpacing = settings.letterSpacing.sp,
                                    color = Color(settings.textColor),
                                    modifier = Modifier.padding(bottom = settings.paragraphSpacing.dp)
                                )
                            }
                        }
                    }

                    PageMode.HORIZONTAL_PAGE -> {
                        // 左右翻页模式
                        var currentPage by remember { mutableStateOf(lastReadPosition) }

                        // 计算每页显示的段落数
                        val itemsPerPage = remember(settings.fontSize, settings.lineHeight) {
                            val lineHeight = settings.fontSize * settings.lineHeight
                            val estimatedLinesPerPage = (600f / lineHeight).toInt()
                            estimatedLinesPerPage.coerceIn(5, 50)
                        }

                        // 将段落分页
                        val pages = remember(paragraphs, itemsPerPage) {
                            paragraphs.chunked(itemsPerPage)
                        }

                        // 确保页码在有效范围内
                        LaunchedEffect(pages.size) {
                            if (pages.isNotEmpty() && currentPage >= pages.size) {
                                currentPage = (pages.size - 1).coerceAtLeast(0)
                            }
                        }

                        val displayParagraphs = pages.getOrElse(currentPage) { paragraphs }

                        Box(modifier = Modifier.fillMaxSize()) {
                            // 内容区域 - 支持滑动和点击翻页
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures(
                                            onDragEnd = { },
                                            onHorizontalDrag = { change, dragAmount ->
                                                change.consume()
                                                if (dragAmount < -50) {
                                                    if (currentPage < pages.size - 1) {
                                                        currentPage++
                                                        viewModel.updateReadPosition(currentPage)
                                                    }
                                                } else if (dragAmount > 50) {
                                                    if (currentPage > 0) {
                                                        currentPage--
                                                        viewModel.updateReadPosition(currentPage)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        // 点击由外层Box的pointerInput处理
                                    }
                                    .padding(16.dp)
                            ) {
                                displayParagraphs.forEach { paragraph ->
                                    Text(
                                        text = paragraph,
                                        fontSize = settings.fontSize.sp,
                                        lineHeight = (settings.fontSize * settings.lineHeight).sp,
                                        letterSpacing = settings.letterSpacing.sp,
                                        color = Color(settings.textColor),
                                        modifier = Modifier.padding(bottom = settings.paragraphSpacing.dp)
                                    )
                                }
                            }

                            // 透明覆盖层 - 处理点击区域
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            val screenWidth = size.width.toFloat()
                                            val thirdWidth = screenWidth / 3

                                            when {
                                                offset.x < thirdWidth -> {
                                                    // 左侧1/3：上一页
                                                    if (currentPage > 0) {
                                                        currentPage--
                                                        viewModel.updateReadPosition(currentPage)
                                                    }
                                                }
                                                offset.x > thirdWidth * 2 -> {
                                                    // 右侧1/3：下一页
                                                    if (currentPage < pages.size - 1) {
                                                        currentPage++
                                                        viewModel.updateReadPosition(currentPage)
                                                    }
                                                }
                                                else -> {
                                                    // 中间1/3：弹出菜单
                                                    viewModel.toggleMenu()
                                                }
                                            }
                                        }
                                    }
                            ) {
                                // 透明区域，仅用于捕获点击事件
                            }

                            // 页码指示器
                            Text(
                                text = "${currentPage + 1}/${pages.size}",
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp),
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
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
                title = { Text(chapters.getOrNull(currentChapterIndex)?.title ?: book?.displayTitle ?: "") },
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

                // 章节列表（带可拖拽滚动条）
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

                    // 可拖拽滚动条
                    if (listState.layoutInfo.totalItemsCount > 1) {
                        val totalItems = listState.layoutInfo.totalItemsCount
                        val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset

                        // 计算滚动比例
                        val scrollFraction = remember(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset, totalItems) {
                            if (totalItems <= 1) 0f
                            else {
                                val firstVisible = listState.firstVisibleItemIndex
                                val firstVisibleOffset = listState.firstVisibleItemScrollOffset
                                val itemHeight = if (listState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                                    listState.layoutInfo.visibleItemsInfo.first().size.toFloat()
                                } else {
                                    100f
                                }
                                val scrollPosition = firstVisible + firstVisibleOffset / itemHeight
                                (scrollPosition / (totalItems - 1)).coerceIn(0f, 1f)
                            }
                        }

                        // 滚动条高度比例（可见区域占总内容的比例）
                        val thumbHeightFraction = remember(totalItems, listState.layoutInfo.visibleItemsInfo.size) {
                            val visibleCount = listState.layoutInfo.visibleItemsInfo.size
                            (visibleCount.toFloat() / totalItems).coerceIn(0.1f, 1f)
                        }

                        // 滑块可用移动范围
                        val thumbTrackFraction = 1f - thumbHeightFraction

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(24.dp)
                                .align(Alignment.CenterEnd)
                                .pointerInput(totalItems, thumbTrackFraction) {
                                    detectVerticalDragGestures(
                                        onDragStart = { offset ->
                                            // 根据触摸位置计算目标滚动位置
                                            val touchFraction = (offset.y / size.height).coerceIn(0f, 1f)
                                            val targetScrollFraction = (touchFraction / thumbTrackFraction).coerceIn(0f, 1f)
                                            val targetIndex = (targetScrollFraction * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)
                                            coroutineScope.launch {
                                                listState.scrollToItem(targetIndex)
                                            }
                                        },
                                        onDragEnd = { },
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            // 根据拖拽量计算目标位置
                                            val dragFraction = dragAmount / size.height
                                            val currentFraction = scrollFraction
                                            val newFraction = (currentFraction + dragFraction / thumbTrackFraction).coerceIn(0f, 1f)
                                            val targetIndex = (newFraction * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)
                                            coroutineScope.launch {
                                                listState.scrollToItem(targetIndex)
                                            }
                                        }
                                    )
                                }
                        ) {
                            // 滚动条轨道背景
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                    .align(Alignment.Center)
                            )
                            // 滚动条滑块
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(thumbHeightFraction)
                                    .width(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .align(Alignment.TopCenter)
                                    .graphicsLayer {
                                        // 滑块位置 = 滚动比例 * 可用轨道高度
                                        translationY = scrollFraction * thumbTrackFraction * size.height * (1f / thumbHeightFraction - 1f)
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

@OptIn(ExperimentalMaterial3Api::class)
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
                // 字体大小（带加减按钮）
                Text("字体大小: ${settings.fontSize.toInt()}sp")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newValue = (settings.fontSize - 1f).coerceIn(12f, 36f)
                            onSettingsChange(settings.copy(fontSize = newValue))
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "减小")
                    }
                    Slider(
                        value = settings.fontSize.coerceIn(12f, 36f),
                        onValueChange = { value ->
                            onSettingsChange(settings.copy(fontSize = value.coerceIn(12f, 36f)))
                        },
                        valueRange = 12f..36f,
                        steps = 23,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val newValue = (settings.fontSize + 1f).coerceIn(12f, 36f)
                            onSettingsChange(settings.copy(fontSize = newValue))
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "增大")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 行间距（带加减按钮）
                Text("行间距: ${String.format("%.1f", settings.lineHeight)}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newValue = (settings.lineHeight - 0.1f).coerceIn(1.0f, 4.0f)
                            onSettingsChange(settings.copy(lineHeight = newValue))
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "减小")
                    }
                    Slider(
                        value = settings.lineHeight.coerceIn(1.0f, 4.0f),
                        onValueChange = { value ->
                            onSettingsChange(settings.copy(lineHeight = value.coerceIn(1.0f, 4.0f)))
                        },
                        valueRange = 1.0f..4.0f,
                        steps = 29,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val newValue = (settings.lineHeight + 0.1f).coerceIn(1.0f, 4.0f)
                            onSettingsChange(settings.copy(lineHeight = newValue))
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "增大")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 字间距（带加减按钮）
                Text("字间距: ${String.format("%.1f", settings.letterSpacing)}sp")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newValue = (settings.letterSpacing - 0.5f).coerceIn(0f, 5f)
                            onSettingsChange(settings.copy(letterSpacing = newValue))
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "减小")
                    }
                    Slider(
                        value = settings.letterSpacing.coerceIn(0f, 5f),
                        onValueChange = { value ->
                            onSettingsChange(settings.copy(letterSpacing = value.coerceIn(0f, 5f)))
                        },
                        valueRange = 0f..5f,
                        steps = 9,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val newValue = (settings.letterSpacing + 0.5f).coerceIn(0f, 5f)
                            onSettingsChange(settings.copy(letterSpacing = newValue))
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "增大")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 段间距（带加减按钮）
                Text("段间距: ${String.format("%.1f", settings.paragraphSpacing / 12)}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newValue = (settings.paragraphSpacing - 2f).coerceIn(0f, 24f)
                            onSettingsChange(settings.copy(paragraphSpacing = newValue))
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "减小")
                    }
                    Slider(
                        value = settings.paragraphSpacing.coerceIn(0f, 24f),
                        onValueChange = { value ->
                            onSettingsChange(settings.copy(paragraphSpacing = value.coerceIn(0f, 24f)))
                        },
                        valueRange = 0f..24f,
                        steps = 11,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val newValue = (settings.paragraphSpacing + 2f).coerceIn(0f, 24f)
                            onSettingsChange(settings.copy(paragraphSpacing = newValue))
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "增大")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 翻页方式
                Text("翻页方式")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PageMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.pageMode == mode,
                            onClick = {
                                onSettingsChange(settings.copy(pageMode = mode))
                            },
                            label = { Text(mode.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 背景颜色（删除黑色）
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
