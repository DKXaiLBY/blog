let observer = null

function getObserver() {
  if (!observer) {
    observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const img = entry.target
          const src = img.dataset.src
          if (src) {
            img.src = src
            img.removeAttribute('data-src')
          }
          observer.unobserve(img)
        }
      })
    }, { rootMargin: '100px' })
  }
  return observer
}

function processImages(el) {
  el.querySelectorAll('img:not([data-lazy-processed])').forEach((img) => {
    const src = img.src || img.getAttribute('src')
    if (src && !img.dataset.src && !img.src.startsWith('data:')) {
      img.dataset.src = src
      img.src = ''
      img.setAttribute('data-lazy-processed', '1')
    }
    getObserver().observe(img)
  })
}

export default {
  mounted(el) {
    processImages(el)
    const mo = new MutationObserver(() => processImages(el))
    mo.observe(el, { childList: true, subtree: true })
    el.__lazyImgObserver = mo
  },
  unmounted(el) {
    el.__lazyImgObserver?.disconnect()
  }
}
