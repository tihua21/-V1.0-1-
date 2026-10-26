<template>
  <div>
    <NavBar />
    <div class="detail" v-loading="loading">
      <template v-if="room">
        <el-page-header content="会议室详情" @back="$router.push('/rooms')" />
        <el-descriptions :title="room.name" :column="2" border style="margin-top: 20px;">
          <el-descriptions-item label="编号">{{ room.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ room.location }}</el-descriptions-item>
          <el-descriptions-item label="容纳人数">{{ room.capacity }} 人</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="room.status === 1 ? 'success' : 'info'">
              {{ room.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="设备" :span="2">
            <el-tag v-for="f in room.facilities" :key="f" size="small" style="margin-right: 4px;">{{ f }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ room.description || '—' }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 24px;">可预约时段（预置）</h3>
        <!-- 调 GET /api/time-slots，无参数 -->
        <el-table :data="slots" border stripe size="small" style="max-width: 640px;">
          <el-table-column prop="slotName" label="时段名称" />
          <el-table-column label="时间">
            <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
          </el-table-column>
          <el-table-column prop="durationMinutes" label="时长（分钟）" width="110" />
        </el-table>

        <el-button type="primary" style="margin-top: 20px;" :disabled="room.status !== 1" @click="goBook">
          立即预约
        </el-button>
      </template>
      <el-empty v-else-if="!loading" description="未找到会议室信息，请从列表页进入" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getRooms, getTimeSlots } from '../api'

const route = useRoute()
const router = useRouter()
const room = ref(null)
const slots = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    // 优先用列表页带来的数据；直接刷新则重新调 GET /api/rooms 按 id 找
    if (history.state?.room) {
      room.value = JSON.parse(history.state.room)
    } else {
      const res = await getRooms()
      room.value = res.data.data.find(r => r.id === Number(route.params.id)) || null
    }
    const slotRes = await getTimeSlots()
    if (slotRes.data.code === 0) {
      slots.value = slotRes.data.data
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function goBook() {
  router.push({ path: '/booking/new', query: { roomId: room.value.id, roomName: room.value.name } })
}

onMounted(load)
</script>

<style scoped>
.detail {
  max-width: 900px;
  margin: 20px auto;
  padding: 0 20px;
}
</style>
