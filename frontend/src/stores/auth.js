import { defineStore } from 'pinia'
import { authAPI } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null'),
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token,
    // TODO: Implement proper role-based authorization from backend
    // Currently using simplified username check - should be replaced with proper role checking
    isAdmin: (state) => {
      // This would need to be determined based on user roles from backend
      // For now, simplified check
      return state.user?.username === 'admin'
    }
  },
  
  actions: {
    async login(credentials) {
      try {
        const response = await authAPI.login(credentials)
        if (response.success) {
          this.token = response.data.token
          this.user = {
            userId: response.data.userId,
            username: response.data.username
          }
          localStorage.setItem('token', this.token)
          localStorage.setItem('user', JSON.stringify(this.user))
          return response
        }
        throw new Error(response.message)
      } catch (error) {
        console.error('Login failed:', error)
        throw error
      }
    },
    
    async register(userData) {
      try {
        const response = await authAPI.register(userData)
        return response
      } catch (error) {
        console.error('Registration failed:', error)
        throw error
      }
    },
    
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
