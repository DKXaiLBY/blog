<template>
  <div class="dashboard" v-fade-in>
    <h2 class="page-title">仪表盘</h2>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: var(--accent-bg); color: var(--accent)">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.publishedArticles }}</span>
          <span class="stat-label">已发布文章</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(251,191,36,0.1); color: #fbbf24">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.draftArticles }}</span>
          <span class="stat-label">草稿</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(74,222,128,0.1); color: #4ade80">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.totalComments }}</span>
          <span class="stat-label">评论</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(96,165,250,0.1); color: #60a5fa">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.totalViews }}</span>
          <span class="stat-label">总阅读量</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(244,114,182,0.1); color: #f472b6">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.totalLikes }}</span>
          <span class="stat-label">获赞</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(251,146,60,0.1); color: #fb923c">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ fmtNum(stats.totalWords) }}</span>
          <span class="stat-label">总字数</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(168,85,247,0.1); color: #a855f7">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <line x1="7" y1="7" x2="7.01" y2="7"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.totalTags }}</span>
          <span class="stat-label">标签 / 分类</span>
        </div>
        <div class="stat-sub">{{ stats.totalCategories }} 分类</div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(52,211,153,0.1); color: #34d399">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ stats.thisWeekArticles || 0 }}</span>
          <span class="stat-label">本周新增</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(56,189,248,0.1); color: #38bdf8">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num">{{ fmtNum(stats.avgViews) }}</span>
          <span class="stat-label">平均阅读</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(244,114,182,0.1); color: #f472b6">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-num" style="font-size:18px">{{ stats.topCategory || '-' }}</span>
          <span class="stat-label">热门分类</span>
        </div>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="charts-row">
      <!-- Monthly trend -->
      <div class="chart-card" v-if="monthlyKeys.length">
        <h3>月度发布趋势</h3>
        <div class="bar-chart">
          <div v-for="key in monthlyKeys" :key="key" class="bar-col">
            <div class="bar-wrapper">
              <div
                class="bar-fill"
                :style="{ height: barHeight(key) + '%' }"
                :title="monthlyTrend[key] + ' 篇'"
              ></div>
            </div>
            <span class="bar-value">{{ monthlyTrend[key] }}</span>
            <span class="bar-label">{{ key.slice(5) }}月</span>
          </div>
        </div>
      </div>

      <!-- Top articles -->
      <div class="chart-card" v-if="topArticles.length">
        <h3>热门文章 Top 5</h3>
        <div class="top-list">
          <div
            v-for="(item, idx) in topArticles"
            :key="item.id"
            class="top-item"
            @click="$router.push(`/article/${item.id}`)"
          >
            <span class="top-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
            <span class="top-title">{{ item.title }}</span>
            <span class="top-views">{{ item.viewCount }} 阅读</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Publish Heatmap -->
    <div class="chart-card" v-if="heatmapDates.length">
      <h3>发布热力图 (近半年)</h3>
      <div class="heatmap">
        <div v-for="(cell, i) in heatmapDates" :key="i"
             class="heat-cell"
             :class="'level-' + cell.level"
             :title="cell.date + (cell.count ? ': ' + cell.count + ' 篇' : ': 无')"></div>
      </div>
      <div class="heatmap-legend">
        <span>少</span>
        <span class="legend-cell level-0"></span>
        <span class="legend-cell level-1"></span>
        <span class="legend-cell level-2"></span>
        <span class="legend-cell level-3"></span>
        <span class="legend-cell level-4"></span>
        <span>多</span>
      </div>
    </div>

    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="actions-row">
        <router-link to="/admin/articles" class="action-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          写文章
        </router-link>
        <router-link to="/admin/comments" class="action-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          管理评论
        </router-link>
        <router-link to="/admin/profile" class="action-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          个人设置
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboard } from '@/api/user'

const stats = ref({
  publishedArticles: 0,
  draftArticles: 0,
  totalComments: 0,
  totalCategories: 0,
  totalTags: 0,
  totalViews: 0,
  totalLikes: 0
})

