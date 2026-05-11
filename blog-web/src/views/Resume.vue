<template>
  <div class="about-page" v-fade-in>
    <div class="about-hero">
      <el-avatar :size="80" :src="profile.avatar" class="hero-avatar">
        {{ profile.nickname?.charAt(0) }}
      </el-avatar>
      <h1>{{ profile.nickname }}</h1>
      <p class="hero-tagline">{{ profile.tagline || '热爱技术，专注 Java 与前端开发' }}</p>
    </div>

    <div class="about-grid">
      <div class="about-card" v-if="profile.bio">
        <h3>关于我</h3>
        <p v-for="(para, i) in bioParagraphs" :key="i">{{ para }}</p>
      </div>

      <div class="about-card" v-if="skills.length">
        <h3>技能栈</h3>
        <div class="skills-grid">
          <div v-for="(s, i) in skills" :key="i" class="skill-item">
            <span class="skill-dot" :style="{ background: s.color || 'var(--accent)' }"></span>
            <span>{{ s.name }}</span>
          </div>
        </div>
      </div>

      <div class="about-card">
        <h3>统计数据</h3>
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-number">{{ stats.articleCount }}</span>
            <span class="stat-label">文章</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats.categoryCount }}</span>
            <span class="stat-label">分类</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats.tagCount }}</span>
            <span class="stat-label">标签</span>
          </div>
        </div>
      </div>

      <div class="about-card" v-if="projectList.length">
        <h3>项目经验</h3>
        <div class="project-list">
          <div v-for="(p, i) in projectList" :key="i" class="project-item">
            <div class="project-top">
              <a v-if="p.url" :href="p.url" target="_blank" class="project-name">{{ p.name }}</a>
              <span v-else class="project-name">{{ p.name }}</span>
              <span v-if="p.stars" class="project-stars">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                {{ p.stars }}
              </span>
            </div>
            <p v-if="p.description" class="project-desc">{{ p.description }}</p>
            <div v-if="p.tags" class="project-tags">
              <span v-for="t in (typeof p.tags === 'string' ? p.tags.split(',').map(x => x.trim()) : p.tags)" :key="t" class="project-tag">{{ t }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="about-card" v-if="contacts.length">
        <h3>联系方式</h3>
        <div class="contact-list">
          <a v-for="(c, i) in contacts" :key="i" :href="c.href" target="_blank" class="contact-item">
            <svg v-if="c.icon === 'github'" width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/></svg>
            <el-icon v-else-if="c.icon === 'email'" :size="20"><Message /></el-icon>
            <svg v-else-if="c.icon === 'rss'" width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><circle cx="6.18" cy="17.82" r="2.18"/><path d="M4 4.44v2.83c7.03 0 12.73 5.7 12.73 12.73h2.83c0-8.59-6.97-15.56-15.56-15.56zm0 5.66v2.83c3.9 0 7.07 3.17 7.07 7.07h2.83c0-5.47-4.43-9.9-9.9-9.9z"/></svg>
            <span>{{ c.label }}</span>
          </a>
        </div>
      </div>
    </div>

    <div class="back-wrap">
      <el-button plain @click="printResume">打印 / 导出 PDF</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Message } from '@element-plus/icons-vue'
import { getUserProfile, getCategories, getTags } from '@/api/comment'
import { getArticles } from '@/api/article'
import { useSEO } from '@/composables/useSEO'

const profile = ref({ nickname: '博主', avatar: '', bio: '' })
const skills = ref([])
const contacts = ref([])
const projectList = ref([])
const stats = ref({ articleCount: 0, categoryCount: 0, tagCount: 0 })

const bioParagraphs = computed(() => {
  if (!profile.value.bio) return []
  return profile.value.bio.split('\n').filter(p => p.trim())
})

const printResume = () => {
  // Force light theme for printing then restore
  const html = document.documentElement
  const prevTheme = html.getAttribute('data-theme')
  html.setAttribute('data-theme', 'light')
  setTimeout(() => {
    window.print()
    setTimeout(() => {
      if (prevTheme) {
        html.setAttribute('data-theme', prevTheme)
      } else {
        html.removeAttribute('data-theme')
      }
    }, 100)
  }, 50)
}

onMounted(async () => {
  useSEO({ title: '关于博主', description: profile.value.tagline || '了解更多关于 DKX 的信息' })
  try {
    const [profileRes, catRes, tagRes, articleRes] = await Promise.all([
      getUserProfile(),
      getCategories(),
      getTags(),
      getArticles({ page: 1, size: 1 })
    ])
    if (profileRes.data) {
      profile.value = profileRes.data
      try { skills.value = JSON.parse(profileRes.data.skills || '[]') } catch { skills.value = [] }
      try { contacts.value = JSON.parse(profileRes.data.contacts || '[]') } catch { contacts.value = [] }
      try { projectList.value = JSON.parse(profileRes.data.projects || '[]') } catch { projectList.value = [] }
    }
    stats.value.categoryCount = catRes.data?.length || 0
    stats.value.tagCount = tagRes.data?.length || 0
    stats.value.articleCount = articleRes.data?.total || 0
  } catch { /* ignore */ }
})
</script>

<style scoped>
.about-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 48px 24px;
}

.about-hero {
  text-align: center;
  margin-bottom: 40px;
}
.hero-avatar {
  margin-bottom: 16px;
  border: 3px solid var(--accent);
}
.about-hero h1 {
  font-size: 28px;
  margin-bottom: 8px;
}
.hero-tagline {
  font-size: 15px;
  color: var(--text-muted);
}

.about-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 32px;
}

.about-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 24px;
}
.about-card h3 {
  font-size: 16px;
  margin-bottom: 14px;
  color: var(--text-primary);
}
.about-card p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 10px;
}

/* Skills */
.skills-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.skill-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}
.skill-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* Stats */
.stats-row {
  display: flex;
  gap: 24px;
  justify-content: center;
  text-align: center;
}
.stat-item {
  display: flex;
  flex-direction: column;
}
.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--accent);
}
.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 2px;
}

/* Contact */
.contact-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
}
.contact-item:hover {
  background: var(--accent-bg);
  color: var(--accent);
  text-decoration: none;
}

/* Projects */
.project-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.project-item {
  background: var(--bg-primary);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px 14px;
}
.project-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}
.project-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--accent);
  text-decoration: none;
}
a.project-name:hover { text-decoration: underline; }
.project-stars {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #fbbf24;
}
.project-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 4px 0 6px;
  line-height: 1.5;
}
.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.project-tag {
  font-size: 11px;
  color: var(--accent);
  background: var(--accent-bg);
  padding: 1px 7px;
  border-radius: 8px;
}

.back-wrap {
  text-align: center;
}

/* Print — temporarily switch to light theme before printing */
@media print {
  .nav-bar, .announce-bar, .back-wrap, .BackToTop, .ReadingProgress,
  .site-footer, .subscribe-box { display: none !important; }

  .about-page { padding: 0; max-width: 100%; }

  @page { margin: 18mm; size: A4; }
}

@media (max-width: 640px) {
  .about-grid { grid-template-columns: 1fr; }
  .skills-grid { grid-template-columns: 1fr 1fr; }
}
</style>
