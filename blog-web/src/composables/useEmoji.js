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

export function useEmoji() {
  const render = (text) => {
    if (!text) return ''
    return text.replace(/:[\w+-]+:/g, (match) => EMOJI[match] || match)
  }
  return { render }
}
