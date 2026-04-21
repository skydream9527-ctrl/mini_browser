# MiniBrowser - Android 极简浏览器设计文档

## 概述

| 字段 | 值 |
|------|-----|
| 项目名 | MiniBrowser |
| 包名 | com.minibrowser.app |
| 最低 SDK | 29 (Android 10) |
| 目标 SDK | 34 (Android 14) |
| UI 框架 | Jetpack Compose + Material 3 |
| 渲染引擎 | GeckoView (Firefox 内核) |
| 视频播放 | Media3 ExoPlayer (HLS/DASH/MP4) |
| 界面风格 | 深色科技风 |
| CI | GitHub Actions |

## 功能需求

1. **网页浏览** - 基于 GeckoView 加载和渲染网页
2. **智能搜索** - URL 自动识别与搜索引擎查询分流
3. **多搜索引擎** - 8 个内置引擎可切换 (Google, Bing, DuckDuckGo, Baidu, Yahoo, Yandex, Sogou, 360)
4. **视频播放** - 网页内视频全屏时由 ExoPlayer 接管
5. **直播浏览** - 支持 HLS (.m3u8) 和 DASH (.mpd) 流媒体
6. **GitHub CI** - push/PR 自动构建 APK

## 项目结构

```
mini_browser/
├── app/
│   ├── src/main/
│   │   ├── java/com/minibrowser/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MiniBrowserApp.kt              # Application (GeckoRuntime 初始化)
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   ├── components/
│   │   │   │   │   ├── SearchBar.kt
│   │   │   │   │   ├── BrowserView.kt
│   │   │   │   │   ├── VideoPlayer.kt
│   │   │   │   │   └── SearchEngineSelector.kt
│   │   │   │   └── screens/
│   │   │   │       ├── HomeScreen.kt
│   │   │   │       └── BrowserScreen.kt
│   │   │   ├── engine/
│   │   │   │   ├── GeckoEngineManager.kt
│   │   │   │   └── SearchEngineConfig.kt
│   │   │   └── player/
│   │   │       └── VideoPlayerManager.kt
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   └── mipmap-*/  (launcher icons)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml
├── .github/workflows/build.yml
├── .gitignore
└── README.md
```

## 技术栈与依赖

| 组件 | 库 | 版本 |
|------|-----|------|
| 语言 | Kotlin | 1.9.22 |
| 构建 | AGP | 8.2.2 |
| UI | Compose BOM | 2024.02.00 |
| 渲染 | GeckoView | 125.0 |
| 视频 | Media3 ExoPlayer | 1.3.0 |
| 导航 | Navigation Compose | 2.7.7 |
| 持久化 | DataStore Preferences | 1.0.0 |
| AndroidX | Core KTX | 1.12.0 |
| Activity | Activity Compose | 1.8.2 |

## UI 设计

### 色彩方案 (深色科技风)

| 元素 | 颜色 |
|------|------|
| 背景主色 | `#1A1A2E` |
| 表面色 | `#16213E` |
| 工具栏 | `#0F3460` |
| 主强调色 | `#533483` → `#E94560` (渐变) |
| 文字主色 | `#EAEAEA` |
| 文字次色 | `#8892B0` |
| 进度条 | `#533483` → `#E94560` (渐变) |
| 链接色 | `#64FFDA` |

### 首页 (HomeScreen)

- 居中应用名 + Logo
- 搜索栏 (点击进入编辑态)
- 当前搜索引擎标识 (可点击切换)
- 8 个搜索引擎快捷导航网格 (2行4列)
- 深色背景

### 浏览页 (BrowserScreen)

- 顶部工具栏: 后退、前进、URL 显示/编辑栏、菜单
- 中部: GeckoView 全屏网页区域
- 加载进度条 (蓝紫渐变，加载时可见)
- 菜单项: 切换搜索引擎、刷新、分享

### 视频全屏播放

- ExoPlayer 横屏全屏
- 标准播放控件: 播放/暂停、进度条、时间显示
- 退出全屏恢复竖屏 + GeckoView

### 搜索引擎选择器

- 下拉/底部弹出 Sheet
- 单选列表，显示图标 + 名称
- 选中即刻生效并持久化

## 核心模块设计

### GeckoEngineManager

- 职责: GeckoRuntime 单例 + GeckoSession 生命周期
- 初始化: Application.onCreate() 中创建 GeckoRuntime
- ContentDelegate: 标题更新、全屏视频请求拦截
- NavigationDelegate: URL 变化回调、前进/后退状态更新
- ProgressDelegate: 页面加载进度 (0-100)
- PermissionDelegate: 权限请求转发给系统

### SearchEngineConfig

```kotlin
data class SearchEngine(
    val id: String,
    val name: String,
    val iconRes: Int,
    val searchUrl: String,   // 含 {query} 占位符
    val homeUrl: String
)
```

内置引擎:

| ID | 搜索 URL |
|----|----------|
| google | `https://www.google.com/search?q={query}` |
| bing | `https://www.bing.com/search?q={query}` |
| duckduckgo | `https://duckduckgo.com/?q={query}` |
| baidu | `https://www.baidu.com/s?wd={query}` |
| yahoo | `https://search.yahoo.com/search?p={query}` |
| yandex | `https://yandex.com/search/?text={query}` |
| sogou | `https://www.sogou.com/web?query={query}` |
| so360 | `https://www.so.com/s?q={query}` |

默认引擎: Google。用户选择通过 DataStore Preferences 持久化。

### VideoPlayerManager

- 触发: GeckoView ContentDelegate.onFullScreen() 回调
- 提取视频 URL: 通过 JS 注入 `document.querySelector('video').src`
- URL 类型判断:
  - `.m3u8` → HlsMediaSource
  - `.mpd` → DashMediaSource
  - 其他 → ProgressiveMediaSource
- 全屏时横屏锁定，退出恢复
- ExoPlayer 配置: DefaultHttpDataSource + 自定义 User-Agent

### URL 智能判断

```
输入 → trim()
  ├─ 匹配 URL (含协议头 或 含.且无空格)
  │   └─ 无协议则补 https:// → GeckoView 导航
  └─ 否则
      └─ URL 编码 → 替换 {query} → 导航
```

## AndroidManifest 权限

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## GitHub Actions CI

**触发条件**: push to `main` / PR to `main`

**流程**:
1. Checkout 代码
2. Setup JDK 17
3. Gradle cache (hash of wrapper properties)
4. `./gradlew assembleRelease`
5. Upload `app-release-unsigned.apk` 作为 artifact

产出: 未签名 Release APK，可在 GitHub Actions 页面下载。

## APK 大小预估

| 组件 | 大小 |
|------|------|
| GeckoView (arm64) | ~30 MB |
| ExoPlayer (Media3) | ~3 MB |
| Compose + Material3 | ~5 MB |
| App 代码 + 资源 | ~1 MB |
| **Release APK (压缩)** | **~25-30 MB** |

## 验收标准

- [ ] 应用启动显示首页，搜索栏和引擎网格正常渲染
- [ ] 输入关键词搜索，使用当前引擎跳转正确
- [ ] 输入 URL 直接导航到目标网页
- [ ] 切换搜索引擎即时生效且重启后保持
- [ ] 网页内视频点击全屏后由 ExoPlayer 接管播放
- [ ] HLS 直播链接可正常播放
- [ ] 前进/后退导航正常
- [ ] 加载进度条显示且自动隐藏
- [ ] GitHub Actions 推送后自动构建成功
- [ ] APK 可安装并正常运行在 Android 10+ 设备
