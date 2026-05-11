<template>
  <div class="subscribe-box">
    <h4 class="subscribe-title">邮件订阅</h4>
    <p class="subscribe-desc">新文章发布时邮件通知你</p>
    <div class="subscribe-form">
      <el-input
        v-model="email"
        size="small"
        placeholder="输入邮箱地址"
        clearable
        @keyup.enter="handleSubscribe"
      />
      <el-button size="small" type="primary" :loading="loading" @click="handleSubscribe">
        订阅
      </el-button>
    </div>
    <p v-if="msg" class="subscribe-msg" :class="{ error: isError }">{{ msg }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { subscribe } from '@/api/subscribe'
import { ElMessage } from 'element-plus'

const email = ref('')
const loading = ref(false)
const msg = ref('')
const isError = ref(false)

const handleSubscribe = async () => {
  const val = email.value.trim()
  if (!val) {
    ElMessage.warning('请输入邮箱地址')
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }
  loading.value = true
  msg.value = ''
  try {
    await subscribe(val)
    msg.value = '订阅成功！请查收确认邮件。'
    isError.value = false
    email.value = ''
  } catch (e) {
    msg.value = e?.response?.data?.message || '订阅失败'
    isError.value = true
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.subscribe-box {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 16px;
  margin-top: 14px;
}
.subscribe-title {
  font-size: 14px;
  color: var(--text-primary);
  margin: 0 0 4px;
}
.subscribe-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0 0 10px;
}
.subscribe-form {
  display: flex;
  gap: 6px;
}
.subscribe-msg {
  font-size: 12px;
  margin: 8px 0 0;
  color: var(--success);
}
.subscribe-msg.error {
  color: var(--danger);
}
</style>
