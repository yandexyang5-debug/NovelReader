# 小说阅读器 Android版

一个基于Jetpack Compose的小说阅读APP，支持导入TXT文件并自动生成章节结构。

## ✨ 功能特性

### 📚 书籍管理
- 从手机存储导入TXT文件
- 自动识别章节结构
- 书籍列表展示（网格布局）
- 长按删除书籍

### 📖 阅读体验
- 点击屏幕中央显示/隐藏菜单
- 流畅的滚动阅读
- 章节标题高亮显示
- 段落首行缩进

### 🔍 搜索功能
- 全文搜索
- 精确匹配模式
- 模糊搜索模式
- 多关键词搜索
- 搜索结果高亮显示

### ⚙️ 个性化设置
- 字体大小调节（12-36sp）
- 行间距调节（1.0-4.0倍）
- 字间距调节（0-5sp）
- 背景颜色选择（白色、米色、护眼绿、深色）
- 夜间模式切换

### 💾 阅读进度
- 自动保存阅读进度
- 记录最后阅读章节
- 下次打开自动恢复

## 🛠️ 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **数据库**: Room
- **导航**: Navigation Compose
- **异步**: Kotlin Coroutines + Flow

## 📁 项目结构

```
app/src/main/java/com/novelreader/
├── data/
│   ├── local/          # 数据库相关
│   │   ├── BookDao.kt
│   │   ├── ChapterDao.kt
│   │   └── NovelDatabase.kt
│   ├── model/          # 数据模型
│   │   ├── Book.kt
│   │   ├── Chapter.kt
│   │   └── ReadingSettings.kt
│   └── repository/     # 数据仓库
│       └── BookRepository.kt
├── ui/
│   ├── book/           # 书架界面
│   │   ├── BookListScreen.kt
│   │   └── BookViewModel.kt
│   ├── reader/         # 阅读界面
│   │   ├── ReaderScreen.kt
│   │   └── ReaderViewModel.kt
│   ├── search/         # 搜索界面
│   │   └── SearchScreen.kt
│   ├── settings/       # 设置界面
│   │   └── SettingsScreen.kt
│   └── theme/          # 主题配置
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/               # 工具类
│   ├── ChapterParser.kt
│   └── FileImporter.kt
├── MainActivity.kt
└── NovelReaderApplication.kt
```

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- Kotlin 1.9.20 或更高版本
- Android SDK 34

### 构建步骤

1. **克隆项目**
```bash
cd NovelReader/Android
```

2. **打开项目**
- 使用 Android Studio 打开 `NovelReader/Android` 目录

3. **同步 Gradle**
- 等待 Gradle 同步完成

4. **运行应用**
- 连接 Android 设备或启动模拟器
- 点击 Run 按钮运行应用

## 📱 使用说明

### 导入书籍
1. 点击右下角的 `+` 按钮
2. 选择手机中的TXT文件
3. 等待导入完成
4. 书籍会自动出现在书架上

### 阅读书籍
1. 点击书架上的书籍封面
2. 进入阅读界面
3. 上下滑动翻页

### 显示菜单
1. 点击屏幕中央区域
2. 顶部显示返回按钮和书名
3. 底部显示功能按钮（目录、搜索、夜间、设置）
4. 中间显示章节导航（上一章、下一章）

### 搜索内容
1. 点击底部菜单的"搜索"按钮
2. 输入关键词
3. 选择搜索模式（精确/模糊/多关键词）
4. 点击搜索按钮
5. 点击搜索结果跳转到对应位置

### 调整设置
1. 点击底部菜单的"设置"按钮
2. 调节字体大小、行间距、字间距
3. 选择背景颜色
4. 开启/关闭夜间模式

## 🎨 界面预览

### 书架界面
- 网格布局展示书籍
- 显示书籍标题和作者
- 首字作为封面

### 阅读界面
- 干净的阅读区域
- 流畅的滚动体验
- 自定义字体和间距

### 设置面板
- 直观的滑块调节
- 实时预览效果
- 多种背景颜色可选

## 🔧 开发说明

### 添加新功能
1. 在相应的包中创建新的 Screen 和 ViewModel
2. 在 Navigation 中添加路由
3. 更新相关的数据模型

### 修改主题颜色
编辑 `ui/theme/Color.kt` 文件中的颜色值

### 修改章节解析规则
编辑 `util/ChapterParser.kt` 文件中的正则表达式

## 📝 TODO

- [ ] 书签功能
- [ ] 笔记批注
- [ ] 翻页动画
- [ ] 字体选择
- [ ] 云端同步
- [ ] 语音朗读

## 📄 许可证

本项目仅供学习和个人使用。

## 👥 贡献

欢迎提交 Issue 和 Pull Request！
