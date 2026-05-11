<template>
  <div class="archive-layout">
    <div class="archive-header" v-fade-in>
      <h1>文章归档</h1>
      <p>共 {{ totalCount }} 篇文章</p>
    </div>

    <div v-if="loading" class="loading-wrap">
      <div v-for="i in 3" :key="'s'+i" class="skeleton-card">
        <div class="sk-line sk-month"></div>
        <div class="sk-line sk-item"></div>
        <div class="sk-line sk-item short"></div>
      </div>
    </div>

    <template v-else-if="archives.length">
      <div v-for="item in archives" :key="item.month" class="timeline-group" v-fade-in>
        <div class="timeline-month">{{ item.month }}</div>
        <div class="timeline-count">{{ item.count }} 篇</div>
        <div class="timeline-list">
          <div
            v-for="article in item.articles"
            :key="article.id"
            class="timeline-item"
            @click="$router.push(`/article/${article.id}`)"
          >
            <span class="timeline-dot"></span>
            <div class="timeline-card">
              <span class="timeline-title">{{ article.title }}</span>
              <span class="timeline-date">{{ fmtDay(article.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
    <el-empty v-else description="暂无文章" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getArchives } from '@/api/article'
import { useSEO } from '@/composables/useSEO'

const archives = ref([])
const loading = ref(false)

const totalCount = computed(() => {
  return archives.value.reduce((sum, g) => sum + (g.count || 0), 0)
})

const fmtDay = (s) => {
  if (!s) return ''
  return s.substring(0, 10)
}

onMounted(async () => {
  useSEO({ title: '文章归档', description: '按时间线浏览所有文章' })
  loading.value = true
  try {
    const res = await getArchives()
    archives.value = res.data || []
  } catch { /* ignore */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.archive-layout {
  max-width: 860px;
  margin: 0 auto;
  padding: 48px 24px;
}

.archive-header {
  text-align: center;
  margin-bottom: 40px;
}
.archive-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
}
.archive-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

/* Skeleton */
.loading-wrap { min-height: 400px; }
.skeleton-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 28px;
}
.sk-line {
  background: linear-gradient(90deg, var(--border) 25%, transparent 50%, var(--border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 10px;
}
.sk-month { height: 20px; width: 30%; margin-bottom: 14px; }
.sk-item { height: 14px; width: 100%; }
.sk-item.short { width: 60%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Timeline */
.timeline-group {
  margin-bottom: 32px;
}
.timeline-month {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.timeline-count {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 14px;
}
.timeline-list {
  position: relative;
  padding-left: 20px;
}
.timeline-list::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: var(--border);
}
.timeline-item {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  margin-bottom: 12px;
  position: relative;
}
.timeline-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--accent);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  transition: transform 0.2s, box-shadow 0.2s;
}
.timeline-item:hover .timeline-dot {
  transform: scale(1.4);
  box-shadow: 0 0 8px var(--accent);
}
.timeline-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
  transition: border-color 0.2s, transform 0.2s;
}
.timeline-item:hover .timeline-card {
  border-color: var(--accent);
  transform: translateX(4px);
}
.timeline-title {
  font-size: 14px;
  color: var(--text-primary);
}
.timeline-date {
  font-size: 12px;
  color: var(--text-muted);
  flex-shrink: 0;
  margin-left: 12px;
}

@media (max-width: 768px) {
  .archive-layout { padding: 32px 16px; }
  .archive-header h1 { font-size: 22px; }
  .timeline-card { flex-direction: column; align-items: flex-start; gap: 4px; }
  .timeline-date { margin-left: 0; }
}
</style>
