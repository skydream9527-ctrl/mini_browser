# 交互增强模块

## 状态：已实现 ✅

## 功能清单

| 功能 | 实现文件 | 说明 |
|------|---------|------|
| 手势导航 | `ui/components/GestureNavigationWrapper.kt` | 左边缘右滑=返回，右边缘左滑=前进 |
| 下拉刷新 | `ui/components/PullToRefreshWrapper.kt` | Material3 PullToRefresh |
| 长按菜单 | `ui/components/WebContextMenu.kt` | 新标签打开/复制/分享/下载 |
| 全页截图 | `screenshot/ScreenshotCapture.kt` | View.draw → MediaStore 保存到相册 |
| 网页翻译 | `translate/PageTranslator.kt` | Google Translate 重定向，8 种语言 |
| 翻译选择 | `ui/components/TranslateDialog.kt` | 语言选择 Bottom Sheet |
| 语音搜索 | `ui/components/VoiceSearchButton.kt` | Android 语音识别 Intent |
