import request from './request'

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
