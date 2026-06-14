# Android Studio 安装指南

## 第一步：下载 Android Studio

### 访问官网
打开浏览器，访问：
```
https://developer.android.com/studio
```

### 下载安装包
1. 点击 "Download Android Studio" 按钮
2. 选择 "Windows (64-bit)" 版本
3. 点击 "Download" 开始下载（约1GB）

---

## 第二步：安装 Android Studio

### 运行安装程序
1. 找到下载的文件：`android-studio-xxx.exe`
2. 双击运行
3. 如果出现安全提示，点击 "是"

### 安装向导
1. **欢迎页面** → 点击 "Next"
2. **选择组件** → 保持默认，点击 "Next"
3. **选择安装位置** → 建议保持默认路径，点击 "Next"
4. **选择开始菜单文件夹** → 点击 "Install"
5. 等待安装完成（约5-10分钟）
6. **完成页面** → 勾选 "Start Android Studio"，点击 "Finish"

---

## 第三步：首次配置

### 设置向导
Android Studio 启动后会出现设置向导：

1. **选择安装类型**
   - 选择 "Standard"（标准安装）
   - 点击 "Next"

2. **选择UI主题**
   - 选择你喜欢的主题（Light 或 Darcula）
   - 点击 "Next"

3. **验证设置**
   - 点击 "Finish"
   - 等待SDK下载完成（约10-30分钟，取决于网速）

### 下载组件
设置向导会自动下载：
- Android SDK
- Android SDK Platform-Tools
- Android Emulator
- Intel HAXM（如果支持）

---

## 第四步：打开你的项目

### 打开项目
1. 在 Android Studio 欢迎页面，点击 "Open an Existing Project"
2. 导航到：`c:\Users\Administrator\Desktop\更俗\NovelReader\Android`
3. 选择 `Android` 文件夹
4. 点击 "OK"

### 等待Gradle同步
1. 首次打开项目会自动进行Gradle同步
2. 查看底部状态栏，等待同步完成（约5-10分钟）
3. 如果出现 "Gradle sync finished" 表示成功

---

## 第五步：创建模拟器

### 打开设备管理器
1. 点击顶部菜单栏的设备图标（手机形状）
2. 或者点击 Tools → Device Manager

### 创建新设备
1. 点击 "Create Virtual Device"（或 "+" 按钮）
2. **选择硬件**
   - 选择 "Phone" 类别
   - 选择 "Pixel 6"（或其他设备）
   - 点击 "Next"

3. **选择系统镜像**
   - 选择 "API 33"（Android 13）
   - 下载镜像（如果还没下载，点击下载链接）
   - 等待下载完成
   - 点击 "Next"

4. **配置虚拟设备**
   - AVD Name: 保持默认或自定义
   - 其他设置保持默认
   - 点击 "Finish"

---

## 第六步：运行应用

### 选择模拟器
1. 在顶部工具栏的设备列表中，选择刚创建的模拟器
   （如：Pixel 6 API 33）

### 运行应用
1. 点击绿色三角形按钮（Run 'app'）
2. 等待模拟器启动（首次启动约1-2分钟）
3. 等待应用安装和启动

### 查看效果
1. 模拟器会显示你的应用
2. 可以在模拟器中测试所有功能

---

## 常见问题

### Q1: Gradle同步失败
**解决方案：**
1. 点击 File → Sync Project with Gradle Files
2. 或者点击 File → Invalidate Caches → Restart

### Q2: SDK下载失败
**解决方案：**
1. 点击 File → Settings → Appearance & Behavior → System Settings → Android SDK
2. 点击 "Edit" 重新配置SDK路径
3. 确保网络连接正常

### Q3: 模拟器运行慢
**解决方案：**
1. 确保电脑支持VT-x虚拟化（BIOS中开启）
2. 增加模拟器内存：Edit AVD → Show Advanced Settings → Memory: 2048 MB
3. 使用真机测试会更快

### Q4: 应用安装失败
**解决方案：**
1. 检查手机/模拟器是否已开启USB调试
2. 尝试重启模拟器
3. 查看底部 Logcat 窗口的错误信息

---

## 下一步

安装完成后，你可以：

1. **测试应用**
   - 导入一本TXT小说
   - 测试阅读功能
   - 测试搜索功能
   - 测试设置功能

2. **自定义应用**
   - 修改颜色主题
   - 添加新功能
   - 优化性能

---

## 技术支持

如果遇到问题，可以：
1. 查看 Android Studio 帮助文档
2. 搜索 Stack Overflow
3. 查看 Logcat 日志排查问题

---

**祝你使用愉快！** 📚
