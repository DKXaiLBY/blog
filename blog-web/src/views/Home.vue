<template>
  <div class="home-layout">
    <main class="article-list">
      <!-- Hero Banner -->
      <div v-if="featured && !loading" class="hero-banner" @click="goDetail(featured.id)">
        <div class="hero-badge">精选推荐</div>
        <h2 class="hero-title">{{ featured.title }}</h2>
        <p class="hero-summary">{{ featured.summary }}</p>
        <div class="hero-meta">
          <span>{{ fmtDate(featured.createdAt) }}</span>
          <span>·</span>
          <span>{{ featured.categoryName || '未分类' }}</span>
          <span>·</span>
          <span>{{ readingTime(featured.content) }} 阅读</span>
        </div>
      </div>

      <!-- Skeleton Loading -->
      <template v-if="loading">
        <div v-for="i in 3" :key="'s'+i" class="skeleton-card">
          <div class="sk-line sk-title"></div>
          <div class="sk-line sk-meta"></div>
          <div class="sk-line sk-text"></div>
          <div class="sk-line sk-text short"></div>
        </div>
      </template>

      <!-- Article Cards -->
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
            <h2 class="article-title">{{ item.title }}</h2>
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
          <p v-if="item.summary" class="article-summary">{{ item.summary }}</p>
          <div v-if="item.tagNames?.length" class="article-tags">
            <el-tag
              v-for="tag in item.tagNames"
              :key="tag"
              size="small"
              effect="dark"
            >{{ tag }}</el-tag>
          </div>
        </div>
      </article>

      <el-empty v-if="!articles.length && !loading" description="暂无文章" />
      <div v-if="hasMore" class="load-more-wrap">
        <el-button :loading="loadingMore" @click="loadMore" size="large" round>加载更多</el-button>
      </div>
    </main>

    <aside class="sidebar">
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
          >{{ cat.name }} <span class="count">({{ cat.articleCount }})</span></span>
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
          >{{ tag.name }} ({{ tag.articleCount }})</el-tag>
        </div>
      </div>

      <div v-if="readingHistory.length" class="sidebar-card">
        <h3>最近阅读</h3>
        <div class="history-list">
          <div
            v-for="item in readingHistory"
            :key="item.id"
            class="history-item"
            @click="goDetail(item.id)"
          >
            <span class="history-dot"></span>
            <span class="history-title">{{ item.title }}</span>
          </div>
        </div>
      </div>

      <SubscribeBox />

      <div class="sidebar-card about-card">
        <h3>关于我</h3>
        <div class="about-content">
          <div class="about-avatar" @click="triggerUpload" :title="isAdmin ? '点击更换头像' : ''">
            <el-avatar :size="56" :src="profile.avatar" class="avatar-img">
              {{ profile.nickname?.charAt(0) }}
            </el-avatar>
            <div v-if="isAdmin" class="avatar-overlay">
              <el-icon :size="14"><Camera /></el-icon>
            </div>
          </div>
          <div class="about-name">{{ profile.nickname }}</div>
          <div class="about-desc">
            <span class="typewriter">{{ displayText }}</span><span class="cursor">|</span>
          </div>
          <div class="about-contact">
            <a v-for="item in contacts" :key="item.label" :href="item.href" :title="item.label" target="_blank" class="contact-icon">
              <svg v-if="item.icon === 'github'" width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/></svg>
              <el-icon v-else-if="item.icon === 'email'" :size="18"><Message /></el-icon>
              <svg v-else-if="item.icon === 'rss'" width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><circle cx="6.18" cy="17.82" r="2.18"/><path d="M4 4.44v2.83c7.03 0 12.73 5.7 12.73 12.73h2.83c0-8.59-6.97-15.56-15.56-15.56zm0 5.66v2.83c3.9 0 7.07 3.17 7.07 7.07h2.83c0-5.47-4.43-9.9-9.9-9.9z"/></svg>
            </a>
          </div>
        </div>
      </div>
    </aside>

    <!-- Hidden file input for avatar upload -->
    <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { getArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { getUserProfile, updateAvatar } from '@/api/user'
import { getFeaturedArticle } from '@/api/featured'
import { useSEO } from '@/composables/useSEO'
import { useReadingHistory } from '@/composables/useReadingHistory'
import { useUserStore } from '@/store/user'
import SubscribeBox from '@/components/SubscribeBox.vue'

const contacts = ref([
  { label: 'GitHub', icon: 'github', href: 'https://github.com/DKXaiLBY' },
  { label: '邮箱', icon: 'email', href: 'https://mail.qq.com/' },
  { label: 'RSS', icon: 'rss', href: '/api/rss' }
])

const userStore = useUserStore()
const router = useRouter()
const { history: readingHistory } = useReadingHistory()
const articles = ref([])
const categories = ref([])
const tags = ref([])
const profile = ref({ nickname: '博主', avatar: '' })
const featured = ref(null)
const total = ref(0)
const page = ref(1)
const size = 10
const activeCategoryId = ref(null)
const loading = ref(false)
const loadingMore = ref(false)
const fileInput = ref(null)

const isAdmin = computed(() => userStore.isLoggedIn())

// Typewriter effect
const phrases = computed(() => {
  const base = [
    '欢迎来到我的博客，分享技术、记录生活。',
    '热爱编程，专注于 Java 与前端开发。',
    '学无止境，每天进步一点点。'
  ]
  if (profile.value.bio) {
    return [profile.value.bio.replace(/\n/g, ' ').substring(0, 80), ...base]
  }
  return base
})
const typewriterIndex = ref(0)
const charIndex = ref(0)
const isDeleting = ref(false)
const displayText = ref('')
let typeTimer = null

const startTypewriter = () => {
  const current = phrases.value[typewriterIndex.value]
  if (isDeleting.value) {
    displayText.value = current.substring(0, charIndex.value - 1)
    charIndex.value--
  } else {
    displayText.value = current.substring(0, charIndex.value + 1)
    charIndex.value++
  }

  let delay = isDeleting.value ? 40 : 80

  if (!isDeleting.value && charIndex.value === current.length) {
    delay = 2000
    isDeleting.value = true
  } else if (isDeleting.value && charIndex.value === 0) {
    isDeleting.value = false
    typewriterIndex.value = (typewriterIndex.value + 1) % phrases.value.length
    delay = 300
  }

  typeTimer = setTimeout(startTypewriter, delay)
}

// Reading time
const readingTime = (content) => {
  if (!content) return '1 分钟'
  const chars = content.replace(/[#*\-\s`>\[\]()!|~]/g, '').length
  const minutes = Math.max(1, Math.round(chars / 400))
  return `${minutes} 分钟`
}

// Avatar upload
const triggerUpload = () => {
  if (!isAdmin.value) return
  fileInput.value?.click()
}

const handleAvatarChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  try {
    const uploadRes = await request.post('/admin/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (uploadRes.data.code === 200) {
      await updateAvatar({ avatar: uploadRes.data.data.url })
      profile.value.avatar = uploadRes.data.data.url
      ElMessage.success('头像已更新')
    }
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}

const fetchArticles = async () => {
  loading.value = true
  try {
    page.value = 1
    const params = { page: 1, size }
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    const res = await getArticles(params)
    articles.value = res.data.records
    total.value = res.data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

const hasMore = computed(() => articles.value.length < total.value)

const loadMore = async () => {
  loadingMore.value = true
  try {
    page.value++
    const params = { page: page.value, size }
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    const res = await getArticles(params)
    articles.value.push(...res.data.records)
  } catch { /* ignore */ }
  finally { loadingMore.value = false }
}

const selectCategory = (id) => {
  activeCategoryId.value = id
  page.value = 1
  fetchArticles()
}

const fetchFeatured = async () => {
  try {
    const res = await getFeaturedArticle()
    if (res.data) featured.value = res.data
  } catch { /* ignore */ }
}

const fetchCategories = async () => {
  const res = await getCategories()
  categories.value = res.data
}

const fetchTags = async () => {
  const res = await getTags()
  tags.value = res.data
}

const fetchProfile = async () => {
  try {
    const res = await getUserProfile()
    if (res.data) {
      profile.value = res.data
      try {
        const apiContacts = JSON.parse(res.data.contacts || '[]')
        if (apiContacts.length) contacts.value = apiContacts
      } catch { /* keep defaults */ }
    }
  } catch { /* ignore */ }
}

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const goDetail = (id) => router.push(`/article/${id}`)
const goSearchByTag = (tagName) => {
  router.push({ path: '/search', query: { keyword: tagName } })
}
onMounted(() => {
  useSEO({ title: '博客', description: 'DKX 的技术博客 — 分享 Java、Vue、Spring Boot 等技术文章' })
  fetchArticles()
  fetchFeatured()
  fetchCategories()
  fetchTags()
  fetchProfile()
  startTypewriter()
})

onUnmounted(() => {
  clearTimeout(typeTimer)
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

/* ===== Hero Banner ===== */
.hero-banner {
  position: relative;
  background: linear-gradient(135deg, var(--accent-bg), var(--bg-tertiary));
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 28px 32px;
  margin-bottom: 20px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s, transform 0.2s;
}
.hero-banner::before {
  content: '';
  position: absolute;
  top: -40px;
  right: -40px;
  width: 120px;
  height: 120px;
  background: var(--accent);
  opacity: 0.08;
  border-radius: 50%;
}
.hero-banner:hover {
  border-color: var(--accent);
  transform: translateY(-2px);
}
.hero-badge {
  display: inline-block;
  background: var(--accent);
  color: #fff;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 20px;
  margin-bottom: 12px;
  letter-spacing: 0.5px;
}
.hero-title {
  font-size: 22px;
  margin-bottom: 8px;
  position: relative;
  z-index: 1;
}
.hero-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  position: relative;
  z-index: 1;
}
.hero-meta {
  font-size: 13px;
  color: var(--text-muted);
  display: flex;
  gap: 6px;
  position: relative;
  z-index: 1;
}

/* ===== Skeleton Loading ===== */
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
.sk-title {
  height: 24px;
  width: 60%;
  margin-bottom: 14px;
}
.sk-meta {
  height: 14px;
  width: 40%;
}
.sk-text {
  height: 14px;
  width: 100%;
}
.sk-text.short {
  width: 70%;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ===== Article Cards ===== */
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
.article-card.is-pinned {
  border-left: 3px solid var(--accent);
}
.article-title {
  font-size: 20px;
  margin: 0;
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

.load-more-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* ===== Sidebar ===== */
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
.category-item .count {
  font-size: 12px;
  color: var(--text-muted);
}

/* ===== Reading History ===== */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.history-item:hover {
  background: var(--bg-tertiary);
}
.history-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-muted);
  flex-shrink: 0;
}
.history-title {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== About Me ===== */
.about-content {
  text-align: center;
}
.about-avatar {
  position: relative;
  display: inline-block;
  margin-bottom: 10px;
  cursor: default;
}
.about-avatar[title]:not([title=""]) {
  cursor: pointer;
}
.avatar-img {
  transition: opacity 0.2s;
}
.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}
.about-avatar:hover .avatar-overlay {
  opacity: 1;
}

.about-name {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 6px;
}

.about-desc {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
  margin-bottom: 14px;
  min-height: 20px;
}
.typewriter {
  color: var(--text-secondary);
}
.cursor {
  color: var(--accent);
  animation: blink 0.8s infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.about-contact {
  display: flex;
  justify-content: center;
  gap: 14px;
}
.contact-icon {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  transition: background 0.2s, border-color 0.2s, color 0.2s;
}
.contact-icon:hover {
  background: var(--accent-bg);
  border-color: var(--accent);
  color: var(--accent);
}

@media (max-width: 768px) {
  .home-layout { flex-direction: column; padding: 16px; }
  .sidebar { min-width: unset; }
  .hero-banner { padding: 20px; }
  .hero-title { font-size: 18px; }
}
</style>
