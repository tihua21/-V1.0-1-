<template>
  <div>
    <NavBar />
    <div class="mine">
      <h2>我的预约</h2>

      <el-table :data="bookings" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="bookingNo" label="预约编号" width="180">
          <template #default="{ row }">
            {{ row.bookingNo || '生成中…' }}
          </template>
        </el-table-column>
        <el-table-column prop="roomName" label="会议室" />
        <el-table-column label="时间" width="210">
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary"
                       :disabled="!canModify(row)"
                       @click="openModify(row)">修改</el-button>
            <el-button size="small" link type="danger"
                       :disabled="!canCancel(row)"
                       @click="openCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="!loading && bookings.length === 0" style="text-align: center; color: #999;">暂无预约记录</p>
    </div>

    <!-- 修改弹窗 -->
    <el-dialog v-model="modifyDialog.visible" title="修改预约" width="520px">
      <el-form :model="modifyDialog.form" label-width="90px">
        <el-form-item label="会议室">
          <el-input :model-value="currentBooking?.roomName" disabled />
        </el-form-item>
        <el-form-item label="日期">
          <el-input :model-value="currentBooking?.bookingDate" disabled />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="modifyDialog.form.timeSlotId" placeholder="选择时段" style="width: 100%;">
            <el-option v-for="s in slots" :key="s.id" :value="s.id"
                       :label="`${s.slotName}（${s.startTime} - ${s.endTime}）`" />
          </el-select>
          <div style="font-size:12px; color:#999; margin-top:4px;">不改时段则保持不变；换时段会重新做冲突检测</div>
        </el-form-item>
        <el-form-item label="用途">
          <el-input v-model="modifyDialog.form.purpose" type="textarea" :rows="2"
                    maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modifyDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doUpdate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 取消弹窗 -->
    <el-dialog v-model="cancelDialog.visible" title="取消预约" width="460px">
      <p>确定要取消这条预约吗？取消后该时段可被他人重新预订。</p>
      <el-form :model="cancelDialog.form" label-width="90px" style="margin-top:10px;">
        <el-form-item label="取消原因" required>
          <el-input v-model="cancelDialog.form.reason" type="textarea" :rows="2"
                    maxlength="200" show-word-limit placeholder="请填写取消原因" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getMyBookings, getTimeSlots, updateBooking, cancelBooking } from '../api'

const bookings = ref([])
const slots = ref([])
const loading = ref(false)
const submitting = ref(false)
const currentBooking = ref(null)

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

// 可修改/可取消：状态为 0 或 1，且未开始
function canModify(row) {
  return (row.status === 0 || row.status === 1)
    && row.startTime && new Date(row.startTime.replace(' ', 'T')) > new Date()
}
function canCancel(row) {
  return canModify(row) // 规则相同
}

const modifyDialog = reactive({ visible: false, form: { purpose: '', timeSlotId: null } })
const cancelDialog = reactive({ visible: false, form: { reason: '' } })

function openModify(row) {
  currentBooking.value = row
  modifyDialog.form = { purpose: row.purpose, timeSlotId: row.slotId }
  modifyDialog.visible = true
}

function openCancel(row) {
  currentBooking.value = row
  cancelDialog.form.reason = ''
  cancelDialog.visible = true
}

async function doUpdate() {
  if (!modifyDialog.form.purpose.trim()) {
    ElMessage.warning('用途不能为空')
    return
  }
  submitting.value = true
  try {
    const data = { purpose: modifyDialog.form.purpose.trim() }
    // 只有真正换了时段才带上
    if (modifyDialog.form.timeSlotId && modifyDialog.form.timeSlotId !== currentBooking.value.slotId) {
      data.timeSlotId = modifyDialog.form.timeSlotId
    }
    const res = await updateBooking(currentBooking.value.id, data)
    if (res.data.code === 0) {
      ElMessage.success('修改成功')
      modifyDialog.visible = false
      load()
    } else {
      ElMessage.error(res.data.msg || '修改失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '修改失败')
  } finally {
    submitting.value = false
  }
}

async function doCancel() {
  if (!cancelDialog.form.reason.trim()) {
    ElMessage.warning('请填写取消原因')
    return
  }
  submitting.value = true
  try {
    const res = await cancelBooking(currentBooking.value.id, cancelDialog.form.reason.trim())
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

async function load() {
  loading.value = true
  try {
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

onMounted(async () => {
  load()
  // 加载时段（修改弹窗的下拉）
  try {
    const slotRes = await getTimeSlots()
    if (slotRes.data.code === 0) {
      slots.value = slotRes.data.data
    }
  } catch (e) { /* 静默 */ }
})
</script>

<style scoped>
.mine {
  max-width: 1100px;
  margin: 20px auto;
  padding: 0 20px;
}
</style>
