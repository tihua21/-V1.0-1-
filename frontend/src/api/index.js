import axios from 'axios'

// Axios 封装
const request = axios.create({
  baseURL: '/api',
  timeout: 5000
})

// 登录
export function login(data) {
  return request.post('/login', data)
}

// 查询会议室列表
export function getRooms() {
  return request.get('/rooms')
}

export default request
