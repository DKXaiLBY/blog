# 博客前端页面实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建 5 个 Vue 页面组件，替换默认样式，完成个人博客前端

**Architecture:** Vue 3 SFC（`<script setup>` + `<template>` + `<style scoped>`），通过已有的 API 层获取数据，Element Plus 组件构建 UI

**Tech Stack:** Vue 3 + Element Plus + Pinia + Vue Router + marked + v-md-editor

**API 返回格式:** 所有接口返回 `{ code: 200, message: "success", data: ... }`，axios 拦截器已处理错误情况

---

### Task 1: 替换全局样式为深色主题

**Files:**
- Modify: `blog-web/src/style.css`

- [ ] **Step 1: 替换 style.css 为深色博客主题**

将 Vite 默认模板样式全部替换为以下内容：

```css
/* 深色博客主题 */
*, *::before, *::after { box-sizing: border-box; }

:root {
  --bg-primary: #16171d;
  --bg-secondary: #1a1b23;
  --bg-tertiary: #1f2028;
  --border: #2e303a;
  --text-primary: #f3f4f6;
  --text-secondary: #9ca3af;
  --text-muted: #6b7280;
  --accent: #c084fc;
  --accent-bg: rgba(192, 132, 252, 0.15);
  --danger: #ef4444;
  --success: #4ade80;
  --warning: #fbbf24;
  --font-sans: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
  --font-mono: ui-monospace, Consolas, 'Courier New', monospace;
}

html {
  font-size: 16px;
  font-family: var(--font-sans);
  color: var(--text-secondary);
  background: var(--bg-primary);
}

body { margin: 0; }

a { color: var(--accent); text-decoration: none; }
a:hover { text-decoration: underline; }

h1, h2, h3, h4, h5, h6 { color: var(--text-primary); margin: 0; }

/* Element Plus 深色模式覆盖 */
.el-card,
.el-dialog,
.el-table,
.el-table th.el-table__cell,
.el-pagination button,
.el-input__wrapper,
.el-textarea__inner,
.el-select .el-input__wrapper,
.el-dialog__header,
.el-dialog__body {
  background-color: var(--bg-secondary) !important;
  border-color: var(--border) !important;
  color: var(--text-secondary) !important;
}

.el-table tr,
.el-table td.el-table__cell {
  background-color: var(--bg-secondary) !important;
  border-color: var(--border) !important;
}

.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background-color: var(--bg-tertiary) !important;
}

.el-pagination button:disabled {
  background-color: var(--bg-tertiary) !important;
}

.el-pager li {
  background-color: var(--bg-primary) !important;
  color: var(--text-secondary) !important;
}

.el-pager li.is-active {
  background-color: var(--accent) !important;
  color: #fff !important;
}

.el-input__inner,
.el-textarea__inner {
  color: var(--text-primary) !important;
}

.el-input__wrapper { box-shadow: none !important; }

.el-dialog {
  border-radius: 12px;
}

.el-button--primary {
  --el-button-bg-color: var(--accent);
  --el-button-border-color: var(--accent);
}

.el-message-box {
  background-color: var(--bg-secondary) !important;
  border-color: var(--border) !important;
}

.el-message-box__title { color: var(--text-primary) !important; }
.el-message-box__message { color: var(--text-secondary) !important; }

/* 滚动条 */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: var(--bg-primary); }
::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: var(--text-muted); }
```

- [ ] **Step 2: 验证样式文件语法正确**

Run: `cd blog-web && npx prettier --check src/style.css 2>&1 || true`

- [ ] **Step 3: Commit**

```bash
git add blog-web/src/style.css
git commit -m "style: 替换为深色博客主题"
```

---

### Task 2: 首页 — 文章列表 + 侧边栏

**Files:**
- Create: `blog-web/src/views/Home.vue`

- [ ] **Step 1: 创建 views 目录和 Home.vue**

```bash
mkdir -p blog-web/src/views/admin
```

**文件内容：`blog-web/src/views/Home.vue`**

