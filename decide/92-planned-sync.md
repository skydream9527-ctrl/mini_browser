# [规划] 云同步

## 状态：待实现 🔲

## 需求
- 跨设备同步书签和浏览历史
- 账号系统（可选 Firebase Auth）
- 增量同步，冲突合并

## 技术方案
- Firebase Firestore / 自建 API
- WorkManager 定时后台同步
- 设备 UUID 标识
