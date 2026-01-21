# Readout-10min-App Supabase 用户配置指南

## 概述
本指南将帮助您完成 Supabase 云数据库的配置，以便 Readout-10min-App 能够正常运行。请按照以下步骤操作：

## 1. 注册/登录 Supabase 账号

1. 打开浏览器，访问 [Supabase 控制台](https://app.supabase.com/)
2. 如果您已有账号，直接登录
3. 如果您没有账号，点击 "Sign Up" 按钮注册新账号

## 2. 创建 Supabase 项目

1. 登录后，点击 "New Project" 按钮
2. 填写项目信息：
   - **Project Name**：输入 "Readout-10min-App"
   - **Database Password**：设置一个安全的密码
   - **Region**：选择离您最近的区域（建议选择亚洲区域如 Singapore）
3. 点击 "Create Project" 按钮
4. 等待项目创建完成（约需 2-5 分钟）

## 3. 运行 SQL 脚本创建数据库表

1. 项目创建完成后，进入项目控制台
2. 在左侧菜单中点击 "SQL Editor"
3. 点击 "New Query" 按钮
4. 复制 `/Users/yyiy/vibe_coding_project/Readout-10min-App/docs/SUPABASE_DATABASE_SETUP.sql` 文件的全部内容
5. 将复制的内容粘贴到 SQL Editor 中
6. 点击 "Run" 按钮执行脚本
7. 等待脚本执行完成，确保没有错误信息

## 4. 创建存储桶

1. 在左侧菜单中点击 "Storage"
2. 点击 "New Bucket" 按钮
3. 填写存储桶信息：
   - **Name**：输入 "files"
   - **Visibility**：选择 "Public"
4. 点击 "Create Bucket" 按钮

## 5. 获取项目凭证

1. 在左侧菜单中点击 "Project Settings"
2. 点击 "API" 选项卡
3. 找到并复制以下信息：
   - **Project URL**（例如：https://abc123.supabase.co）
   - **Publishable API Key**（例如：eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...）

## 6. 配置环境变量

### 6.1 网页端环境变量

1. 打开文件 `/Users/yyiy/vibe_coding_project/Readout-10min-App/web/.env`
2. 将第 2 行的 `VITE_SUPABASE_URL` 值替换为您的 Project URL
3. 将第 3 行的 `VITE_SUPABASE_PUBLISHABLE_KEY` 值替换为您的 Publishable API Key
4. 保存文件

### 6.2 移动端环境变量

1. 打开文件 `/Users/yyiy/vibe_coding_project/Readout-10min-App/android/local.properties`
2. 将第 11 行的 `SUPABASE_URL` 值替换为您的 Project URL
3. 将第 12 行的 `SUPABASE_PUBLISHABLE_KEY` 值替换为您的 Publishable API Key
4. 保存文件

## 7. 测试连接

### 7.1 网页端测试

1. 确保您已完成网页端环境变量配置
2. 在项目根目录下运行以下命令：
   ```bash
   cd web
   npm run dev
   ```
3. 打开浏览器访问 http://localhost:5173/
4. 尝试上传一个文件，验证是否能成功存储到 Supabase

### 7.2 移动端测试

1. 确保您已完成移动端环境变量配置
2. 在 Android Studio 中打开 `/Users/yyiy/vibe_coding_project/Readout-10min-App/android` 文件夹
3. 运行应用，查看是否能成功连接到 Supabase

## 8. 常见问题排查

### 8.1 连接失败

- **问题**：应用无法连接到 Supabase
- **解决方法**：
  1. 检查环境变量是否正确设置
  2. 确保 Project URL 和 Anon Public Key 没有拼写错误
  3. 检查网络连接是否正常

### 8.2 权限错误

- **问题**：上传文件或访问数据时出现权限错误
- **解决方法**：
  1. 检查存储桶的访问权限是否设置为 "Public"
  2. 确保 RLS 策略已正确配置
  3. 检查 API 密钥是否具有足够的权限

### 8.3 存储错误

- **问题**：无法上传文件到存储桶
- **解决方法**：
  1. 检查存储桶名称是否正确（应为 "files"）
  2. 确保存储桶已创建
  3. 检查文件大小是否超过限制

## 9. 完成配置

一旦您完成了以上所有步骤，您的 Supabase 数据库配置就完成了。您可以继续开发或使用 Readout-10min-App 应用。

## 10. 后续维护

- 定期备份您的 Supabase 数据库
- 监控项目的使用情况和性能
- 及时更新依赖库和 SDK 版本

## 11. 联系支持

如果您在配置过程中遇到任何问题，可以：
- 查看 [Supabase 官方文档](https://supabase.com/docs)
- 在 [Supabase 社区论坛](https://github.com/supabase/supabase/discussions) 寻求帮助
- 联系项目开发团队

祝您使用愉快！