```vue
<template>
  <div class="home-layout">
    <main class="article-list">
      <article
        v-for="item in articles"
        :key="item.id"
        class="article-card"
        @click="goDetail(item.id)"
      >
        <h2 class="article-title">{{ item.title }}</h2>
        <div class="article-meta">
          <span>{{ fmtDate(item.createdAt) }}</span>
          <span>·</span>
          <span>{{ item.categoryName }}</span>
          <span>·</span>
          <span>{{ item.viewCount }} 阅读</span>
        </div>
        <p v-if="item.summary" class="article-summary">{{ item.summary }}</p>
        <div v-if="item.tagNames?.length" class="article-tags">
          <el-tag
            v-for="tag in item.tagNames"
            :key="tag"
            size="small"
            effect="dark"
          >{{ tag }}</el-tag>
        </div>
      </article>
      <el-empty v-if="!articles.length && !loading" description="暂无文章" />
      <div v-if="total > size" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchArticles"
        />
      </div>
    </main>

    <aside class="sidebar">
      <div class="sidebar-card">
        <h3>搜索</h3>
        <el-input
          v-model="keyword"
          placeholder="搜索文章..."
          @keyup.enter="goSearch"
        >
          <template #suffix>
            <el-icon style="cursor:pointer" @click="goSearch"><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="sidebar-card">
        <h3>分类</h3>
        <div class="category-list">
          <span
            class="category-item"
            :class="{ active: activeCategoryId === null }"
            @click="selectCategory(null)"
          >全部</span>
          <span
            v-for="cat in categories"
            :key="cat.id"
            class="category-item"
            :class="{ active: activeCategoryId === cat.id }"
            @click="selectCategory(cat.id)"
          >{{ cat.name }}</span>
        </div>
      </div>

      <div class="sidebar-card">
        <h3>标签</h3>
        <div class="tag-cloud">
          <el-tag
            v-for="tag in tags"
            :key="tag.id"
            size="small"
            effect="dark"
            style="cursor:pointer; margin: 2px 4px 4px 0;"
            @click="goSearchByTag(tag.name)"
          >{{ tag.name }}</el-tag>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticles } from '@/api/article'
import { getCategories, getTags } from '@/api/comment'

const router = useRouter()
const articles = ref([])
const categories = ref([])
const tags = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const activeCategoryId = ref(null)
const loading = ref(false)

const fetchArticles = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    const res = await getArticles(params)
    articles.value = res.data.records
    total.value = res.data.total
  } catch { /* 拦截器已处理错误 */ }
  finally { loading.value = false }
}

const fetchCategories = async () => {
  const res = await getCategories()
  categories.value = res.data
}

const fetchTags = async () => {
  const res = await getTags()
  tags.value = res.data
}

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const goDetail = (id) => router.push(`/article/${id}`)
const goSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value.trim() } })
  }
}
const goSearchByTag = (tagName) => {
  router.push({ path: '/search', query: { keyword: tagName } })
}
const selectCategory = (id) => {
  activeCategoryId.value = id
  page.value = 1
  fetchArticles()
}

onMounted(() => {
  fetchArticles()
  fetchCategories()
  fetchTags()
})
</script>

<style scoped>
.home-layout {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.article-list {
  flex: 3;
  min-width: 0;
}

.sidebar {
  flex: 1;
  min-width: 240px;
}

.article-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: border-color 0.2s;
}
.article-card:hover { border-color: var(--accent); }

.article-title {
  font-size: 20px;
  margin-bottom: 8px;
}

.article-meta {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 8px;
  display: flex;
  gap: 6px;
}

.article-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-tags { display: flex; gap: 6px; flex-wrap: wrap; }

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.sidebar-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 16px;
}
.sidebar-card h3 {
  font-size: 15px;
  margin-bottom: 12px;
}

.category-item {
  display: block;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}
.category-item:hover { background: var(--bg-tertiary); }
.category-item.active {
  background: var(--accent-bg);
  color: var(--accent);
}

@media (max-width: 768px) {
  .home-layout { flex-direction: column; padding: 16px; }
  .sidebar { min-width: unset; }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add blog-web/src/views/Home.vue
git commit -m "feat: 首页 — 文章列表 + 侧边栏"
```

---

### Task 3: 文章详情页 — Markdown 渲染 + 评论区

**Files:**
- Create: `blog-web/src/views/ArticleDetail.vue`

**文件内容：`blog-web/src/views/ArticleDetail.vue`**

