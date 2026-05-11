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

export function deleteComment(id) {
  return request.delete(`/admin/comments/${id}`)
}

export function updateCommentStatus(id, status) {
  return request.put(`/admin/comments/${id}/status`, null, { params: { status } })
}

export function getCategories() {
  return request.get('/categories')
}

export function getTags() {
  return request.get('/tags')
}

export function getUserProfile() {
  return request.get('/user/profile')
}

export function getAdminComments(params) {
  return request.get('/admin/comments', { params })
}

export function updateAvatar(data) {
  return request.put('/admin/user/avatar', data)
}

export function getFeaturedArticle() {
  return request.get('/featured')
}

export function getFriends() {
  return request.get('/friends')
}

export function getAdminFriends() {
  return request.get('/admin/friends')
}

export function createFriend(data) {
  return request.post('/admin/friends', data)
}

export function updateFriend(id, data) {
  return request.put(`/admin/friends/${id}`, data)
}

export function deleteFriend(id) {
  return request.delete(`/admin/friends/${id}`)
}

export function applyFriend(data) {
  return request.post('/friends/apply', data)
}

export function updateFriendStatus(id, status) {
  return request.put(`/admin/friends/${id}/status`, null, { params: { status } })
}

export function getDashboard() {
  return request.get('/admin/dashboard')
}

export function updateProfile(data) {
  return request.put('/admin/user/profile', data)
}

export function changePassword(data) {
  return request.put('/admin/user/password', data)
}

export function subscribe(email) {
  return request.post('/subscribe', { email })
}

export function getSubscribers() {
  return request.get('/admin/subscribers')
}

export function deleteSubscriber(id) {
  return request.delete(`/admin/subscribers/${id}`)
}
