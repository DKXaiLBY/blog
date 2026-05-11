import request from './request'

export function getComments(articleId) {
  return request.get(`/articles/${articleId}/comments`)
}

export function addComment(data) {
  return request.post('/comments', data)
}

export function getCaptcha() {
  return request.get('/captcha')
}

export function getAdminComments(params) {
  return request.get('/admin/comments', { params })
}

export function deleteComment(id) {
  return request.delete(`/admin/comments/${id}`)
}

export function updateCommentStatus(id, status) {
  return request.put(`/admin/comments/${id}/status`, null, { params: { status } })
}