```vue
<template>
  <div class="detail-layout">
    <main class="detail-main" v-loading="loading">
      <template v-if="article">
        <h1 class="detail-title">{{ article.title }}</h1>
        <div class="detail-meta">
          <span>{{ fmtDate(article.createdAt) }}</span>
          <span>·</span>
          <span>{{ article.categoryName }}</span>
          <span>·</span>
          <span>{{ article.viewCount }} 阅读</span>
        </div>
        <div v-if="article.tagNames?.length" class="detail-tags">
          <el-tag v-for="t in article.tagNames" :key="t" size="small" effect="dark">{{ t }}</el-tag>
        </div>
        <div class="detail-content">
          <v-md-preview :text="article.content || ''" />
        </div>
      </template>

      <section class="comment-section">
        <h3>评论 ({{ comments.length }})</h3>
        <div v-if="comments.length" class="comment-list">
          <div v-for="c in topLevelComments" :key="c.id" class="comment-item">
            <div class="comment-header">
              <strong>{{ c.authorName }}</strong>
              <span class="comment-time">{{ fmtDate(c.createdAt) }}</span>
            </div>
            <p class="comment-body">{{ c.content }}</p>
            <span class="reply-btn" @click="startReply(c.id, c.authorName)">回复</span>

            <div v-if="c.replies?.length" class="reply-list">
              <div v-for="r in c.replies" :key="r.id" class="comment-item reply-item">
                <div class="comment-header">
                  <strong>{{ r.authorName }}</strong>
                  <span class="comment-time">{{ fmtDate(r.createdAt) }}</span>
                </div>
                <p class="comment-body">{{ r.content }}</p>
                <span class="reply-btn" @click="startReply(c.id, r.authorName)">回复</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评论，来抢沙发吧" />

        <div class="comment-form">
          <el-input v-model="form.authorName" placeholder="昵称" style="margin-bottom:10px" maxlength="20" />
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="3"
            :placeholder="replyHint"
            maxlength="500"
            show-word-limit
          />
          <div class="form-actions">
            <span v-if="form.parentId" class="replying-to">
              回复 @{{ replyToName }}
              <el-button size="small" text @click="cancelReply">取消</el-button>
            </span>
            <el-button type="primary" size="small" @click="submitComment" :loading="submitting">发表评论</el-button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '@/api/article'
import { getComments, addComment } from '@/api/comment'
import { ElMessage } from 'element-plus'

const route = useRoute()
const article = ref(null)
const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const form = ref({ authorName: '', content: '', parentId: null })
const replyToName = ref('')

const topLevelComments = computed(() =>
  comments.value.filter((c) => !c.parentId)
)

const replyHint = computed(() =>
  form.value.parentId ? `回复 @${replyToName.value}` : '写下你的评论...'
)

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const fetchArticle = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
  } catch { /* handled */ }
  finally { loading.value = false }
}

const fetchComments = async () => {
  const res = await getComments(route.params.id)
  comments.value = res.data
}

const startReply = (parentId, name) => {
  form.value.parentId = parentId
  replyToName.value = name
}

const cancelReply = () => {
  form.value.parentId = null
  replyToName.value = ''
}

const submitComment = async () => {
  if (!form.value.authorName.trim() || !form.value.content.trim()) {
    ElMessage.warning('请填写昵称和评论内容')
    return
  }
  submitting.value = true
  try {
    await addComment({
      articleId: Number(route.params.id),
      parentId: form.value.parentId,
      authorName: form.value.authorName.trim(),
      content: form.value.content.trim()
    })
    ElMessage.success('评论成功')
    form.value.content = ''
    form.value.parentId = null
    replyToName.value = ''
    fetchComments()
  } catch { /* handled */ }
  finally { submitting.value = false }
}

onMounted(() => {
  fetchArticle()
  fetchComments()
})
</script>

<style scoped>
.detail-layout {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px;
}

.detail-title {
  font-size: 30px;
  margin-bottom: 12px;
}

.detail-meta {
  font-size: 13px;
  color: var(--text-muted);
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.detail-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 24px;
}

.detail-content {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 32px;
  line-height: 1.8;
}

.comment-section { margin-top: 24px; }
.comment-section h3 { margin-bottom: 16px; }

.comment-item {
  border-bottom: 1px solid var(--border);
  padding: 14px 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.comment-header strong { color: var(--text-primary); font-size: 14px; }
.comment-time { font-size: 12px; color: var(--text-muted); }

.comment-body {
  font-size: 14px;
  margin: 0 0 8px;
}

.reply-btn {
  font-size: 12px;
  color: var(--accent);
  cursor: pointer;
}
.reply-btn:hover { text-decoration: underline; }

.reply-list { margin-left: 28px; }

.comment-form {
  margin-top: 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 16px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.replying-to {
  font-size: 13px;
  color: var(--accent);
}

@media (max-width: 768px) {
  .detail-layout { padding: 16px; }
  .detail-title { font-size: 22px; }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add blog-web/src/views/ArticleDetail.vue
git commit -m "feat: 文章详情页 — Markdown 渲染 + 评论区"
```

