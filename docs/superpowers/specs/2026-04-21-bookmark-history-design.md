# MiniBrowser v1.2 — 书签与历史记录 设计文档

## 概述

| 字段 | 值 |
|------|-----|
| 功能版本 | v1.2 |
| 功能范围 | 书签收藏 + 浏览历史 + 搜索 |
| 前置依赖 | v1.0 基础浏览功能 |
| 后续依赖 | 多标签页（无痕模式需跳过历史写入） |

## 数据模型

### BookmarkEntity

```kotlin
@Entity(tableName = "bookmarks", indices = [Index(value = ["url"], unique = true)])
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val favicon: String? = null,
    val folderId: Long? = null,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
```

### HistoryEntity

```kotlin
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitCount: Int = 1,
    val lastVisitAt: Long = System.currentTimeMillis(),
    val favicon: String? = null
)
```

## 核心逻辑

- GeckoEngineManager 的 `onUrlChanged` 回调触发时，自动写入/更新 HistoryEntity
- 同 URL 合并：递增 visitCount，更新 lastVisitAt 和 title
- 无痕模式标记（`isIncognito: Boolean`）预留给多标签页功能，为 true 时跳过历史写入
- 书签通过浏览页菜单「收藏」按钮操作
- 收藏已存在 URL 时 Toast 提示「已收藏」
- 历史记录和书签均支持按标题/URL 模糊搜索

## UI

### 首页入口

- 视频库旁新增书签图标和历史图标

### BookmarkScreen

- 列表展示所有书签：favicon + 标题 + URL
- 点击打开（导航到 BrowserScreen）
- 长按弹出菜单：编辑标题、删除
- 顶部搜索栏

### HistoryScreen

- 按日期分组：今天、昨天、更早
- 每项：标题 + URL + 访问次数
- 顶部搜索栏
- 底部「清除所有历史」按钮

### BrowserScreen 菜单扩展

- 新增「收藏」菜单项（星标图标，已收藏时高亮显示）
- 新增「历史记录」菜单项
- 新增「书签」菜单项

## 新增文件

```
app/src/main/java/com/minibrowser/app/
├── data/
│   ├── MiniBrowserDatabase.kt    — 统一数据库（合并 DownloadDatabase）
│   ├── BookmarkEntity.kt
│   ├── BookmarkDao.kt
│   ├── HistoryEntity.kt
│   ├── HistoryDao.kt
│   ├── BookmarkRepository.kt
│   └── HistoryRepository.kt
├── ui/screens/
│   ├── BookmarkScreen.kt
│   └── HistoryScreen.kt
```

## 修改文件

- `MiniBrowserApp.kt` — 初始化新数据库和 Repository
- `NavGraph.kt` — 新增 bookmark / history 路由
- `HomeScreen.kt` — 新增书签和历史入口
- `BrowserScreen.kt` — 菜单扩展（收藏/历史/书签）
- `download/DownloadDatabase.kt` → 迁移到 `data/MiniBrowserDatabase.kt`
- `download/DownloadDao.kt` / `DownloadManager.kt` — 引用新数据库

## 验收标准

- [ ] 浏览网页时自动记录历史，重复访问递增计数
- [ ] 菜单点击「收藏」可添加书签，已收藏 URL 提示
- [ ] 书签页面列表正确展示，点击可跳转
- [ ] 历史页面按日期分组展示，支持搜索
- [ ] 清除历史功能正常
- [ ] 长按书签可删除
- [ ] 数据库升级不丢失已有下载任务数据