const monthlyTrend = ref({})
const topArticles = ref([])
const publishDates = ref([])
const heatmapDates = computed(() => {
  if (!publishDates.value.length) return []
  const countMap = {}
  publishDates.value.forEach(d => { countMap[d] = (countMap[d] || 0) + 1 })
  const maxCount = Math.max(1, ...Object.values(countMap))
  const today = new Date()
  const cells = []
  for (let i = 180; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    const key = d.toISOString().slice(0, 10)
    const count = countMap[key] || 0
    const level = count === 0 ? 0 : Math.ceil(count / maxCount * 4)
    cells.push({ date: key, count, level })
  }
  return cells
})

const monthlyKeys = computed(() => Object.keys(monthlyTrend.value))

const fmtNum = (n) => {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toLocaleString()
}

const barHeight = (key) => {
  const max = Math.max(...Object.values(monthlyTrend.value), 1)
  return Math.round((monthlyTrend.value[key] / max) * 100)
}

onMounted(async () => {
  try {
    const res = await getDashboard()
    if (res.data) {
      stats.value = res.data
      monthlyTrend.value = res.data.monthlyTrend || {}
      topArticles.value = res.data.topArticles || []
      publishDates.value = res.data.publishDates || []
    }
  } catch { /* ignore */ }
})
</script>

<style scoped>
.page-title {
  font-size: 22px;
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: border-color 0.2s, transform 0.2s;
}
.stat-card:hover {
  border-color: var(--accent);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-num {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
}
.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 2px;
}
.stat-sub {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

/* Charts */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 32px;
}

.chart-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px;
}
.chart-card h3 {
  font-size: 15px;
  margin-bottom: 20px;
}

/* Bar chart */
.bar-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 180px;
  gap: 6px;
}
.bar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
}
.bar-wrapper {
  width: 100%;
  max-width: 40px;
  height: 140px;
  display: flex;
  align-items: flex-end;
  background: var(--bg-tertiary);
  border-radius: 4px 4px 0 0;
  overflow: hidden;
}
.bar-fill {
  width: 100%;
  background: linear-gradient(180deg, var(--accent), rgba(192,132,252,0.4));
  border-radius: 4px 4px 0 0;
  transition: height 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 4px;
}
.bar-value {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
}
.bar-label {
  font-size: 11px;
  color: var(--text-muted);
}

/* Top articles */
.top-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.top-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}
.top-item:hover {
  background: var(--bg-tertiary);
}
.top-rank {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: var(--bg-tertiary);
  color: var(--text-muted);
  flex-shrink: 0;
}
.top-rank.rank-1 { background: #fbbf24; color: #000; }
.top-rank.rank-2 { background: #9ca3af; color: #000; }
.top-rank.rank-3 { background: #d97706; color: #fff; }
.top-title {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.top-views {
  font-size: 12px;
  color: var(--text-muted);
  flex-shrink: 0;
}

/* Quick actions */
.quick-actions h3 {
  font-size: 16px;
  margin-bottom: 14px;
}

.actions-row {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
}
.action-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-bg);
}

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
  .actions-row { flex-direction: column; }
}

/* Heatmap */
.heatmap {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
  margin-top: 12px;
}
.heat-cell {
  width: 14px;
  height: 14px;
  border-radius: 2px;
  background: var(--bg-tertiary);
}
.heat-cell.level-1 { background: rgba(74,222,128,0.25); }
.heat-cell.level-2 { background: rgba(34,197,94,0.5); }
.heat-cell.level-3 { background: rgba(34,197,94,0.75); }
.heat-cell.level-4 { background: #22c55e; }
.heatmap-legend {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-muted);
}
.legend-cell {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}
.legend-cell.level-0 { background: var(--bg-tertiary); }
.legend-cell.level-1 { background: rgba(74,222,128,0.25); }
.legend-cell.level-2 { background: rgba(34,197,94,0.5); }
.legend-cell.level-3 { background: rgba(34,197,94,0.75); }
.legend-cell.level-4 { background: #22c55e; }
</style>