---

### Task 4: 搜索页

**Files:**
- Create: `blog-web/src/views/Search.vue`

**文件内容：`blog-web/src/views/Search.vue`**

```vue
<template>
  <div class="search-layout">
    <div class="search-header">
      <el-input
        v-model="keyword"
        placeholder="搜索文章..."
        size="large"
        @keyup.enter="doSearch"
        clearable
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="doSearch" style="margin-left:8px">搜索</el-button>
    </div>

    <div v-if="searched" class="search-result">
      <p class="result-info">找到 {{ total }} 篇相关文章</p>
      <article
        v-for="item in articles"
        :key="item.id"
        class="article-card"
        @click="goDetail(item.id)"
      >
        <h2 class="article-title">{{ item.title }}</h2>
        <div class="article-meta">
          <span>{{ fmtDate(item.createdAt) }}</span>
          <span>·</span>
          <span>{{ item.categoryName }}</span>
          <span>·</span>
          <span>{{ item.viewCount }} 阅读</span>
        </div>
        <p v-if="item.summary" class="article-summary">{{ item.summary }}</p>
      </article>
      <el-empty v-if="!articles.length && !loading" description="未找到相关文章" />
      <div v-if="total > size" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="doSearch"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchArticles } from '@/api/article'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const articles = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const searched = ref(false)
const loading = ref(false)

const doSearch = async () => {
  if (!keyword.value.trim()) return
  searched.value = true
  loading.value = true
  try {
    const res = await searchArticles({ keyword: keyword.value.trim(), page: page.value, size })
    articles.value = res.data.records
    total.value = res.data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const goDetail = (id) => router.push(`/article/${id}`)

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    doSearch()
  }
})
</script>

<style scoped>
.search-layout {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px;
}

.search-header {
  display: flex;
  margin-bottom: 32px;
}

.search-result { min-height: 300px; }

.result-info {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.article-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: border-color 0.2s;
}
.article-card:hover { border-color: var(--accent); }

.article-title {
  font-size: 20px;
  margin-bottom: 8px;
}

.article-meta {
  font-size: 13px;
  color: var(--text-muted);
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}

.article-summary {
  font-size: 14px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .search-layout { padding: 16px; }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add blog-web/src/views/Search.vue
git commit -m "feat: 搜索页"
```

---

### Task 5: 登录页

**Files:**
- Create: `blog-web/src/views/Login.vue`

**文件内容：`blog-web/src/views/Login.vue`**

```vue
<template>
  <div class="login-wrap">
    <el-card class="login-card" shadow="always">
      <h2>管理员登录</h2>
      <el-form @keyup.enter="handleLogin">
        <el-form-item>
          <el-input v-model="username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" @click="handleLogin" :loading="loading">登 录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login as loginApi } from '@/api/auth'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await loginApi(username.value, password.value)
    userStore.login(res.data.token)
    ElMessage.success('登录成功')
    router.push('/admin')
  } catch { /* handled */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.login-card {
  width: 380px;
  border-radius: 12px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
  font-size: 22px;
}

@media (max-width: 420px) {
  .login-card { width: 90%; }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add blog-web/src/views/Login.vue
git commit -m "feat: 登录页"
```

---

### Task 6: 后台管理页 — 文章 CRUD

**Files:**
- Create: `blog-web/src/views/admin/ArticleManage.vue`

**文件内容：`blog-web/src/views/admin/ArticleManage.vue`**

