# Readout-10min-App Supabase 设置指南

## 1. 创建Supabase项目

### 步骤1: 访问Supabase控制台
- 打开浏览器，访问 [Supabase控制台](https://app.supabase.com/)
- 登录或注册Supabase账号

### 步骤2: 创建新项目
- 点击"New Project"按钮
- 填写项目名称（例如：Readout-10min-App）
- 选择数据库密码
- 选择区域（建议选择离用户最近的区域）
- 点击"Create Project"按钮

### 步骤3: 获取项目凭证
- 项目创建完成后，进入项目设置
- 点击"API"选项卡
- 复制以下信息：
  - Project URL
  - Anon Public Key

## 2. 配置环境变量

### 网页端环境变量
在 `web/.env` 文件中添加：
```
VITE_SUPABASE_URL=YOUR_PROJECT_URL
VITE_SUPABASE_ANON_KEY=YOUR_ANON_PUBLIC_KEY
```

### 移动端环境变量
在 `android/local.properties` 文件中添加：
```
SUPABASE_URL=YOUR_PROJECT_URL
SUPABASE_ANON_KEY=YOUR_ANON_PUBLIC_KEY
```

## 3. 数据库设置

### 步骤1: 运行SQL脚本
- 在Supabase控制台中，进入"SQL Editor"
- 复制并粘贴 `SUPABASE_DATABASE_SETUP.sql` 文件中的内容
- 点击"Run"按钮执行脚本

### 步骤2: 验证表结构
- 进入"Database"选项卡
- 点击"Tables"查看是否成功创建了以下表：
  - content
  - paragraphs
  - progress
  - practice_records

## 4. 存储设置

### 步骤1: 创建存储桶
- 进入"Storage"选项卡
- 点击"New Bucket"按钮
- 填写桶名称：`files`
- 选择"Public"访问权限
- 点击"Create Bucket"按钮

### 步骤2: 配置存储策略
- 确保存储桶有适当的访问策略
- 允许用户上传和读取文件

## 5. 身份验证设置（可选）

### 步骤1: 启用身份验证
- 进入"Authentication"选项卡
- 启用所需的身份验证方法（如邮箱/密码）

### 步骤2: 配置重定向URL
- 添加适当的重定向URL，以便在身份验证后返回应用

## 6. 测试连接

### 网页端测试
- 运行网页端应用
- 尝试上传文件，验证是否能成功存储到Supabase

### 移动端测试
- 运行移动端应用
- 尝试获取内容列表，验证是否能成功从Supabase获取数据

## 7. 故障排除

### 常见问题
1. **连接失败**：检查环境变量是否正确设置
2. **权限错误**：检查RLS策略是否正确配置
3. **存储错误**：检查存储桶权限是否正确设置

### 解决方法
- 确保网络连接稳定
- 检查Supabase项目状态
- 验证API密钥和URL是否正确
- 查看Supabase控制台中的错误日志

## 8. 后续维护

### 数据库备份
- Supabase自动提供每日备份
- 可以在"Database" > "Backups"中查看和管理备份

### 监控
- 在Supabase控制台中查看项目使用情况
- 监控API调用和错误率

### 扩展
- 随着应用的增长，可能需要调整数据库索引
- 考虑使用Supabase的边缘函数进行更复杂的后端逻辑