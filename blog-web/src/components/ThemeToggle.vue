<template>
  <button ref="btnRef" class="theme-toggle" :title="isDark ? '切换浅色模式' : '切换深色模式'" @click="onToggle">
    <Transition name="icon-swap" mode="out-in">
      <el-icon v-if="isDark" :size="18" key="moon"><Moon /></el-icon>
      <el-icon v-else :size="18" key="sun"><Sunny /></el-icon>
    </Transition>
  </button>
</template>

<script setup>
import { ref } from 'vue'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'

const { isDark, toggle } = useTheme()
const btnRef = ref(null)

const onToggle = () => {
  toggle(btnRef.value)
}
</script>

<style scoped>
.theme-toggle {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, border-color 0.2s, color 0.2s;
  flex-shrink: 0;
}
.theme-toggle:hover {
  background: var(--accent-bg);
  border-color: var(--accent);
  color: var(--accent);
}

.icon-swap-enter-active,
.icon-swap-leave-active {
  transition: opacity 0.15s ease, transform 0.2s ease;
}
.icon-swap-enter-from,
.icon-swap-leave-to {
  opacity: 0;
  transform: scale(0.6) rotate(-90deg);
}
</style>
