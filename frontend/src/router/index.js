import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/competitions'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/competitions',
    name: 'Competitions',
    component: () => import('../views/Competitions.vue')
  },
  {
    path: '/competitions/:id',
    name: 'CompetitionDetail',
    component: () => import('../views/CompetitionDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my-registrations',
    name: 'MyRegistrations',
    component: () => import('../views/MyRegistrations.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/apply-competition',
    name: 'ApplyCompetition',
    component: () => import('../views/ApplyCompetition.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/teams',
    name: 'Teams',
    component: () => import('../views/Teams.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my-submissions',
    name: 'MySubmissions',
    component: () => import('../views/MySubmissions.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('../views/Notifications.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/teacher-review',
    name: 'TeacherReview',
    component: () => import('../views/TeacherReview.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue')
      },
      {
        path: 'competitions',
        name: 'AdminCompetitions',
        component: () => import('../views/admin/Competitions.vue')
      },
      {
        path: 'registrations',
        name: 'AdminRegistrations',
        component: () => import('../views/admin/Registrations.vue')
      },
      {
        path: 'judges',
        name: 'AdminJudges',
        component: () => import('../views/admin/Judges.vue')
      },
      {
        path: 'awards',
        name: 'AdminAwards',
        component: () => import('../views/admin/Awards.vue')
      },
      {
        path: 'system-time',
        name: 'AdminSystemTime',
        component: () => import('../views/admin/SystemTime.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next('/competitions')
  } else {
    next()
  }
})

export default router
