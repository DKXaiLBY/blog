import { describe, it, expect, beforeEach, vi } from 'vitest'

describe('useBookmarks', () => {
  beforeEach(() => {
    vi.resetModules()
    localStorage.clear()
  })

  it('should toggle bookmark on and off', async () => {
    const { useBookmarks } = await import('@/composables/useBookmarks.js')
    const { isBookmarked, toggleBookmark, getBookmarks } = useBookmarks()

    expect(isBookmarked(1)).toBe(false)
    expect(getBookmarks()).toHaveLength(0)

    toggleBookmark({ id: 1, title: 'Test' })
    expect(isBookmarked(1)).toBe(true)
    expect(getBookmarks()).toHaveLength(1)

    toggleBookmark({ id: 1, title: 'Test' })
    expect(isBookmarked(1)).toBe(false)
    expect(getBookmarks()).toHaveLength(0)
  })

  it('should persist to localStorage', async () => {
    const { useBookmarks } = await import('@/composables/useBookmarks.js')
    const { toggleBookmark } = useBookmarks()

    toggleBookmark({ id: 1, title: 'A' })
    toggleBookmark({ id: 2, title: 'B' })

    const raw = JSON.parse(localStorage.getItem('blog_bookmarks'))
    expect(raw).toHaveLength(2)
    expect(raw[0].id).toBe(2)
  })

  it('should load existing bookmarks from storage', async () => {
    localStorage.setItem('blog_bookmarks', JSON.stringify([
      { id: 5, title: 'Stored', createdAt: new Date().toISOString() }
    ]))

    const { useBookmarks } = await import('@/composables/useBookmarks.js')
    const { isBookmarked, getBookmarks } = useBookmarks()

    expect(isBookmarked(5)).toBe(true)
    expect(getBookmarks()).toHaveLength(1)
  })
})
