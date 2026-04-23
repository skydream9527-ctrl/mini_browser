# 数据管理模块

## 状态：已实现 ✅

## 功能清单

| 功能 | 实现文件 | 说明 |
|------|---------|------|
| 书签收藏 | `data/BookmarkEntity.kt` + `BookmarkDao.kt` | Room Entity，URL 唯一索引 |
| 书签仓库 | `data/BookmarkRepository.kt` | 添加去重、搜索、删除 |
| 书签页面 | `ui/screens/BookmarkScreen.kt` | 列表 + 搜索 + 滑动删除 |
| 浏览历史 | `data/HistoryEntity.kt` + `HistoryDao.kt` | 自动记录，访问次数累计 |
| 历史仓库 | `data/HistoryRepository.kt` | recordVisit 合并逻辑 |
| 历史页面 | `ui/screens/HistoryScreen.kt` | 日期分组（今天/昨天/更早）+ 清除 |
| 快捷方式 | `data/ShortcutEntity.kt` + `ShortcutDao.kt` | 首页自定义快捷导航 |
| 添加快捷方式 | `ui/components/AddShortcutDialog.kt` | 弹窗输入名称和 URL |
| 统一数据库 | `data/MiniBrowserDatabase.kt` | Room v3，4 个 Entity |
| 偏好设置 | `data/PreferencesRepository.kt` | DataStore 搜索引擎选择 |

## 数据库架构
MiniBrowserDatabase (v3):
- download_tasks — 下载任务
- bookmarks — 书签（URL 唯一）
- history — 浏览历史
- shortcuts — 首页快捷方式
