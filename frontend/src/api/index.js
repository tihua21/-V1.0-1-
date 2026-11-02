import axios from 'axios'
import router from '../router'

// Axios 封装：baseURL /api 由 Vite 代理转发到后端 8080
const request = axios.create({
  baseURL: '/api',
  timeout: 5000
})

// 请求拦截：自动携带 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

// 响应拦截：401 统一跳登录页；403 也统一提示
request.interceptors.response.use(
  res => res,
  err => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
    return Promise.reject(err)
  }
)

// ==================== 第 9 周接口 ====================

export function login(data) {
  return request.post('/login', data)
}

export function getRooms(params) {
  return request.get('/rooms', { params })
}

export function getTimeSlots() {
  return request.get('/time-slots')
}

export function createBooking(data) {
  return request.post('/bookings', data)
}

export function getMyBookings(params) {
  return request.get('/bookings/mine', { params })
}

// ==================== 第 10 周接口 ====================

// 会议室某天占用时段
export function getOccupiedSlots(roomId, date) {
  return request.get(`/rooms/${roomId}/occupied`, { params: { date } })
}

// 修改预约
export function updateBooking(id, data) {
  return request.put(`/bookings/${id}`, data)
}

// 取消预约（普通用户）
export function cancelBooking(id, reason) {
  return request.delete(`/bookings/${id}`, { data: { reason } })
}

// 管理员看全部预约
export function adminGetAllBookings(params) {
  return request.get('/admin/bookings', { params })
}

// 管理员取消异常预约
export function adminCancelBooking(id, reason) {
  return request.put(`/admin/bookings/${id}/cancel`, { reason })
}

export default request
