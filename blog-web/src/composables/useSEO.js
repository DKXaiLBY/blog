export function useSEO({ title, description, image, publishedAt } = {}) {
  const siteName = "DKX's Blog"
  const siteUrl = window.location.origin

  if (title) {
    document.title = `${title} - ${siteName}`
  } else {
    document.title = siteName
  }

  let metaDesc = document.querySelector('meta[name="description"]')
  if (!metaDesc) {
    metaDesc = document.createElement('meta')
    metaDesc.name = 'description'
    document.head.appendChild(metaDesc)
  }
  metaDesc.content = description || 'DKX 的技术博客 — 分享 Java、Vue、Spring Boot 等技术文章'

  const setMeta = (attr, value, isProperty = false) => {
    const selector = isProperty
      ? `meta[property="${attr}"]`
      : `meta[name="${attr}"]`
    let el = document.querySelector(selector)
    if (!el) {
      el = document.createElement('meta')
      if (isProperty) {
        el.setAttribute('property', attr)
      } else {
        el.setAttribute('name', attr)
      }
      document.head.appendChild(el)
    }
    el.setAttribute('content', value)
  }

  const pageTitle = title ? `${title} - ${siteName}` : siteName
  const pageDesc = metaDesc.content
  const pageUrl = window.location.href

  setMeta('og:title', pageTitle, true)
  setMeta('og:description', pageDesc, true)
  setMeta('og:type', title ? 'article' : 'website', true)
  setMeta('og:url', pageUrl, true)
  setMeta('og:site_name', siteName, true)
  if (image) {
    setMeta('og:image', image.startsWith('http') ? image : siteUrl + image, true)
  }

  setMeta('twitter:card', image ? 'summary_large_image' : 'summary')
  setMeta('twitter:title', pageTitle)
  setMeta('twitter:description', pageDesc)
  if (image) {
    setMeta('twitter:image', image.startsWith('http') ? image : siteUrl + image)
  }

  if (publishedAt) {
    setMeta('article:published_time', publishedAt, true)
  }
}
