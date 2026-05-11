const observerMap = new WeakMap()

function getObserver() {
  if (!getObserver.instance) {
    getObserver.instance = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const el = entry.target
          const delay = parseInt(el.dataset.fadeDelay) || 0
          setTimeout(() => {
            el.classList.add('fade-visible')
          }, delay)
          getObserver.instance.unobserve(el)
        }
      })
    }, { threshold: 0.1 })
  }
  return getObserver.instance
}

export default {
  mounted(el, binding) {
    const delay = binding.value ?? binding.arg ?? 0
    el.dataset.fadeDelay = delay
    el.classList.add('fade-enter')
    getObserver().observe(el)
  },
  unmounted(el) {
    getObserver().unobserve(el)
  }
}
