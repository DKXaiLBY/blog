<template>
  <div class="projects-layout">
    <div class="projects-header" v-fade-in>
      <h1>项目展示</h1>
      <p>我参与或独立开发的开源项目</p>
    </div>

    <div v-if="loading" class="loading-wrap">
      <div v-for="i in 3" :key="'s'+i" class="skeleton-card">
        <div class="sk-line sk-name"></div>
        <div class="sk-line sk-desc"></div>
        <div class="sk-line sk-desc short"></div>
      </div>
    </div>

    <template v-else-if="projects.length">
      <div class="projects-grid">
        <div
          v-for="(proj, idx) in projects"
          :key="idx"
          class="project-card"
          v-fade-in="idx * 80"
        >
          <div class="project-body">
            <h3 class="project-name">
              <a v-if="proj.url" :href="proj.url" target="_blank" rel="noopener">{{ proj.name }}</a>
              <span v-else>{{ proj.name }}</span>
            </h3>
            <p v-if="proj.description" class="project-desc">{{ proj.description }}</p>
            <div v-if="proj.tags?.length" class="project-tags">
              <span v-for="tag in proj.tags" :key="tag" class="project-tag">{{ tag }}</span>
            </div>
          </div>
          <div class="project-footer">
            <div v-if="proj.stars !== undefined" class="project-stat">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              {{ proj.stars }}
            </div>
            <a v-if="proj.url" :href="proj.url" target="_blank" rel="noopener" class="project-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
              访问项目
            </a>
          </div>
        </div>
      </div>
    </template>
    <el-empty v-else description="暂无项目" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserProfile } from '@/api/user'
import { useSEO } from '@/composables/useSEO'

const projects = ref([])
const loading = ref(false)

onMounted(async () => {
  useSEO({ title: '项目展示', description: 'DKX 的开源项目与作品展示' })
  loading.value = true
  try {
    const res = await getUserProfile()
    if (res.data?.projects) {
      try {
        projects.value = JSON.parse(res.data.projects)
      } catch { projects.value = [] }
    }
  } catch { /* ignore */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.projects-layout {
  max-width: 900px;
  margin: 0 auto;
  padding: 48px 24px;
}

.projects-header {
  text-align: center;
  margin-bottom: 40px;
}
.projects-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
}
.projects-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
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
.sk-name { height: 20px; width: 40%; }
.sk-desc { height: 14px; width: 100%; }
.sk-desc.short { width: 60%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Projects Grid */
.projects-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.project-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  transition: border-color 0.2s, transform 0.2s;
}
.project-card:hover {
  border-color: var(--accent);
  transform: translateY(-2px);
}

.project-body {
  flex: 1;
  min-width: 0;
}
.project-name {
  font-size: 17px;
  margin: 0 0 8px;
}
.project-name a {
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.2s;
}
.project-name a:hover {
  color: var(--accent);
}
.project-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 10px;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.project-tag {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 12px;
  background: var(--accent-bg);
  color: var(--accent);
}

.project-footer {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}
.project-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
}
.project-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--accent);
  text-decoration: none;
  transition: opacity 0.2s;
}
.project-link:hover {
  opacity: 0.8;
}

@media (max-width: 768px) {
  .projects-layout { padding: 32px 16px; }
  .project-card { flex-direction: column; }
  .project-footer { flex-direction: row; align-items: center; }
}
</style>
