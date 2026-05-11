import { ref, onBeforeUnmount } from 'vue'

export function useTypewriter(phrases) {
  const typewriterIndex = ref(0)
  const charIndex = ref(0)
  const isDeleting = ref(false)
  const displayText = ref('')
  let timer = null

  const tick = () => {
    const current = phrases.value[typewriterIndex.value]
    if (!current) return

    if (isDeleting.value) {
      displayText.value = current.substring(0, charIndex.value - 1)
      charIndex.value--
    } else {
      displayText.value = current.substring(0, charIndex.value + 1)
      charIndex.value++
    }

    let delay = isDeleting.value ? 40 : 80

    if (!isDeleting.value && charIndex.value === current.length) {
      delay = 2000
      isDeleting.value = true
    } else if (isDeleting.value && charIndex.value === 0) {
      isDeleting.value = false
      typewriterIndex.value = (typewriterIndex.value + 1) % phrases.value.length
      delay = 300
    }

    timer = setTimeout(tick, delay)
  }

  onBeforeUnmount(() => { if (timer) clearTimeout(timer) })

  return { displayText, start: tick }
}
