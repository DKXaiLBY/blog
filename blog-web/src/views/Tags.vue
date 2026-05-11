<template>
  <div class="tags-layout">
    <div class="tags-header" v-fade-in>
      <h1>标签云</h1>
      <p>共 {{ tags.length }} 个标签，点击查看相关文章</p>
    </div>

    <div v-if="tags.length" class="tags-cloud">
      <span
        v-for="(tag, idx) in tags"
        :key="tag.id"
        class="tag-item"
        v-fade-in="idx * 40"
        :style="tagStyle(tag)"
        @click="goTag(tag.name)"
      >{{ tag.name }}</span>
    </div>
    <el-empty v-else description="暂无标签" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTags } from '@/api/tag'
import { useSEO } from '@/composables/useSEO'

const router = useRouter()
const tags = ref([])

const fetchTags = async () => {
  const res = await getTags()
  tags.value = res.data || []
}

const counts = () => tags.value.map((t) => t.articleCount)
const maxCount = () => Math.max(...counts(), 1)
const minCount = () => Math.min(...counts(), 1)

const hues = ['#c084fc', '#a78bfa', '#818cf8', '#60a5fa', '#67e8f9', '#f472b6', '#fbbf24', '#34d399']

const tagStyle = (tag) => {
  const range = maxCount() - minCount() || 1
  const ratio = (tag.articleCount - minCount()) / range
  const size = 14 + ratio * 16
  const hue = hues[tag.id % hues.length]
  return {
    fontSize: `${size}px`,
    color: hue,
    borderColor: hue + '44',
    background: hue + '11'
  }
}

const goTag = (name) => {
  router.push({ path: '/search', query: { keyword: name } })
}

onMounted(() => {
  useSEO({ title: '标签云', description: '按标签浏览文章' })
  fetchTags()
})
</script>

<style scoped>
.tags-layout {
  max-width: 860px;
  margin: 0 auto;
  padding: 48px 24px;
}

.tags-header {
  text-align: center;
  margin-bottom: 40px;
}
.tags-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
}
.tags-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px 16px;
  align-items: center;
}

.tag-item {
  display: inline-block;
  padding: 4px 16px;
  border-radius: 20px;
  border: 1px solid;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  font-weight: 500;
}
.tag-item:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

@media (max-width: 768px) {
  .tags-layout { padding: 32px 16px; }
  .tags-header h1 { font-size: 22px; }
}
</style>
