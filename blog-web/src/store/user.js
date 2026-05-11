import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/token'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())

  function login(newToken) {
    token.value = newToken
    setToken(newToken)
  }

  function logout() {
    token.value = ''
    removeToken()
  }

  function isLoggedIn() {
    return !!token.value
  }

  return { token, login, logout, isLoggedIn }
})
