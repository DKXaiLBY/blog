import { ref } from 'vue'

const STORAGE_KEY = 'blog-reading-history'
const MAX_ITEMS = 6

const history = ref(loadHistory())

function loadHistory() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch { return [] }
}

function saveHistory(items) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
}

export function useReadingHistory() {
  function addArticle(article) {
    if (!article?.id) return
    const items = loadHistory()
    const idx = items.findIndex((h) => h.id === article.id)
    if (idx !== -1) {
      items.splice(idx, 1)
    }
    items.unshift({
      id: article.id,
      title: article.title,
      coverImage: article.coverImage || '',
      readAt: new Date().toISOString()
    })
    if (items.length > MAX_ITEMS) {
      items.length = MAX_ITEMS
    }
    history.value = items
    saveHistory(items)
  }

  function getHistory() {
    return history.value
  }

  function clearHistory() {
    history.value = []
    localStorage.removeItem(STORAGE_KEY)
  }

  return { addArticle, getHistory, clearHistory, history }
}
