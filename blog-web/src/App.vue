<template>
  <template v-if="isWelcome">
    <router-view v-slot="{ Component }">
      <Transition name="page" mode="out-in">
        <component :is="Component" />
      </Transition>
    </router-view>
  </template>

  <template v-else>
    <ReadingProgress />
    <!-- Announcement Bar -->
    <div v-if="announcement" class="announce-bar">
      <span class="announce-text">{{ announcement }}</span>
      <button class="announce-close" @click="dismissAnnounce" title="关闭">&times;</button>
    </div>
    <div class="app-wrapper">
      <nav class="nav-bar">
        <div class="nav-inner">
          <router-link to="/" class="nav-brand">DKX's Blog</router-link>

          <div class="nav-center">
            <router-link to="/blog">博客</router-link>
            <router-link to="/archive">归档</router-link>
            <router-link to="/tags">标签</router-link>
            <router-link to="/friends">友链</router-link>
            <router-link to="/projects">项目</router-link>
            <router-link to="/resume">关于</router-link>
          </div>

          <div class="nav-right nav-desktop">
            <div class="nav-search">
              <el-input
                v-model="searchWord"
                size="small"
                placeholder="搜索..."
                @keyup.enter="goSearch"
                clearable
              >
                <template #prefix>
                  <el-icon :size="14"><Search /></el-icon>
                </template>
              </el-input>
            </div>
            <router-link v-if="!isLogged" to="/login" class="nav-link-text">登录</router-link>
            <router-link v-else to="/admin" class="nav-link-text">管理</router-link>
            <ThemeToggle />
          </div>

          <!-- Hamburger -->
          <button class="hamburger" :class="{ open: menuOpen }" @click="menuOpen = !menuOpen" aria-label="菜单">
            <span></span><span></span><span></span>
          </button>
        </div>
      </nav>

      <!-- Mobile overlay -->
      <Transition name="menu-slide">
        <div v-if="menuOpen" class="mobile-overlay" @click.self="menuOpen = false">
          <div class="mobile-menu">
            <div class="mobile-search">
              <el-input
                v-model="searchWord"
                size="small"
                placeholder="搜索..."
                @keyup.enter="goSearch"
                clearable
              >
                <template #prefix>
                  <el-icon :size="14"><Search /></el-icon>
                </template>
              </el-input>
            </div>
            <router-link to="/blog" @click="menuOpen = false">博客</router-link>
            <router-link to="/archive" @click="menuOpen = false">归档</router-link>
            <router-link to="/tags" @click="menuOpen = false">标签</router-link>
            <router-link to="/friends" @click="menuOpen = false">友链</router-link>
            <router-link to="/projects" @click="menuOpen = false">项目</router-link>
            <router-link to="/resume" @click="menuOpen = false">关于</router-link>
            <div class="mobile-divider"></div>
            <router-link v-if="!isLogged" to="/login" @click="menuOpen = false">登录</router-link>
            <router-link v-else to="/admin" @click="menuOpen = false">管理</router-link>
            <div class="mobile-theme">
              <span>主题</span>
              <ThemeToggle />
            </div>
          </div>
        </div>
      </Transition>

      <main class="main-content">
        <router-view v-slot="{ Component }">
          <Transition name="page" mode="out-in">
            <component :is="Component" />
          </Transition>
        </router-view>
      </main>
      <BackToTop />
      <footer class="site-footer">
        <p>DKX's Blog &copy; {{ year }} — 记录技术，分享生活</p>
      </footer>
    </div>
  </template>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getUserProfile } from '@/api/comment'
import ReadingProgress from '@/components/ReadingProgress.vue'
import BackToTop from '@/components/BackToTop.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isLogged = computed(() => userStore.isLoggedIn())
const isWelcome = computed(() => route.path === '/')
const year = new Date().getFullYear()
const searchWord = ref('')
const menuOpen = ref(false)
const announcement = ref('')

