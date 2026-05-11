<template>
  <div v-if="article" class="hero-banner" @click="$router.push(`/article/${article.id}`)">
    <div class="hero-badge">精选推荐</div>
    <h2 class="hero-title">{{ article.title }}</h2>
    <p class="hero-summary">{{ article.summary }}</p>
    <div class="hero-meta">
      <span>{{ fmtDate(article.createdAt) }}</span>
      <span>·</span>
      <span>{{ article.categoryName || '未分类' }}</span>
      <span>·</span>
      <span>{{ readingTime(article.content) }} 阅读</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  article: { type: Object, default: null }
})

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 10)
}

const readingTime = (content) => {
  if (!content) return '1 分钟'
  const words = content.replace(/[#*\-\s`>\[\]()!|~\\n]/g, '').length
  return Math.max(1, Math.ceil(words / 300)) + ' 分钟'
}
</script>
