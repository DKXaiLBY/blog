import request from './request'

export function subscribe(email) {
  return request.post('/subscribe', { email })
}

export function getSubscribers() {
  return request.get('/admin/subscribers')
}

export function deleteSubscriber(id) {
  return request.delete(`/admin/subscribers/${id}`)
}
