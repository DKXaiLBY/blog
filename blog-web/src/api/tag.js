import request from './request'

export function getTags() {
  return request.get('/tags')
}
