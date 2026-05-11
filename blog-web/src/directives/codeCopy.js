const COPIED_CLASS = 'copy-btn-copied'

function addLineNumbers(pre, code) {
  if (pre.querySelector('.code-lines')) return
  const text = code?.textContent || pre.textContent || ''
  const lines = text.split('\n')
  const count = lines[lines.length - 1] === '' ? lines.length - 1 : lines.length
  if (count <= 1) return
  const nums = document.createElement('span')
  nums.className = 'code-lines'
  nums.setAttribute('aria-hidden', 'true')
  for (let i = 1; i <= count; i++) {
    nums.appendChild(document.createTextNode(i + '\n'))
  }
  pre.insertBefore(nums, pre.firstChild)
}

function addCopyButtons(el) {
  el.querySelectorAll('pre').forEach((pre) => {
    if (pre.querySelector('.code-copy-btn')) return

    pre.style.position = 'relative'

    const code = pre.querySelector('code')
    if (code) {
      const cls = code.className || ''
      const m = cls.match(/language-(\w+)/)
      if (m && !pre.querySelector('.lang-label')) {
        const label = document.createElement('span')
        label.className = 'lang-label'
        label.textContent = m[1]
        pre.appendChild(label)
      }
    }

    addLineNumbers(pre, code)

    const btn = document.createElement('button')
    btn.className = 'code-copy-btn'
    btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>'
    btn.title = '复制代码'

    btn.addEventListener('click', async () => {
      const codeText = code?.textContent || pre.textContent || ''
      try {
        await navigator.clipboard.writeText(codeText)
        btn.classList.add(COPIED_CLASS)
        btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>'
        setTimeout(() => {
          btn.classList.remove(COPIED_CLASS)
          btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>'
        }, 1500)
      } catch {
        const ta = document.createElement('textarea')
        ta.value = codeText
        ta.style.position = 'fixed'
        ta.style.opacity = '0'
        document.body.appendChild(ta)
        ta.select()
        document.execCommand('copy')
        document.body.removeChild(ta)
        btn.classList.add(COPIED_CLASS)
        setTimeout(() => btn.classList.remove(COPIED_CLASS), 1500)
      }
    })

    pre.appendChild(btn)
  })
}

export default {
  mounted(el) {
    addCopyButtons(el)
    const observer = new MutationObserver(() => addCopyButtons(el))
    observer.observe(el, { childList: true, subtree: true })
    el.__codeCopyObserver = observer
  },
  updated(el) {
    addCopyButtons(el)
  },
  unmounted(el) {
    el.__codeCopyObserver?.disconnect()
  }
}
