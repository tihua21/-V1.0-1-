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

        <h3 style="margin-top: 24px;">查看某日期的可用时段</h3>
        <div style="display:flex; align-items:center; gap:12px; margin-bottom:12px;">
          <el-date-picker v-model="selectedDate" type="date" value-format="YYYY-MM-DD"
                          placeholder="选择日期"
                          :disabled-date="d => d.getTime() < Date.now() - 86400000"
                          @change="loadOccupied" />
          <span v-if="occupiedLoading" style="color:#999; font-size:13px;">加载中…</span>
        </div>

        <el-table :data="slots" border stripe size="small" style="max-width: 640px;">
          <el-table-column prop="slotName" label="时段名称" width="140" />
          <el-table-column label="时间">
            <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
          </el-table-column>
          <el-table-column label="状态" width="140">
            <template #default="{ row }">
              <el-tag v-if="isOccupied(row)" type="danger" size="small">已被占用</el-tag>
              <el-tag v-else type="success" size="small">可预订</el-tag>
            </template>
          </el-table-column>
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
import { getRooms, getTimeSlots, getOccupiedSlots } from '../api'

const route = useRoute()
const router = useRouter()
const room = ref(null)
const slots = ref([])
const occupied = ref([])   // { start, end, status, purpose }
const loading = ref(false)
const occupiedLoading = ref(false)
const selectedDate = ref(new Date().toISOString().slice(0, 10))

// 判断某个预置时段在 selectedDate 那天是否被占用
function isOccupied(slot) {
  if (!occupied.value.length) return false
  const slotStart = `${selectedDate.value} ${slot.startTime}`
  const slotEnd = `${selectedDate.value} ${slot.endTime}`
  // 半开区间冲突判定
  return occupied.value.some(o => o.startTime < slotEnd && o.endTime > slotStart)
}

async function loadOccupied() {
  if (!selectedDate.value) return
  if (!room.value) return
  occupiedLoading.value = true
  try {
    const res = await getOccupiedSlots(room.value.id, selectedDate.value)
    if (res.data.code === 0) {
      occupied.value = res.data.data
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '加载占用时段失败')
  } finally {
    occupiedLoading.value = false
  }
}

function goBook() {
  router.push({
    path: '/booking/new',
    query: {
      roomId: room.value.id,
      roomName: room.value.name,
      date: selectedDate.value
    }
  })
}

async function load() {
  loading.value = true
  try {
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
    // 默认加载今天的占用情况
    if (room.value) {
      loadOccupied()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
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
