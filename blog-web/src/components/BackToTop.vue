<template>
  <Transition name="btt-fade">
    <button v-show="visible" class="back-to-top" @click="scrollTop" :title="`滚动进度 ${percent}%`">
      <svg class="progress-ring" width="44" height="44" viewBox="0 0 44 44">
        <circle
          class="ring-bg"
          cx="22" cy="22" r="18"
          fill="none"
          stroke="var(--border)"
          stroke-width="3"
        />
        <circle
          class="ring-fill"
          cx="22" cy="22" r="18"
          fill="none"
          stroke="var(--accent)"
          stroke-width="3"
          stroke-linecap="round"
          :stroke-dasharray="circumference"
          :stroke-dashoffset="dashOffset"
          transform="rotate(-90 22 22)"
        />
      </svg>
      <el-icon :size="14" class="arrow-icon"><ArrowUp /></el-icon>
    </button>
  </Transition>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ArrowUp } from '@element-plus/icons-vue'

const visible = ref(false)
const percent = ref(0)
const circumference = 2 * Math.PI * 18

const dashOffset = computed(() => {
  return circumference - (percent.value / 100) * circumference
})

const onScroll = () => {
  const h = document.documentElement.scrollHeight - window.innerHeight
  percent.value = h > 0 ? Math.round((window.scrollY / h) * 100) : 0
  visible.value = window.scrollY > 200
}

const scrollTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.back-to-top {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 999;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}
.back-to-top:hover {
  transform: translateY(-2px);
}

.progress-ring {
  position: absolute;
  inset: 0;
  transform: rotate(0deg);
}
.ring-bg {
  opacity: 0.3;
}
.ring-fill {
  transition: stroke-dashoffset 0.15s linear;
}

.arrow-icon {
  position: relative;
  z-index: 1;
  color: var(--accent);
  transition: transform 0.2s;
}
.back-to-top:hover .arrow-icon {
  transform: translateY(-2px);
}

.btt-fade-enter-active,
.btt-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.btt-fade-enter-from,
.btt-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
