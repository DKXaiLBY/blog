<template>
  <div class="sidebar-card about-card">
    <h3>关于我</h3>
    <div class="about-content">
      <div class="about-avatar" @click="$emit('upload-avatar')" :title="isAdmin ? '点击更换头像' : ''">
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
</template>

<script setup>
import { Camera, Message } from '@element-plus/icons-vue'

defineProps({
  profile: { type: Object, required: true },
  displayText: { type: String, default: '' },
  contacts: { type: Array, default: () => [] },
  isAdmin: { type: Boolean, default: false }
})

defineEmits(['upload-avatar'])
</script>
