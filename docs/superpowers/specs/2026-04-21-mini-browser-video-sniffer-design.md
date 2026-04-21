# MiniBrowser v1.1 — 视频嗅探与下载 设计文档

## 概述

| 字段 | 值 |
|------|-----|
| 项目名 | MiniBrowser |
| 功能版本 | v1.1 |
| 功能范围 | 双层视频嗅探 + 下载管理器 + M3U8 下载 + 离线视频库 |
| 竞品参考 | 悟空浏览器（视频嗅探）、夸克浏览器（下载体验） |
| 新增依赖 | OkHttp 4.12, FFmpegKit (min-gpl), Room 2.6, WorkManager |

## 功能需求

1. **双层视频嗅探** — DOM 检测 + 网络请求拦截，覆盖静态和动态加载的视频资源
2. **普通视频下载** — MP4/WebM/FLV 多线程分片下载，断点续传
3. **M3U8 流媒体下载** — 解析 playlist、下载 TS 分片、AES 解密、合并为 MP4
4. **下载管理器** — 多任务并发、暂停/续传、后台下载、网络感知
5. **离线视频库** — 本地视频浏览、播放、删除、导出到公共目录

## 架构

### 双层嗅探架构

```
ContentScript (DOM Layer)  ──┐
                              ├──▶ VideoSniffer (Native) ──▶ 去重+分类 ──▶ UI 浮窗
BackgroundScript (Net Layer) ┘
```

#### Layer 1 — DOM 嗅探 (Content Script)

- 注入 Content Script 到每个页面
- `MutationObserver` 实时监听 DOM 变化，捕获 `<video>` / `<source>` / `<iframe>` 元素
- 提取 `video.src`、`video.currentSrc`、所有 `<source>` 标签的候选 URL
- 通过 `WebExtension.MessageDelegate` 将视频 URL 传递给 Native 层

#### Layer 2 — 网络层嗅探 (Background Script)

- `browser.webRequest.onBeforeRequest` 监听所有请求
- URL 后缀匹配：`.mp4`, `.m3u8`, `.flv`, `.webm`, `.mpd`, `.ts`
- MIME type 匹配：`video/*`, `application/x-mpegURL`, `application/dash+xml`
- 去重合并后通过 messaging 通知 Native 层

#### WebExtension 结构

```
sniffer-extension/
├── manifest.json
├── content.js       # DOM 视频检测 + MutationObserver
└── background.js    # webRequest 网络层拦截
```

#### Native 层 VideoSniffer

- 维护 per-tab 视频资源列表（`Map<String, List<SniffedVideo>>`）
- 去重策略：URL 精确匹配 + 同域名相似路径合并
- 分类：普通视频（MP4/WebM/FLV）、直播流（M3U8/DASH）、未知类型
- 通过 `StateFlow<List<SniffedVideo>>` 暴露给 UI 层

### 下载引擎

#### 普通视频下载

- OkHttp 多线程分片下载（默认 3 并发分片）
- 支持 HTTP Range 请求断点续传
- 每个分片状态持久化到 Room
- 下载路径：`app-specific/files/Videos/`（无需额外存储权限）

#### M3U8 流媒体下载

```
M3U8 URL
  ├─ 下载 master playlist
  │    └─ 多码率 → 展示分辨率选择 (1080p/720p/480p)
  ├─ 下载 media playlist
  │    └─ 解析 .ts 分片 URL + #EXT-X-KEY 解密密钥
  ├─ 并发下载 TS 分片（并发数 = 5）
  │    └─ AES-128 加密流：下载 key → 每分片本地解密
  └─ FFmpegKit 合并为 MP4
       └─ ffmpeg -i concat:seg1.ts|seg2.ts... -c copy output.mp4
```

- 进度计算：`已完成分片数 / 总分片数`

#### 下载管理器

| 状态 | 说明 |
|------|------|
| `PENDING` | 排队中 |
| `DOWNLOADING` | 下载中 |
| `PAUSED` | 暂停 |
| `MERGING` | M3U8 合并中 |
| `COMPLETED` | 完成 |
| `FAILED` | 失败（可重试） |

