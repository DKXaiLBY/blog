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

      <!-- Skeleton Loading -->
      <template v-if="loading">
        <div v-for="i in 3" :key="'s'+i" class="skeleton-card">
          <div class="sk-line sk-title"></div>
          <div class="sk-line sk-meta"></div>
          <div class="sk-line sk-text"></div>
          <div class="sk-line sk-text short"></div>
        </div>
      </template>

      <article
        v-for="(item, idx) in articles"
        :key="item.id"
        class="article-card"
        :class="{ 'is-pinned': item.isTop, 'has-cover': item.coverImage }"
        v-fade-in="idx * 80"
        @click="goDetail(item.id)"
      >
        <div v-if="item.coverImage" class="card-cover">
          <img :src="item.coverImage" alt="" loading="lazy" />
        </div>
        <div class="card-body">
          <div class="article-title-row">
            <span v-if="item.isTop" class="pin-badge">置顶</span>
            <span v-if="item.series" class="series-badge">{{ item.series }}</span>
            <h2 class="article-title" v-html="highlight(item.title)"></h2>
          </div>
          <div class="article-meta">
            <span>{{ fmtDate(item.createdAt) }}</span>
            <span>·</span>
            <span>{{ item.categoryName || '未分类' }}</span>
            <span>·</span>
            <span>{{ item.viewCount }} 阅读</span>
            <span>·</span>
            <span>{{ readingTime(item.content) }}</span>
            <template v-if="item.authorName">
              <span>·</span>
              <span>{{ item.authorName }}</span>
            </template>
          </div>
          <p v-if="item.summary" class="article-summary" v-html="highlight(item.summary)"></p>
          <div v-if="item.tagNames?.length" class="article-tags">
            <el-tag v-for="t in item.tagNames" :key="t" size="small" effect="dark">{{ t }}</el-tag>
          </div>
        </div>
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
import { useSEO } from '@/composables/useSEO'

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

const readingTime = (content) => {
  if (!content) return '1 分钟'
  const chars = content.replace(/[#*\-\s`>\[\]()!|~]/g, '').length
  const minutes = Math.max(1, Math.round(chars / 400))
  return `${minutes} 分钟`
}

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const goDetail = (id) => router.push(`/article/${id}`)

const highlight = (text) => {
  if (!text || !keyword.value.trim()) return text
  const escaped = keyword.value.trim().replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark>$1</mark>')
}

onMounted(() => {
  useSEO({ title: '搜索', description: '搜索 DKX 博客文章' })
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

/* Skeleton */
.skeleton-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 16px;
}
.sk-line {
  background: linear-gradient(90deg, var(--border) 25%, transparent 50%, var(--border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 10px;
}
.sk-title { height: 24px; width: 60%; margin-bottom: 14px; }
.sk-meta { height: 14px; width: 40%; }
.sk-text { height: 14px; width: 100%; }
.sk-text.short { width: 70%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Article Card */
.article-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
  overflow: hidden;
}
.article-card:hover {
  border-color: var(--accent);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15), 0 0 0 1px var(--accent) inset;
}
.article-card.has-cover {
  display: flex;
  padding: 0;
}
.card-cover {
  width: 200px;
  flex-shrink: 0;
  overflow: hidden;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.article-card:hover .card-cover img {
  transform: scale(1.05);
}
.card-body {
  padding: 20px 24px;
  flex: 1;
  min-width: 0;
}

.article-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.pin-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 3px;
  background: var(--accent);
  color: #fff;
  flex-shrink: 0;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.article-card.is-pinned {
  border-left: 3px solid var(--accent);
}
.series-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 3px;
  background: var(--accent-bg);
  color: var(--accent);
  flex-shrink: 0;
  font-weight: 500;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.article-title { font-size: 20px; margin: 0; }

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

@media (max-width: 768px) {
  .search-layout { padding: 16px; }
}
</style>
