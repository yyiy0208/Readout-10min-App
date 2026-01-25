# Readout-10min-App 文件结构

## 项目概述
Readout-10min-App 是一个英语学习应用，包含 Web 和 Android 两个版本，用于帮助用户练习英语阅读和发音。

## 整体文件结构

```
Readout-10min-App/
├── android/             # Android 客户端代码
├── docs/                # 项目文档
├── prototypes/          # UI 原型设计
├── web/                 # Web 客户端代码
├── .gitignore           # Git 忽略文件配置
├── LICENSE              # 许可证文件
└── README.md            # 项目说明文档
```

## 详细目录说明

### 1. android/
Android 客户端代码，使用原生 Android 开发。

```
android/
├── app/                 # 主应用模块
│   ├── src/             # 源代码
│   └── build.gradle     # 模块构建配置
├── gradle/              # Gradle 包装器
├── build.gradle         # 项目构建配置
├── gradle.properties    # Gradle 属性配置
└── settings.gradle      # Gradle 设置
```

### 2. docs/
项目文档，包含开发计划、技术设计、数据库设计等。

```
docs/
├── DEVELOPMENT_PLAN.md         # 开发计划
├── FILE_STRUCTURE.md           # 文件结构文档（本文档）
├── FILE_UPLOAD_SPEC.md         # 文件上传功能规范
├── README_PRD.md               # 产品需求文档
├── SUPABASE_DATABASE_SETUP.sql # Supabase 数据库初始化脚本
├── SUPABASE_SETUP_GUIDE.md     # Supabase 配置指南
├── TECHNICAL_DESIGN.md         # 技术设计文档
└── UI_DESIGN.md                # UI 设计规范
```

### 3. prototypes/
UI 原型设计，包含 Web 和 Mobile 两个版本的界面原型。

```
prototypes/
├── mobile/               # 移动端原型
│   ├── content-library/  # 内容库页面
│   ├── home/             # 首页
│   ├── progress-record/  # 进度记录页面
│   └── reading-practice/ # 阅读练习页面
└── web/                  # Web 端原型
    ├── file-management/  # 文件管理页面
    └── file-processing/  # 文件处理页面
```

### 4. web/
Web 客户端代码，使用 React + TypeScript + Vite 开发。

```
web/
├── public/               # 静态资源
├── src/                  # 源代码
│   ├── assets/           # 应用资源
│   ├── components/       # UI 组件
│   ├── hooks/            # 自定义 Hooks
│   ├── pages/            # 页面组件
│   ├── services/         # 业务逻辑服务
│   ├── types/            # TypeScript 类型定义
│   ├── utils/            # 工具函数
│   ├── App.css           # 应用样式
│   ├── App.tsx           # 应用主组件
│   ├── index.css         # 全局样式
│   └── main.tsx          # 应用入口
├── .env.example          # 环境变量示例
├── .gitignore            # Git 忽略文件配置
├── README.md             # Web 应用说明文档
├── eslint.config.js      # ESLint 配置
├── index.html            # HTML 模板
├── package-lock.json     # NPM 依赖锁定文件
├── package.json          # NPM 依赖配置
├── tsconfig.app.json     # TypeScript 应用配置
├── tsconfig.json         # TypeScript 基础配置
├── tsconfig.node.json    # TypeScript Node 配置
└── vite.config.ts        # Vite 配置
```

## Web 端核心文件说明

### 1. components/
可复用的 UI 组件。

- `FileItem.tsx`：单个文件项组件
- `FileList.tsx`：文件列表组件
- `FileUpload.tsx`：文件上传组件
- `Navbar.tsx`：导航栏组件

### 2. hooks/
自定义 React Hooks，用于状态管理和逻辑复用。

- `useFiles.ts`：文件管理 Hook，处理文件上传、删除等逻辑

### 3. pages/
页面级组件，每个页面对应一个路由。

- `FileManagementPage.tsx`：文件管理页面，包含文件上传和文件列表功能
- `FileProcessingPage.tsx`：文件处理页面，包含段落拆分设置和分段结果展示

### 4. services/
业务逻辑服务，处理数据请求和业务规则。

- `contentService.ts`：内容服务，处理内容的增删改查
- `fileService.ts`：文件服务，处理文件的上传、下载和删除
- `parseService.ts`：解析服务，处理文件内容的解析

### 5. types/
TypeScript 类型定义，统一管理应用中的类型。

- `index.ts`：包含所有类型定义，如 FileItem、ParseResult 等

### 6. utils/
工具函数，提供通用功能。

- `supabase.ts`：Supabase 客户端配置

## 技术栈

- **Web 端**：React 18 + TypeScript 5 + Vite 5 + Ant Design 5 + react-router-dom
- **Android 端**：原生 Android 开发
- **数据库**：Supabase (PostgreSQL)
- **文件存储**：Supabase Storage
- **构建工具**：Vite (Web)、Gradle (Android)

## 开发流程

1. **需求分析**：根据 PRD 文档分析需求
2. **UI 设计**：参考 prototypes 文件夹中的设计
3. **开发实现**：
   - Web 端：在 web/src 目录下开发
   - Android 端：在 android/app/src 目录下开发
4. **测试验证**：
   - Web 端：运行 `npm run dev` 启动开发服务器
   - Android 端：使用 Android Studio 运行
5. **构建部署**：
   - Web 端：运行 `npm run build` 构建生产版本
   - Android 端：使用 Android Studio 构建 APK

## 扩展建议

1. **增加页面**：在 web/src/pages 目录下创建新页面，在 App.tsx 中配置路由
2. **增加组件**：在 web/src/components 目录下创建新组件
3. **增加服务**：在 web/src/services 目录下创建新服务
4. **增加类型**：在 web/src/types/index.ts 中添加新类型
5. **增加自定义 Hook**：在 web/src/hooks 目录下创建新 Hook

## 注意事项

1. 所有 TypeScript 文件必须使用 `.ts` 或 `.tsx` 扩展名
2. 组件命名使用 PascalCase（如 FileUpload.tsx）
3. 函数和变量命名使用 camelCase
4. 类型命名使用 PascalCase
5. 所有组件必须添加类型定义
6. 代码必须通过 ESLint 检查
7. 提交代码前必须运行 `npm run build` 确保构建成功

## 更新记录

- 2026-01-25：初始创建，记录当前文件结构
- 2026-01-25：添加了 pages 目录，实现了路由管理
- 2026-01-25：优化了组件结构，分离了业务逻辑和 UI 组件
