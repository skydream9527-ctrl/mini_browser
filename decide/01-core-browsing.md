# 核心浏览模块

## 状态：已实现 ✅

## 功能清单

| 功能 | 实现文件 | 说明 |
|------|---------|------|
| GeckoView 渲染 | `engine/GeckoEngineManager.kt` | Firefox 内核单例，GeckoSession 生命周期管理 |
| 智能 URL 判断 | `engine/UrlUtil.kt` | 自动识别 URL vs 搜索关键词，补全协议头 |
| 8 搜索引擎 | `engine/SearchEngineConfig.kt` | Google/Bing/DuckDuckGo/百度/Yahoo/Yandex/搜狗/360 |
| 搜索引擎切换 | `ui/components/SearchEngineSelector.kt` | Bottom Sheet 选择器，DataStore 持久化 |
| 搜索栏 | `ui/components/SearchBar.kt` | 键盘 Go 提交，占位符提示 |
| 首页 | `ui/screens/HomeScreen.kt` | 居中标题 + 搜索栏 + 引擎网格 + 快捷入口 |
| 浏览页 | `ui/screens/BrowserScreen.kt` | 工具栏 + URL 栏 + 进度条 + 菜单 |
| 导航控制 | `engine/GeckoEngineManager.kt` | 前进、后退、刷新 |
| 加载进度 | `ui/screens/BrowserScreen.kt` | 渐变进度条，加载完自动隐藏 |
| 分享页面 | `ui/screens/BrowserScreen.kt` | Intent.ACTION_SEND 分享当前 URL |
| 桌面模式 | `engine/UserAgentConfig.kt` | 移动端/桌面端 UA 切换 |

## 技术栈
- GeckoView omni 125.0（Firefox 内核，多架构）
- Jetpack Compose + Material 3
- DataStore Preferences
