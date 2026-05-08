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
