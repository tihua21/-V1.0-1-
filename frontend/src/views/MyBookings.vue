<template>
  <div>
    <NavBar />
    <div class="mine">
      <h2>我的预约</h2>
      <el-table :data="bookings" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roomName" label="会议室" />
        <el-table-column prop="bookingDate" label="日期" width="120" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ row.startTime?.slice(11) }} - {{ row.endTime?.slice(11) }}
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="!loading && bookings.length === 0" style="text-align: center; color: #999;">暂无预约记录</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getMyBookings } from '../api'

const bookings = ref([])
const loading = ref(false)

// 状态：0 待审核 / 1 已预约 / 2 已驳回 / 3 已取消 / 4 已结束
const STATUS = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已预约', type: 'success' },
  2: { text: '已驳回', type: 'danger' },
  3: { text: '已取消', type: 'info' },
  4: { text: '已结束', type: 'info' }
}
const statusText = s => STATUS[s]?.text ?? s
const statusType = s => STATUS[s]?.type ?? 'info'

async function load() {
  loading.value = true
  try {
    // 调 GET /api/bookings/mine，无筛选参数（列表页）
    const res = await getMyBookings()
    if (res.data.code === 0) {
      bookings.value = res.data.data
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '预约记录加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mine {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
}
</style>
