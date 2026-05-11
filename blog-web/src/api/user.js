import request from './request'

export function getUserProfile() {
  return request.get('/user/profile')
}

export function updateAvatar(data) {
  return request.put('/admin/user/avatar', data)
}

export function updateProfile(data) {
  return request.put('/admin/user/profile', data)
}

export function changePassword(data) {
  return request.put('/admin/user/password', data)
}

export function getDashboard() {
  return request.get('/admin/dashboard')
}
