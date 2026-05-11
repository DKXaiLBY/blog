import request from './request'

export function getFeaturedArticle() {
  return request.get('/featured')
}