const dismissAnnounce = () => {
  announcement.value = ''
  localStorage.setItem('announce-dismissed', '1')
}

const goSearch = () => {
  const kw = searchWord.value.trim()
  if (kw) {
    menuOpen.value = false
    router.push({ path: '/search', query: { keyword: kw } })
  }
}

onMounted(async () => {
  if (localStorage.getItem('announce-dismissed') === '1') return
  try {
    const res = await getUserProfile()
    if (res.data?.announcement) {
      announcement.value = res.data.announcement
    }
  } catch { /* ignore */ }
})

</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Announcement */
.announce-bar {
  background: linear-gradient(135deg, var(--accent-bg), rgba(251,191,36,0.15));
  border-bottom: 1px solid var(--accent);
  color: var(--text-primary);
  font-size: 14px;
  text-align: center;
  padding: 10px 48px;
  position: relative;
  z-index: 101;
}
.announce-text {
  color: var(--accent);
}
.announce-close {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: var(--text-muted);
  padding: 0;
  line-height: 1;
  transition: color 0.2s;
}
.announce-close:hover { color: var(--accent); }

.nav-bar {
  background: var(--glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-brand {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
  flex-shrink: 0;
}
.nav-brand:hover { text-decoration: none; }

.nav-center {
  display: flex;
  align-items: center;
  gap: 20px;
}
.nav-center a {
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
}
.nav-center a:hover,
.nav-center a.router-link-active {
  color: var(--accent);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-left: auto;
}

.nav-search {
  width: 180px;
}

.nav-link-text {
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
  flex-shrink: 0;
}
.nav-link-text:hover { color: var(--accent); }

/* Hamburger */
.hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: 36px;
  height: 36px;
  padding: 8px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  flex-shrink: 0;
  transition: border-color 0.2s;
}
.hamburger:hover { border-color: var(--accent); }
.hamburger span {
  display: block;
  height: 2px;
  background: var(--text-primary);
  border-radius: 1px;
  transition: transform 0.3s, opacity 0.2s;
}
.hamburger.open span:nth-child(1) {
  transform: translateY(7px) rotate(45deg);
}
.hamburger.open span:nth-child(2) {
  opacity: 0;
}
.hamburger.open span:nth-child(3) {
  transform: translateY(-7px) rotate(-45deg);
}

/* Mobile overlay */
.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
  backdrop-filter: blur(4px);
}
.mobile-menu {
  position: absolute;
  top: 0;
  right: 0;
  width: 260px;
  height: 100%;
  background: var(--bg-secondary);
  border-left: 1px solid var(--border);
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}
.mobile-menu a {
  display: block;
  padding: 12px 16px;
  font-size: 15px;
  color: var(--text-primary);
  text-decoration: none;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
}
.mobile-menu a:hover,
.mobile-menu a.router-link-active {
  background: var(--accent-bg);
  color: var(--accent);
}
.mobile-search {
  margin-bottom: 12px;
}
.mobile-divider {
  height: 1px;
  background: var(--border);
  margin: 8px 0;
}
.mobile-theme {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  font-size: 15px;
  color: var(--text-secondary);
}

/* Menu transition */
.menu-slide-enter-active,
.menu-slide-leave-active {
  transition: opacity 0.25s ease;
}
.menu-slide-enter-active .mobile-menu,
.menu-slide-leave-active .mobile-menu {
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.menu-slide-enter-from,
.menu-slide-leave-to {
  opacity: 0;
}
.menu-slide-enter-from .mobile-menu,
.menu-slide-leave-to .mobile-menu {
  transform: translateX(100%);
}

.main-content {
  flex: 1;
}

.site-footer {
  text-align: center;
  padding: 24px;
  color: var(--text-muted);
  font-size: 13px;
  border-top: 1px solid var(--border);
}
.site-footer p { margin: 0; }

@media (max-width: 768px) {
  .nav-center,
  .nav-desktop {
    display: none;
  }
  .hamburger { display: flex; }
}
</style>

<style>
.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
