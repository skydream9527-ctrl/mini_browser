# 标签页管理模块

## 状态：已实现 ✅

## 功能清单

| 功能 | 实现文件 | 说明 |
|------|---------|------|
| Tab 数据模型 | `tab/TabInfo.kt` | id/title/url/isIncognito/isActive |
| Tab 管理器 | `tab/TabManager.kt` | 创建/切换/关闭，GeckoSession per tab |
| 标签切换器 | `ui/screens/TabSwitcherScreen.kt` | 2 列网格，无痕标签红色标识 |
| 无痕模式 | `tab/TabManager.kt` | 无痕标签不记录历史 |
| 关闭所有无痕 | `tab/TabManager.kt` | 批量关闭无痕标签 |

## 交互流程
1. 浏览页菜单 → "标签页 (N)" → 打开标签切换器
2. 标签切换器：点击切换 / 右上角 X 关闭 / 无痕图标创建无痕标签
3. FAB "+" 创建新标签 → 跳转首页
