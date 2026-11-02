import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/rooms', name: 'RoomList', component: () => import('../views/RoomList.vue') },
  { path: '/rooms/:id', name: 'RoomDetail', component: () => import('../views/RoomDetail.vue') },
  { path: '/booking/new', name: 'BookingForm', component: () => import('../views/BookingForm.vue') },
  { path: '/bookings/mine', name: 'MyBookings', component: () => import('../views/MyBookings.vue') },
  // 管理员路由（角色守卫）
  {
    path: '/admin/audit',
    name: 'AdminBooking',
    component: () => import('../views/AdminBooking.vue'),
    meta: { role: 'ADMIN' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页；管理员路由还校验 role
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  // 管理员路由守卫
  if (to.meta?.role) {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.role !== to.meta.role) {
      next('/rooms')
      return
    }
  }
  next()
})

export default router
