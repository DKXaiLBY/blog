<template>
  <Teleport to="body">
    <Transition name="lb-fade">
      <div v-if="visible" class="lightbox-overlay" @click.self="close">
        <button class="lightbox-close" @click="close">&times;</button>
        <img :src="src" :alt="alt" class="lightbox-img" />
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'

const visible = ref(false)
const src = ref('')
const alt = ref('')

function open(imgSrc, imgAlt) {
  src.value = imgSrc
  alt.value = imgAlt || ''
  visible.value = true
  document.body.style.overflow = 'hidden'
}

function close() {
  visible.value = false
  document.body.style.overflow = ''
}

defineExpose({ open, close })
</script>

<style scoped>
.lightbox-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: zoom-out;
}
.lightbox-close {
  position: absolute;
  top: 20px;
  right: 28px;
  font-size: 36px;
  color: #fff;
  background: none;
  border: none;
  cursor: pointer;
  line-height: 1;
  z-index: 1;
}
.lightbox-img {
  max-width: 90vw;
  max-height: 90vh;
  border-radius: 6px;
  cursor: default;
}

.lb-fade-enter-active,
.lb-fade-leave-active {
  transition: opacity 0.25s ease;
}
.lb-fade-enter-from,
.lb-fade-leave-to {
  opacity: 0;
}
</style>
