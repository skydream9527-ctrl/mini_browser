# 阅读体验模块

## 状态：已实现 ✅

## 功能清单

| 功能 | 实现文件 | 说明 |
|------|---------|------|
| 阅读模式 | `reader/ReadabilityExtractor.kt` | JS 提取正文，去广告/导航/侧边栏 |
| 阅读页面 | `ui/screens/ReaderScreen.kt` | WebView 深色排版，可调字体 12-32px |
| 页面内搜索 | `ui/components/FindInPageBar.kt` | JS window.find() 高亮匹配 |
| 桌面模式 | `engine/UserAgentConfig.kt` | Firefox 桌面 UA 切换 |

## 阅读模式排版
- 背景：#1A1A2E（深色）
- 文字：#EAEAEA
- 链接：#64FFDA
- 引用：#533483 左边框
- 代码块：#16213E 背景
