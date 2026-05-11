import { describe, it, expect, beforeEach, vi } from 'vitest'

describe('useReadingHistory', () => {
  beforeEach(() => {
    vi.resetModules()
    localStorage.clear()
  })

  it('should add article to history', async () => {
    const { useReadingHistory } = await import('@/composables/useReadingHistory.js')
    const { addArticle, getHistory } = useReadingHistory()

    addArticle({ id: 1, title: 'Article 1' })
    expect(getHistory()).toHaveLength(1)
    expect(getHistory()[0].title).toBe('Article 1')
  })

  it('should not duplicate articles', async () => {
    const { useReadingHistory } = await import('@/composables/useReadingHistory.js')
    const { addArticle, getHistory } = useReadingHistory()

    addArticle({ id: 1, title: 'First' })
    addArticle({ id: 1, title: 'First Again' })
    expect(getHistory()).toHaveLength(1)
  })

  it('should limit to 6 and keep newest first', async () => {
    const { useReadingHistory } = await import('@/composables/useReadingHistory.js')
    const { addArticle, getHistory } = useReadingHistory()

    for (let i = 1; i <= 8; i++) {
      addArticle({ id: i, title: `Article ${i}` })
    }
    const h = getHistory()
    expect(h).toHaveLength(6)
    expect(h[0].id).toBe(8)
    expect(h[5].id).toBe(3)
  })

  it('should persist to localStorage', async () => {
    const { useReadingHistory } = await import('@/composables/useReadingHistory.js')
    const { addArticle } = useReadingHistory()

    addArticle({ id: 1, title: 'Test' })
    addArticle({ id: 2, title: 'Test 2' })

    const raw = JSON.parse(localStorage.getItem('blog-reading-history'))
    expect(raw).toHaveLength(2)
  })
})
