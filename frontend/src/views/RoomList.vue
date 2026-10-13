<template>
  <div class="room-list">
    <div class="header">
      <h2>会议室列表</h2>
      <el-button type="danger" @click="logout">退出登录</el-button>
    </div>
    <el-table :data="rooms" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="会议室名称" />
      <el-table-column prop="location" label="位置" />
      <el-table-column prop="capacity" label="容纳人数" width="120" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'available' ? 'success' : 'info'">
            {{ row.status === 'available' ? '可用' : row.status }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <p v-if="!loading && rooms.length === 0" style="text-align: center; color: #999;">
      暂无会议室数据（0 条也算正常）
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRooms } from '../api'

const router = useRouter()
const rooms = ref([])
const loading = ref(false)

async function loadRooms() {
  loading.value = true
  try {
    const res = await getRooms()
    if (res.data.code === 0) {
      rooms.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('获取会议室列表失败，请检查后端是否启动')
  } finally {
    loading.value = false
  }
}

function logout() {
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  loadRooms()
})
</script>

<style scoped>
.room-list {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
