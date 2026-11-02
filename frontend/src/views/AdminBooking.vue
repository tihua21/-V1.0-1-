<template>
  <div>
    <NavBar />
    <div class="admin-booking">
      <h2>全部预约（管理员）</h2>

      <!-- 筛选区 -->
      <el-form inline class="filter">
        <el-form-item label="会议室">
          <el-select v-model="query.roomId" placeholder="全部" clearable style="width:160px;">
            <el-option v-for="r in rooms" :key="r.id" :value="r.id" :label="r.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" clearable placeholder="全部" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:140px;">
            <el-option v-for="s in statusOptions" :key="s.value" :value="s.value" :label="s.label" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="bookings" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="bookingNo" label="预约编号" width="180">
          <template #default="{ row }">{{ row.bookingNo || '—' }}</template>
        </el-table-column>
        <el-table-column prop="roomName" label="会议室" />
        <el-table-column label="时间" width="220">
          <template #default="{ row }">
            {{ row.bookingDate }} {{ row.startTime?.slice(11) }} - {{ row.endTime?.slice(11) }}
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cancelReason" label="取消原因" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="danger"
                       :disabled="!canCancel(row)"
                       @click="openCancel(row)">强制取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="!loading && bookings.length === 0" style="text-align: center; color: #999;">暂无预约记录</p>
    </div>

    <!-- 取消弹窗 -->
    <el-dialog v-model="cancelDialog.visible" title="强制取消预约" width="460px">
      <p>将取消这条预约，该时段立即释放。操作会写入审核日志。</p>
      <el-form :model="cancelDialog.form" label-width="90px" style="margin-top:10px;">
        <el-form-item label="取消原因" required>
          <el-input v-model="cancelDialog.form.reason" type="textarea" :rows="2"
                    maxlength="200" show-word-limit placeholder="如：预约已过期无人使用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialog.visible = false">再想想</el-button>
        <el-button type="danger" :loading="submitting" @click="doCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { adminGetAllBookings, adminCancelBooking, getRooms } from '../api'

const bookings = ref([])
const rooms = ref([])
const loading = ref(false)
const submitting = ref(false)
const currentBooking = ref(null)

const query = reactive({ roomId: null, date: null, status: null })
const cancelDialog = reactive({ visible: false, form: { reason: '' } })

const statusOptions = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已预约' },
  { value: 2, label: '已驳回' },
  { value: 3, label: '已取消' },
  { value: 4, label: '已结束' }
]
const STATUS_TEXT = { 0: '待审核', 1: '已预约', 2: '已驳回', 3: '已取消', 4: '已结束' }
const STATUS_TYPE = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info', 4: 'info' }
const statusText = s => STATUS_TEXT[s] ?? s
const statusType = s => STATUS_TYPE[s] ?? 'info'

function canCancel(row) {
  // 管理员可取消待审核(0)或已预约(1)的任意预约
  return row.status === 0 || row.status === 1
}

function openCancel(row) {
  currentBooking.value = row
  cancelDialog.form.reason = ''
  cancelDialog.visible = true
}

async function doCancel() {
  if (!cancelDialog.form.reason.trim()) {
    ElMessage.warning('必须填写取消原因')
    return
  }
  submitting.value = true
  try {
    const res = await adminCancelBooking(currentBooking.value.id, cancelDialog.form.reason.trim())
    if (res.data.code === 0) {
      ElMessage.success('已取消')
      cancelDialog.visible = false
      load()
    } else {
      ElMessage.error(res.data.msg || '取消失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '取消失败')
  } finally {
    submitting.value = false
  }
}

function reset() {
  query.roomId = null
  query.date = null
  query.status = null
  load()
}

async function load() {
  loading.value = true
  try {
    const params = {}
    if (query.roomId) params.roomId = query.roomId
    if (query.date) params.date = query.date
    if (query.status != null) params.status = query.status
    const res = await adminGetAllBookings(params)
    if (res.data.code === 0) {
      bookings.value = res.data.data
    }
  } catch (e) {
    if (e.response?.status === 403) {
      ElMessage.error('无权访问管理员功能')
    } else {
      ElMessage.error(e.response?.data?.msg || '加载失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  load()
  // 加载会议室列表（筛选下拉用）
  try {
    const r = await getRooms()
    if (r.data.code === 0) rooms.value = r.data.data
  } catch (e) { /* 静默 */ }
})
</script>

<style scoped>
.admin-booking {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}
.filter { margin-bottom: 10px; }
</style>
