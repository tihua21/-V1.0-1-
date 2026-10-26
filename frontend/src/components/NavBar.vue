<template>
  <div class="navbar">
    <span class="brand">智能会议室管理系统</span>
    <el-menu mode="horizontal" :default-active="$route.path" router class="menu">
      <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">{{ m.name }}</el-menu-item>
    </el-menu>
    <div class="right">
      <span class="user">{{ realName }}（{{ role === 'ADMIN' ? '管理员' : '普通用户' }}）</span>
      <el-button size="small" type="danger" plain @click="logout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
// 菜单直接来自登录接口返回：普通用户不含管理员入口
const user = JSON.parse(localStorage.getItem('user') || '{}')
const menus = ref(user.menus || [])
const realName = ref(user.realName || '')
const role = ref(user.role || '')

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  background: #fff;
}
.brand {
  font-weight: bold;
  margin-right: 30px;
  white-space: nowrap;
}
.menu {
  flex: 1;
  border-bottom: none;
}
.right {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}
.user {
  color: #606266;
  font-size: 14px;
}
</style>
