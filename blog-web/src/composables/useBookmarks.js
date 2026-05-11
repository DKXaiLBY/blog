// Article bookmark composable using localStorage
import { ref } from 'vue'

const STORAGE_KEY = 'blog_bookmarks'
const bookmarks = ref(loadBookmarks())

function loadBookmarks() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch { return [] }
}

function saveBookmarks() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(bookmarks.value))
}

export function useBookmarks() {
  const isBookmarked = (articleId) => bookmarks.value.some(b => b.id === articleId)

  const toggleBookmark = (article) => {
    const idx = bookmarks.value.findIndex(b => b.id === article.id)
    if (idx >= 0) {
      bookmarks.value.splice(idx, 1)
    } else {
      bookmarks.value.unshift({
        id: article.id,
        title: article.title,
        createdAt: new Date().toISOString()
      })
    }
    saveBookmarks()
    return !(idx >= 0) // true = just bookmarked, false = removed
  }

  const getBookmarks = () => bookmarks.value

  return { bookmarks, isBookmarked, toggleBookmark, getBookmarks }
}