- Foreground Service 保活后台下载 + 通知栏进度
- 最多同时 3 个下载任务，超出排队
- WiFi 断开自动暂停（可配置）
- Room 持久化任务状态，App 重启恢复

### 离线视频库

#### 数据模型

```kotlin
@Entity(tableName = "download_tasks")
data class DownloadTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val type: VideoType,          // MP4, M3U8, DASH, OTHER
    val status: DownloadStatus,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val totalSegments: Int,       // M3U8 only
    val completedSegments: Int,
    val filePath: String?,
    val resolution: String?,
    val duration: Long?,
    val sourcePageUrl: String,
    val createdAt: Long,
    val updatedAt: Long
)
```

#### 视频库 UI

- 入口：首页底部 Tab「视频」
- 两个子 Tab：**下载中** / **已完成**
- 下载中列表：封面、标题、进度条、速度、暂停/继续按钮；长按删除/重试
- 已完成列表：2 列网格，封面 + 标题 + 时长 + 文件大小；点击播放，长按删除/分享/导出

#### 导出

- Android 10+ 使用 `MediaStore.Video` API 写入公共 Movies 目录
- 导出为可选操作，默认存 App 私有目录

## 嗅探交互流程

```
用户浏览网页
  ├─ 嗅探到视频 → 右下角浮动按钮 + 角标数字
  ├─ 点击浮动按钮 → Bottom Sheet:
  │    ├─ 列出所有视频资源（URL缩略 + 类型标签 + 预估大小）
  │    ├─ M3U8 → 展示分辨率选择
  │    └─ 点击「下载」→ toast + 开始下载
  └─ 下载中 → 通知栏进度 + 浮动按钮动画
```

## 新增项目结构

```
app/src/main/
├── assets/
│   └── sniffer-extension/
│       ├── manifest.json
│       ├── content.js
│       └── background.js
├── java/com/minibrowser/app/
│   ├── sniffer/
│   │   ├── VideoSniffer.kt           # 嗅探结果管理
│   │   ├── SniffedVideo.kt           # 数据类
│   │   └── WebExtensionManager.kt    # WebExtension 安装+消息
│   ├── download/
│   │   ├── DownloadTask.kt           # Room Entity
│   │   ├── VideoType.kt              # 枚举
│   │   ├── DownloadStatus.kt         # 枚举
│   │   ├── DownloadDao.kt            # Room DAO
│   │   ├── DownloadDatabase.kt       # Room Database
│   │   ├── DownloadEngine.kt         # 普通下载
│   │   ├── M3u8DownloadEngine.kt     # M3U8 解析+下载+合并
│   │   ├── DownloadManager.kt        # 任务调度
│   │   └── DownloadService.kt        # Foreground Service
│   ├── ui/
│   │   ├── components/
│   │   │   ├── VideoSnifferFab.kt    # 嗅探浮动按钮
│   │   │   ├── SniffedVideoSheet.kt  # 视频列表 Bottom Sheet
│   │   │   └── DownloadProgressItem.kt
│   │   └── screens/
│   │       └── VideoLibraryScreen.kt # 离线视频库
│   └── ui/navigation/
│       └── NavGraph.kt               # 新增 video_library route
```

## 新增依赖

| 组件 | 库 | 版本 |
|------|-----|------|
| HTTP | OkHttp | 4.12.0 |
| 转封装 | FFmpegKit (min-gpl) | 6.0-2 |
| 数据库 | Room | 2.6.1 |
| 后台 | WorkManager | 2.9.0 |

## 验收标准

- [ ] 打开含视频的网页时，浮动按钮自动出现并显示视频数量
- [ ] 点击浮动按钮展示所有嗅探到的视频，含类型和大小信息
- [ ] MP4 视频可正常下载，支持暂停续传
- [ ] M3U8 视频可选择分辨率后下载，合并为可播放的 MP4
- [ ] 下载管理器支持多任务并发、暂停、继续、删除
- [ ] App 切后台后下载继续进行，通知栏显示进度
- [ ] 离线视频库正确展示已下载视频，可播放和管理
- [ ] 导出功能将视频写入公共 Movies 目录
- [ ] WiFi 断开时下载自动暂停（可配置）
