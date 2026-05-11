import request from './request'

export function getArticles(params) {
  return request.get('/articles', { params })
}

export function getArticleDetail(id) {
  return request.get(`/articles/${id}`)
}

export function searchArticles(params) {
  return request.get('/articles/search', { params })
}

export function getArchives() {
  return request.get('/articles/archives')
}

export function likeArticle(id) {
  return request.post(`/articles/${id}/like`)
}

export function getPrevNext(id) {
  return request.get(`/articles/${id}/prev-next`)
}

export function createArticle(data) {
  return request.post('/admin/articles', data)
}

export function updateArticle(id, data) {
  return request.put(`/admin/articles/${id}`, data)
}

export function deleteArticle(id) {
  return request.delete(`/admin/articles/${id}`)
}

export function getAdminArticles(params) {
  return request.get('/admin/articles', { params })
}

export function getAdminArticleDetail(id) {
  return request.get(`/admin/articles/${id}`)
}

export function getRelatedArticles(id, limit = 3) {
  return request.get(`/articles/${id}/related`, { params: { limit } })
}

export function batchDeleteArticles(ids) {
  return request.post('/admin/articles/batch-delete', { ids })
}

export function batchUpdateStatus(ids, status) {
  return request.post('/admin/articles/batch-status', { ids, status })
}
