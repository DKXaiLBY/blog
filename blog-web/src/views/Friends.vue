<template>
  <div class="friends-layout">
    <div class="friends-header" v-fade-in>
      <h1>友情链接</h1>
      <p>欢迎交换友链，在下方留言或通过 GitHub 联系我</p>
    </div>

    <div v-if="loading" class="friends-grid">
      <div v-for="i in 4" :key="'s'+i" class="skeleton-card">
        <div class="sk-line sk-name"></div>
        <div class="sk-line sk-desc"></div>
      </div>
    </div>

    <template v-else-if="friends.length">
      <div class="friends-grid">
        <a
          v-for="(link, idx) in friends"
          :key="link.id"
          :href="link.url"
          target="_blank"
          rel="noopener"
          class="friend-card"
          v-fade-in="idx * 60"
        >
          <div class="friend-name">{{ link.name }}</div>
          <div v-if="link.description" class="friend-desc">{{ link.description }}</div>
        </a>
      </div>
    </template>
    <el-empty v-else description="暂无友链" />

    <div class="apply-section">
      <h3>申请友链</h3>
      <p class="apply-hint">填写以下信息提交友链申请，审核通过后将在友链列表展示</p>
      <el-form :model="applyForm" label-width="80px" class="apply-form">
        <el-form-item label="网站名称">
          <el-input v-model="applyForm.name" placeholder="你的网站名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="网站链接">
          <el-input v-model="applyForm.url" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="网站描述">
          <el-input v-model="applyForm.description" placeholder="简短描述（可选）" maxlength="50" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleApply" :loading="applying">提交申请</el-button>
          <el-button @click="resetApplyForm">重置</el-button>
        </el-form-item>
      </el-form>
      <el-alert
        v-if="applyMsg"
        :title="applyMsg"
        :type="applyOk ? 'success' : 'error'"
        closable
        @close="applyMsg = ''"
        style="margin-top:12px"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFriends, applyFriend } from '@/api/comment'
import { useSEO } from '@/composables/useSEO'
import { ElMessage } from 'element-plus'

const friends = ref([])
const loading = ref(false)
const applying = ref(false)
const applyMsg = ref('')
const applyOk = ref(false)

const applyForm = reactive({ name: '', url: '', description: '' })

const resetApplyForm = () => {
  applyForm.name = ''
  applyForm.url = ''
  applyForm.description = ''
}

const handleApply = async () => {
  if (!applyForm.name.trim() || !applyForm.url.trim()) {
    applyMsg.value = '请填写网站名称和链接'
    applyOk.value = false
    return
  }
  applying.value = true
  applyMsg.value = ''
  try {
    await applyFriend({
      name: applyForm.name.trim(),
      url: applyForm.url.trim(),
      description: applyForm.description.trim()
    })
    applyOk.value = true
    applyMsg.value = '申请已提交，审核通过后将在友链列表展示'
    applyForm.name = ''
    applyForm.url = ''
    applyForm.description = ''
  } catch {
    applyOk.value = false
    applyMsg.value = '提交失败，请稍后重试'
  } finally {
    applying.value = false
  }
}

onMounted(async () => {
  useSEO({ title: '友情链接', description: '交换友链，互相学习' })
  loading.value = true
  try {
    const res = await getFriends()
    friends.value = res.data || []
  } catch { /* ignore */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.friends-layout {
  max-width: 860px;
  margin: 0 auto;
  padding: 48px 24px;
}

.friends-header {
  text-align: center;
  margin-bottom: 40px;
}
.friends-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
}
.friends-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.friends-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.friend-card {
  display: block;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px;
  text-decoration: none;
  transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
}
.friend-card:hover {
  border-color: var(--accent);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
  text-decoration: none;
}
.friend-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.friend-desc {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
}

/* Skeleton */
.skeleton-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px;
}
.sk-line {
  background: linear-gradient(90deg, var(--border) 25%, transparent 50%, var(--border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 10px;
}
.sk-name { height: 20px; width: 50%; }
.sk-desc { height: 14px; width: 80%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Apply Section */
.apply-section {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid var(--border);
}
.apply-section h3 {
  font-size: 18px;
  margin-bottom: 6px;
}
.apply-hint {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 20px;
}
.apply-form {
  max-width: 500px;
}

@media (max-width: 768px) {
  .friends-grid { grid-template-columns: 1fr; }
  .friends-layout { padding: 32px 16px; }
}
</style>
