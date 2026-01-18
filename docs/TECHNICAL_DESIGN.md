# Readout-10min-App 技术设计文档

## 1. 技术栈选型

### 1.1 前端（网页端）
- **开发语言**：TypeScript
- **框架**：React
- **UI组件库**：Ant Design
- **构建工具**：Vite
- **部署平台**：Vercel

### 1.2 后端服务
- Supabase

### 1.3 数据库
- Supabase PostgreSQL

### 1.4 云存储
- Supabase Storage

### 1.5 移动端（安卓）
- **开发语言**：Kotlin
- **UI框架**：Jetpack Compose
- **网络请求**：Retrofit
- **本地数据库**：Room
- **本地存储**：SharedPreferences

## 2. 系统架构

### 2.1 整体架构
```
+---------------------+     +---------------------+
|    React Web App     |     |    Android App      |
+---------------------+     +---------------------+
          |                          |
          v                          v
+---------------------+     +---------------------+
|     Vercel CDN       |     |     Supabase API     |
+---------------------+     +---------------------+
          |                          |
          v                          v
+---------------------+     +---------------------+
|    Supabase API      |     |    Room Database     |
+---------------------+     +---------------------+
          |
          v
+---------------------+     +---------------------+
|  Supabase Storage    |     |  Supabase PostgreSQL |
+---------------------+     +---------------------+
```

### 2.2 核心数据流
1. **文件处理流程**：
   - 用户在网页端上传文件 → Vercel处理 → Supabase Storage存储 → 文本解析 → Supabase PostgreSQL存储解析结果

2. **移动端使用流程**：
   - 安卓端从Supabase API获取内容 → 本地缓存 → 用户朗读练习 → 进度同步回Supabase

## 3. 关键功能实现方案

### 3.1 文件上传与解析
- **上传方式**：React前端直接上传至Supabase Storage
- **解析方案**：使用Node.js云函数（Vercel Edge Functions）进行文本提取
- **支持格式**：PDF、Word（.docx）、TXT

### 3.2 智能段落拆分
- **实现位置**：Vercel Edge Functions
- **算法**：基于词数和平均语速（150-180词/分钟）自动拆分
- **存储**：拆分结果存储在Supabase PostgreSQL

### 3.3 朗读练习功能
- **数据获取**：安卓端通过Retrofit调用Supabase API获取内容
- **本地存储**：使用Room数据库存储已下载内容和进度
- **进度同步**：定期将阅读进度同步至Supabase

### 3.4 用户认证（可选）
- **方案**：使用Supabase Auth
- **支持方式**：邮箱/密码、第三方登录

## 4. 部署方案

### 4.1 网页端部署
- **平台**：Vercel
- **流程**：GitHub代码推送自动部署
- **优势**：全球CDN加速，自动SSL，零配置部署

### 4.2 后端服务部署
- **平台**：Supabase
- **配置**：
  - 数据库：Supabase PostgreSQL
  - 存储：Supabase Storage
  - API：自动生成RESTful API

### 4.3 移动端部署
- **构建**：Android Studio构建APK
- **分发**：Google Play Store或内部测试

## 5. 开发工作流

### 5.1 前端开发
- **IDE**：VS Code
- **版本控制**：Git
- **协作**：GitHub

### 5.2 后端开发
- **开发方式**：Supabase控制台 + SQL
- **API测试**：Supabase Studio或Postman

### 5.3 移动端开发
- **IDE**：Android Studio
- **调试**：模拟器或真机调试

## 6. 监控与维护

### 6.1 日志管理
- **前端**：Vercel日志
- **后端**：Supabase日志

### 6.2 性能监控
- **前端**：Vercel Analytics
- **后端**：Supabase监控

### 6.3 数据备份
- **方案**：Supabase自动备份
- **频率**：每日

## 7. 技术选型总结

| 类别 | 技术选型 | 优势 |
|------|----------|------|
| 前端框架 | React + TypeScript | 类型安全，生态丰富 |
| 部署平台 | Vercel | 全球CDN，零配置部署 |
| 后端服务 | Supabase | 全托管BaaS，自动API生成 |
| 数据库 | Supabase PostgreSQL | 关系型数据库，实时同步 |
| 云存储 | Supabase Storage | 与数据库无缝集成 |
| 移动端框架 | Jetpack Compose | 现代UI框架，声明式开发 |

## 8. 开发启动准备

### 8.1 账号准备
- Vercel账号
- Supabase账号
- GitHub账号

### 8.2 环境配置
- Node.js 18+
- npm/yarn/pnpm
- Android Studio
- React开发环境

### 8.3 项目初始化
- 创建React项目
- 配置Vercel部署
- 设置Supabase项目
- 配置数据库表结构

## 9. 核心API设计

### 9.1 网页端API
- **文件上传**：`POST /storage/v1/object`（Supabase Storage API）
- **内容获取**：`GET /rest/v1/paragraphs`（Supabase自动生成）

### 9.2 移动端API
- **内容列表**：`GET /rest/v1/content`
- **段落详情**：`GET /rest/v1/paragraphs?content_id=eq.{id}`
- **进度同步**：`POST /rest/v1/progress`

## 10. 数据结构设计

### 10.1 核心数据表
- **content**：存储文章基本信息
- **paragraphs**：存储拆分后的段落
- **progress**：存储用户阅读进度
- **practice_records**：存储练习记录

## 11. 总结

本项目采用React + Vercel + Supabase技术栈，具有以下优势：
1. **开发效率高**：Supabase自动生成API，无需编写后端代码
2. **部署简单**：Vercel提供零配置部署，全球CDN加速
3. **扩展性强**：支持实时数据，易于添加新功能
4. **成本可控**：Supabase提供免费额度，适合初创项目
5. **生态完善**：React和Supabase社区活跃，资源丰富

技术方案已确定，可以开始开发。