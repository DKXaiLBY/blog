import { ref, computed, watchEffect } from 'vue'

const KEY = 'blog-theme'
const stored = localStorage.getItem(KEY)
const theme = ref(stored === 'light' || stored === 'dark' ? stored : 'auto')

const systemDark = window.matchMedia('(prefers-color-scheme: dark)')

export function useTheme() {
  const effective = computed(() => {
    if (theme.value === 'auto') {
      return systemDark.matches ? 'dark' : 'light'
    }
    return theme.value
  })

  const isDark = computed(() => effective.value === 'dark')

  function apply(t) {
    document.documentElement.setAttribute('data-theme', t)
    // Switch code highlight theme
    const codeLink = document.getElementById('code-theme-link')
    if (codeLink) {
      codeLink.href = t === 'dark'
        ? '/hljs/a11y-dark.css'
        : '/hljs/github.css'
    }
  }

  watchEffect(() => apply(effective.value))

  systemDark.addEventListener('change', () => {
    if (theme.value === 'auto') apply(effective.value)
  })

  function setTheme(t) {
    theme.value = t
    localStorage.setItem(KEY, t)
  }

  function toggle(originEl) {
    const target = effective.value === 'dark' ? 'light' : 'dark'

    if (!document.startViewTransition || !originEl) {
      setTheme(target)
      return
    }

    const rect = originEl.getBoundingClientRect()
    const cx = rect.left + rect.width / 2
    const cy = rect.top + rect.height / 2
    const radius = Math.hypot(
      Math.max(cx, window.innerWidth - cx),
      Math.max(cy, window.innerHeight - cy)
    )

    const style = document.createElement('style')
    style.textContent = [
      '::view-transition-old(root) { animation: none; }',
      `::view-transition-new(root) {`,
      `  clip-path: circle(0 at ${cx}px ${cy}px);`,
      `  animation: theme-reveal 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;`,
      '}',
      '@keyframes theme-reveal {',
      `  to { clip-path: circle(${radius}px at ${cx}px ${cy}px); }`,
      '}'
    ].join('\n')
    document.head.appendChild(style)

    document.startViewTransition(() => {
      setTheme(target)
    })

    setTimeout(() => style.remove(), 600)
  }

  return { theme, effective, isDark, setTheme, toggle }
}
