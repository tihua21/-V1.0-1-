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

// 响应拦截：401 统一跳登录页
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

// 登录 FR-01
export function login(data) {
  return request.post('/login', data)
}

// 会议室列表 FR-03，params: { name, minCapacity, location, equipment } 均可选
export function getRooms(params) {
  return request.get('/rooms', { params })
}

// 预置时段（预约表单数据来源）
export function getTimeSlots() {
  return request.get('/time-slots')
}

// 提交预约 FR-04，data: { roomId, date, timeSlotId, purpose }
export function createBooking(data) {
  return request.post('/bookings', data)
}

// 我的预约 FR-06，params: { status, roomId, date } 均可选
export function getMyBookings(params) {
  return request.get('/bookings/mine', { params })
}

export default request
