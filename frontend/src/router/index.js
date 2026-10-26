import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/rooms', name: 'RoomList', component: () => import('../views/RoomList.vue') },
  { path: '/rooms/:id', name: 'RoomDetail', component: () => import('../views/RoomDetail.vue') },
  { path: '/booking/new', name: 'BookingForm', component: () => import('../views/BookingForm.vue') },
  { path: '/bookings/mine', name: 'MyBookings', component: () => import('../views/MyBookings.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
