# MiniBrowser

Android 极简浏览器 — GeckoView + Jetpack Compose + Material 3

## 功能

- GeckoView (Firefox 内核) 网页渲染
- 智能 URL / 搜索分流
- 8 个内置搜索引擎可切换
- ExoPlayer 视频全屏播放 (HLS/DASH/MP4)
- 视频嗅探与下载（DOM + 网络层双重检测）
- M3U8 流媒体下载与合并
- 后台下载管理器
- 离线视频库
- 深色科技风 UI
- DataStore 偏好持久化

## 技术栈

| 组件 | 版本 |
|------|------|
| Kotlin | 1.9.22 |
| Compose BOM | 2024.02.00 |
| GeckoView | 125.0 |
| Media3 ExoPlayer | 1.3.0 |
| OkHttp | 4.12 |
| FFmpegKit | 6.0-2 |
| Room | 2.6.1 |
| Min SDK | 29 (Android 10) |
| Target SDK | 34 (Android 14) |

## 构建

```bash
./gradlew assembleDebug
```

## CI

Push 到 `main` 或提交 PR 时自动构建 Release APK。
