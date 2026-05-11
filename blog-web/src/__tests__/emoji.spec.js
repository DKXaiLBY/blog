import { describe, it, expect } from 'vitest'

// Mirror of the EMOJI map + renderEmoji from ArticleDetail.vue
const EMOJI = {
  ':smile:': '😊', ':laughing:': '😆', ':joy:': '😂', ':rofl:': '🤣',
  ':heart:': '❤️', ':heart_eyes:': '😍', ':kiss:': '😘',
  ':thumbsup:': '👍', ':thumbsdown:': '👎', ':clap:': '👏',
  ':+1:': '👍', ':-1:': '👎',
  ':cry:': '😢', ':sob:': '😭', ':angry:': '😠', ':rage:': '😡',
  ':ok_hand:': '👌', ':pray:': '🙏', ':muscle:': '💪',
  ':fire:': '🔥', ':star:': '⭐', ':rocket:': '🚀',
  ':tada:': '🎉', ':sparkles:': '✨', ':100:': '💯',
  ':thinking:': '🤔', ':eyes:': '👀', ':wink:': '😉',
  ':sunglasses:': '😎', ':sweat_smile:': '😅',
  ':coffee:': '☕', ':beer:': '🍺', ':pizza:': '🍕',
  ':check:': '✅', ':x:': '❌', ':warning:': '⚠️',
  ':question:': '❓', ':bulb:': '💡', ':book:': '📖'
}

function renderEmoji(text) {
  if (!text) return ''
  return text.replace(/:[\w+-]+:/g, (match) => EMOJI[match] || match)
}

describe('renderEmoji', () => {
  it('should replace known emoji codes', () => {
    expect(renderEmoji(':smile:')).toBe('😊')
    expect(renderEmoji(':heart:')).toBe('❤️')
    expect(renderEmoji(':fire:')).toBe('🔥')
  })

  it('should replace multiple emojis in text', () => {
    expect(renderEmoji(':fire: nice :100:')).toBe('🔥 nice 💯')
  })

  it('should leave unknown codes unchanged', () => {
    expect(renderEmoji(':unknown:')).toBe(':unknown:')
  })

  it('should handle empty/null input', () => {
    expect(renderEmoji('')).toBe('')
    expect(renderEmoji(null)).toBe('')
  })

  it('should handle text with no emojis', () => {
    expect(renderEmoji('Hello World')).toBe('Hello World')
  })
})
