<template>
  <nav v-if="headings.length" class="toc-aside" ref="tocNav">
    <h4>目录</h4>
    <ul>
      <li
        v-for="h in headings"
        :key="h.id"
        :class="['toc-item', `toc-level-${h.level}`, { active: activeId === h.id }]"
        :ref="el => setTocRef(h.id, el)"
        @click="scrollTo(h.id)"
      >{{ h.text }}</li>
    </ul>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'

const headings = ref([])
const activeId = ref('')
const tocNav = ref(null)
let observer = null
const tocRefs = {}
const activeHeadings = new Set()

const setTocRef = (id, el) => {
  if (el) tocRefs[id] = el
}

const extractHeadings = () => {
  const detailContent = document.querySelector('.detail-content')
  if (!detailContent) return
  const hs = detailContent.querySelectorAll('h1, h2, h3')
  headings.value = Array.from(hs).map((h, i) => {
    const id = `heading-${i}`
    h.id = id
    h.style.scrollMarginTop = '70px'
    return { id, text: h.textContent, level: parseInt(h.tagName[1]) }
  })
}

const scrollTo = (id) => {
  const el = document.getElementById(id)
  if (el) {
    activeId.value = id
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

const updateActive = () => {
  if (activeHeadings.size === 0) return
  const visible = headings.value
    .map(h => document.getElementById(h.id))
    .filter(el => el && activeHeadings.has(el.id))
  if (visible.length === 0) return
  const topmost = visible.reduce((a, b) =>
    a.getBoundingClientRect().top < b.getBoundingClientRect().top ? a : b
  )
  activeId.value = topmost.id
  // Auto-scroll the TOC to keep active item in view
  nextTick(() => {
    const tocItem = tocRefs[topmost.id]
    if (tocItem) {
      tocItem.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
    }
  })
}

const setupObserver = () => {
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        activeHeadings.add(entry.target.id)
      } else {
        activeHeadings.delete(entry.target.id)
      }
    })
    updateActive()
  }, { rootMargin: '-70px 0px -50% 0px' })

  setTimeout(() => {
    headings.value.forEach((h) => {
      const el = document.getElementById(h.id)
      if (el) observer.observe(el)
    })
  }, 100)
}

onMounted(() => {
  extractHeadings()
  setupObserver()
})

onUnmounted(() => {
  observer?.disconnect()
  activeHeadings.clear()
})
</script>

<style scoped>
.toc-aside {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 18px;
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}
.toc-aside h4 {
  font-size: 14px;
  margin-bottom: 10px;
  color: var(--text-primary);
}

.toc-item {
  list-style: none;
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 5px 0;
  transition: color 0.15s, border-color 0.15s;
  border-left: 2px solid transparent;
  padding-left: 10px;
}
.toc-item:hover {
  color: var(--accent);
}
.toc-item.active {
  color: var(--accent);
  border-left-color: var(--accent);
  font-weight: 500;
}

.toc-level-2 { padding-left: 20px; }
.toc-level-3 { padding-left: 30px; }
</style>
