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
