# [规划] 密码管理

## 状态：待实现 🔲

## 需求
- 检测登录表单自动弹出保存密码提示
- 已保存密码自动填充
- 密码列表管理（查看/编辑/删除）
- 主密码或生物识别解锁

## 技术方案
- Room 加密存储 (SQLCipher)
- GeckoView ContentDelegate 表单检测
- Android BiometricPrompt 解锁
