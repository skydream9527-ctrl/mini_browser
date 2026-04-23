# 安全与隐私模块

## 状态：已实现 ✅

## 功能清单

| 功能 | 实现文件 | 说明 |
|------|---------|------|
| 广告拦截引擎 | `assets/adblocker-extension/background.js` | 50+ 广告/追踪域名黑名单 |
| URL 模式匹配 | `assets/adblocker-extension/background.js` | /ads/ /banner/ /tracking/ 等路径规则 |
| MIME 拦截 | `assets/adblocker-extension/background.js` | webRequest.onBeforeRequest blocking |
| 拦截计数 | `adblocker/AdBlocker.kt` | StateFlow 实时计数 |
| 扩展管理 | `adblocker/AdBlockerExtensionManager.kt` | GeckoView WebExtension 安装 |
| 无痕浏览 | `tab/TabManager.kt` | 无痕标签跳过历史记录写入 |

## 广告域名覆盖
Google Ads, Facebook, Amazon, Yahoo, Twitter, LinkedIn, Criteo, Outbrain, Taboola, 百度推广, 搜狗, CNZZ, 友盟 等 50+ 域名
