import { defineStore } from 'pinia'
import { authAPI } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null'),
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token,
    
    // Check if user has admin role
    isAdmin: (state) => {
      return state.user?.roles?.includes('admin') || state.user?.roles?.includes('ADMIN')
    },
    
    // Check if user has teacher role
    isTeacher: (state) => {
      return state.user?.roles?.includes('teacher') || state.user?.roles?.includes('TEACHER')
    },
    
    // Check if user has student role
    isStudent: (state) => {
      return state.user?.roles?.includes('student') || state.user?.roles?.includes('STUDENT')
    },
    
    // Get all user roles as display text
    userRolesText: (state) => {
      if (!state.user?.roles || state.user.roles.length === 0) {
        return '普通用户'
      }
      const roleMap = {
        'admin': '管理员',
        'ADMIN': '管理员',
        'teacher': '教师',
        'TEACHER': '教师',
        'student': '学生',
        'STUDENT': '学生'
      }
      return state.user.roles.map(role => roleMap[role] || role).join(', ')
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
            username: response.data.username,
            roles: response.data.roles || []
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
