# 博客系统前端页面设计

## 概述
基于 Vue 3 + Element Plus 构建个人博客前端，共 5 个页面。深色主题 + 传统侧边栏布局。

## 设计决策
- **布局**：传统博客（左侧内容 + 右侧侧边栏）
- **主题**：深色主题
- **组件库**：Element Plus（表格、表单、对话框、分页等）
- **Markdown**：v-md-editor（编辑器）+ marked（渲染）

## 页面清单

### 1. 首页 `Home.vue`
- 路由：`/`
- 布局：左侧文章列表 + 右侧侧边栏
- 文章列表：标题、摘要、发布时间、分类、标签、阅读量
- 侧边栏：搜索框（跳转搜索页）、分类列表（计数）、标签云
- 分页：Element Plus 分页组件
- 数据源：`GET /api/articles?page=&size=&categoryId=&tagId=`

### 2. 文章详情 `ArticleDetail.vue`
- 路由：`/article/:id`
- 顶部：文章标题、元信息（时间、分类、标签、阅读量）
- 正文：Markdown 渲染（marked 或 v-md-editor preview）
- 底部评论区：
  - 已有评论列表（支持嵌套回复，楼中楼）
  - 发表评论表单（昵称 + 内容），支持回复某条评论
- 数据源：`GET /api/articles/:id` + `GET /api/articles/:id/comments`
- 提交评论：`POST /api/comments`

### 3. 搜索页 `Search.vue`
- 路由：`/search?keyword=xxx`
- 与首页类似布局，按关键词搜索
- 数据源：`GET /api/articles/search?keyword=&page=&size=`

### 4. 登录页 `Login.vue`
- 路由：`/login`
- 居中卡片式登录表单（用户名 + 密码）
- 登录成功后存储 token 跳转后台
- 数据源：`POST /api/auth/login`

### 5. 后台管理 `admin/ArticleManage.vue`
- 路由：`/admin`（需要登录）
- 功能：
  - 文章表格（标题、分类、状态、发布时间、操作）
  - 新建/编辑：对话框内含 Markdown 编辑器（v-md-editor）
  - 删除：二次确认
  - 搜索过滤
- 数据源：文章 CRUD 接口

## 技术要点
- Element Plus 暗色模式：通过 CSS 变量覆盖或使用 Element Plus 暗色主题
- Token 管理：localStorage 存储，axios 拦截器自动携带
- 路由守卫：后台页需要 token，已登录用户访问登录页自动跳转后台（已实现）
- Markdown 代码高亮：highlight.js（已集成 github 主题，暗色下可换 dark 主题）

## 不涉及
- 用户注册/多用户
- 文件上传（封面图先用 URL）
- 评论审核/管理
- SEO/SSR