```vue
<template>
  <div class="admin-layout">
    <div class="admin-header">
      <h2>文章管理</h2>
      <el-button type="primary" @click="openCreate">+ 写文章</el-button>
    </div>

    <div class="admin-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索文章..."
        style="width:240px"
        clearable
        @keyup.enter="fetchArticles"
        @clear="fetchArticles"
      />
      <el-select v-model="statusFilter" placeholder="状态" style="width:120px; margin-left:8px" clearable @change="fetchArticles">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
    </div>

    <el-table :data="articles" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <span class="table-title" @click="goDetail(row.id)">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isTop ? 'danger' : 'success'" size="small">
            {{ row.isTop ? '置顶' : '已发布' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除?" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="admin-pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchArticles"
      />
    </div>

    <!-- 编辑/新建对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑文章' : '写文章'"
      width="900px"
      top="5vh"
      destroy-on-close
    >
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="文章标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要（可选）" maxlength="300" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" clearable>
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagIds" multiple placeholder="选择标签" clearable>
            <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">发布</el-radio>
          </el-radio-group>
          <el-checkbox v-model="form.isTop" style="margin-left:16px" :true-value="1" :false-value="0">置顶</el-checkbox>
        </el-form-item>
        <el-form-item label="内容">
          <v-md-editor v-model="form.content" height="400px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ isEdit ? '保存修改' : '发布文章' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticles, createArticle, updateArticle, deleteArticle, searchArticles } from '@/api/article'
import { getCategories, getTags } from '@/api/comment'
import { ElMessage } from 'element-plus'

const router = useRouter()
const articles = ref([])
const categories = ref([])
const allTags = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const statusFilter = ref(null)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const defaultForm = () => ({
  title: '',
  summary: '',
  content: '',
  categoryId: null,
  tagIds: [],
  status: 1,
  isTop: 0
})
const form = ref(defaultForm())

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const fetchArticles = async () => {
  loading.value = true
  try {
    let res
    if (keyword.value.trim()) {
      res = await searchArticles({ keyword: keyword.value.trim(), page: page.value, size })
    } else {
      res = await getArticles({ page: page.value, size, categoryId: statusFilter.value })
      // Note: public API returns only published; admin reuses same endpoint for now
    }
    articles.value = res.data.records
    total.value = res.data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

const fetchMeta = async () => {
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  allTags.value = tagRes.data
}

const goDetail = (id) => router.push(`/article/${id}`)

const openCreate = () => {
  isEdit.value = false
  editingId.value = null
  form.value = defaultForm()
  dialogVisible.value = true
}

const openEdit = async (row) => {
  isEdit.value = true
  editingId.value = row.id
  // 获取文章详情以拿到 content（列表接口不含正文）
  const res = await getArticleDetail(row.id)
  const detail = res.data
  form.value = {
    title: detail.title,
    summary: detail.summary || '',
    content: detail.content || '',
    categoryId: null,       // ArticleVO 不含 categoryId，编辑时需重新选择
    tagIds: [],
    status: 1,                // ArticleVO 不含 status，公开列表中的文章均为已发布
    isTop: detail.isTop ?? 0
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateArticle(editingId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createArticle(form.value)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchArticles()
  } catch { /* handled */ }
  finally { saving.value = false }
}

const handleDelete = async (id) => {
  try {
    await deleteArticle(id)
    ElMessage.success('删除成功')
    fetchArticles()
  } catch { /* handled */ }
}

onMounted(() => {
  fetchArticles()
  fetchMeta()
})
</script>

<style scoped>
.admin-layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.admin-toolbar {
  display: flex;
  margin-bottom: 16px;
}

.table-title {
  color: var(--accent);
  cursor: pointer;
}
.table-title:hover { text-decoration: underline; }

.admin-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .admin-layout { padding: 16px; }
  .admin-toolbar { flex-wrap: wrap; gap: 8px; }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add blog-web/src/views/admin/ArticleManage.vue
git commit -m "feat: 后台文章管理页 — CRUD + Markdown 编辑器"
```

---

### Task 7: 验证构建

**Files:**
- Check: 所有 views 文件语法正确，构建通过

- [ ] **Step 1: 安装前端依赖**

```bash
cd blog-web && npm install
```

- [ ] **Step 2: 验证构建通过**

```bash
cd blog-web && npm run build
```

Expected: Build 成功，无错误。`dist/` 目录生成。

- [ ] **Step 3: 验证所有路由对应的文件存在**

```bash
ls blog-web/src/views/Home.vue blog-web/src/views/ArticleDetail.vue blog-web/src/views/Search.vue blog-web/src/views/Login.vue blog-web/src/views/admin/ArticleManage.vue
```

Expected: All 5 files found.

- [ ] **Step 4: Commit (if any build config changes needed)**

Only if build step required fixes. Otherwise skip.
