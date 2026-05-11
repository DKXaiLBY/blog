import { watch, onBeforeUnmount } from 'vue'

export function useAutoSave(form, key, interval = 30000) {
  let timer = null

  const save = () => {
    if (!form.value) return
    try {
      localStorage.setItem(key, JSON.stringify({
        data: { ...form.value },
        savedAt: new Date().toISOString()
      }))
    } catch { /* ignore */ }
  }

  const restore = () => {
    try {
      const raw = localStorage.getItem(key)
      if (!raw) return false
      const parsed = JSON.parse(raw)
      if (!parsed.data) return false
      Object.assign(form.value, parsed.data)
      return true
    } catch { return false }
  }

  const clear = () => {
    localStorage.removeItem(key)
  }

  const start = () => {
    if (timer) return
    timer = setInterval(save, interval)
  }

  const stop = () => {
    if (timer) { clearInterval(timer); timer = null }
  }

  onBeforeUnmount(stop)

  return { save, restore, clear, start, stop }
}
