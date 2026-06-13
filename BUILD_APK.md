# 如何打包APK

## 方法1：使用GitHub Actions（推荐，无需本地环境）

### 步骤1：创建GitHub账号
如果还没有GitHub账号，先注册一个：
- 访问 https://github.com
- 点击 "Sign up" 注册

### 步骤2：创建新仓库
1. 登录GitHub
2. 点击右上角 "+" → "New repository"
3. Repository name: `NovelReader`
4. 选择 "Public"
5. 点击 "Create repository"

### 步骤3：上传代码
在电脑上打开命令提示符或PowerShell，执行：

```bash
cd c:\Users\Administrator\Desktop\更俗\NovelReader\Android

# 初始化Git仓库
git init
git add .
git commit -m "Initial commit"

# 关联GitHub仓库（替换 YOUR_USERNAME 为你的GitHub用户名）
git remote add origin https://github.com/YOUR_USERNAME/NovelReader.git
git branch -M main
git push -u origin main
```

### 步骤4：自动构建
1. 上传代码后，GitHub会自动开始构建
2. 访问你的仓库页面
3. 点击 "Actions" 标签
4. 等待构建完成（约5-10分钟）

### 步骤5：下载APK
1. 构建完成后，点击 "Actions" 标签
2. 点击最新的构建任务
3. 在 "Artifacts" 部分，点击 "app-debug.apk" 下载
4. 解压下载的zip文件，得到APK

### 步骤6：安装到手机
1. 将APK文件传输到手机（USB、微信、QQ等）
2. 在手机上打开APK文件
3. 允许安装未知来源应用
4. 安装完成！

---

## 方法2：使用本地Android Studio（需要安装）

### 前提条件
需要先安装 Android Studio，参考 `INSTALL_GUIDE.md`

### 构建步骤
1. 打开 Android Studio
2. 打开项目：`c:\Users\Administrator\Desktop\更俗\NovelReader\Android`
3. 等待Gradle同步完成
4. 点击菜单 Build → Build Bundle(s) / APK(s) → Build APK(s)
5. 等待构建完成
6. 点击通知栏的 "locate" 找到APK文件

### APK位置
构建完成后，APK文件在：
```
app\build\outputs\apk\debug\app-debug.apk
```

---

## 方法3：使用在线构建服务

### Appetize.io
- 访问 https://appetize.io
- 上传项目ZIP文件
- 在线编译并下载APK

### CircleCI
- 访问 https://circleci.com
- 连接GitHub仓库
- 自动构建APK

---

## APK文件说明

### debug vs release
- **debug APK**: 调试版本，可以直接安装，但没有签名优化
- **release APK**: 发布版本，需要签名，性能更好

### 文件大小
- 预计APK大小：约5-10MB
- 包含所有依赖和资源

---

## 安装APK到手机

### Android 8.0及以上
1. 将APK传输到手机
2. 点击APK文件
3. 如果提示"禁止安装"，点击"设置"
4. 开启"允许此来源应用"
5. 返回继续安装

### Android 7.0及以下
1. 将APK传输到手机
2. 点击APK文件
3. 点击"安装"

---

## 常见问题

### Q1: GitHub Actions构建失败
**解决方案：**
- 检查代码是否有语法错误
- 查看Actions日志了解具体错误
- 确保所有文件都已上传

### Q2: APK安装失败
**解决方案：**
- 确保手机开启了"允许未知来源应用"
- 检查Android版本是否支持（最低Android 8.0）
- 尝试卸载旧版本后重新安装

### Q3: 应用闪退
**解决方案：**
- 检查是否有权限问题
- 查看Logcat日志
- 确保导入的TXT文件编码正确（UTF-8）

---

## 推荐方案

**对于初学者，推荐使用方法1（GitHub Actions）**：
- ✅ 无需安装任何开发工具
- ✅ 无需配置环境
- ✅ 自动构建，简单快捷
- ✅ 可以随时重新构建

**对于开发者，推荐使用方法2（Android Studio）**：
- ✅ 可以实时调试
- ✅ 可以修改代码后立即测试
- ✅ 功能更完整

---

**选择适合你的方法开始吧！** 🚀
