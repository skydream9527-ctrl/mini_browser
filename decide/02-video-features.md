# 视频功能模块

## 状态：已实现 ✅

## 功能清单

| 功能 | 实现文件 | 说明 |
|------|---------|------|
| DOM 视频嗅探 | `assets/sniffer-extension/content.js` | MutationObserver 监听 video/source 标签 |
| 网络层嗅探 | `assets/sniffer-extension/background.js` | webRequest 拦截视频 URL + MIME 检测 |
| 嗅探管理 | `sniffer/VideoSniffer.kt` | 去重、分类、StateFlow 暴露给 UI |
| WebExtension 安装 | `sniffer/WebExtensionManager.kt` | GeckoView 内置扩展安装 + 消息桥接 |
| 嗅探浮窗 | `ui/components/VideoSnifferFab.kt` | 角标显示视频数量，动画出现 |
| 视频列表 | `ui/components/SniffedVideoSheet.kt` | Bottom Sheet 展示嗅探结果 + 下载 |
| 普通下载 | `download/DownloadEngine.kt` | OkHttp 多线程分片 + 断点续传 |
| M3U8 下载 | `download/M3u8DownloadEngine.kt` | 解析 playlist + TS 分片下载 + AES 解密 + 合并 |
| 下载管理 | `download/DownloadManager.kt` | 多任务并发(max 3)、暂停/续传/重试 |
| 后台下载 | `download/DownloadService.kt` | Foreground Service + 通知栏进度 |
| 离线视频库 | `ui/screens/VideoLibraryScreen.kt` | 下载中/已完成双 Tab，网格展示 |
| ExoPlayer 播放 | `player/VideoPlayerManager.kt` | HLS/DASH/Progressive 自适应 |
| 画中画 | `player/PipHelper.kt` | Android PiP 16:9，Android 12+ 自动进入 |
| 通用文件下载 | `download/FileDownloader.kt` | Android DownloadManager，支持所有文件类型 |

## 数据模型
- `DownloadTask` (Room Entity) — 下载任务持久化
- `SniffedVideo` — 嗅探结果数据类
