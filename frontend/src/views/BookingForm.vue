<template>
  <div>
    <NavBar />
    <div class="booking-form">
      <el-page-header content="提交预约" @back="$router.back()" />
      <el-card style="margin-top: 20px; max-width: 560px;">
        <el-alert v-if="feedback.message" :title="feedback.message" :type="feedback.type"
                  show-icon :closable="false" style="margin-bottom: 18px;" />
        <el-form :model="form" label-width="90px">
          <el-form-item label="会议室">
            <el-input :model-value="roomName" disabled />
          </el-form-item>
          <el-form-item label="预约日期">
            <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD"
                            placeholder="选择日期" :disabled-date="isDateDisabled" />
          </el-form-item>
          <el-form-item label="预置时段">
            <el-select v-model="form.timeSlotId" placeholder="选择时段" style="width: 100%;">
              <el-option v-for="s in slots" :key="s.id" :value="s.id"
                         :disabled="isPastSlot(s)" :label="slotLabel(s)" />
            </el-select>
          </el-form-item>
          <el-form-item label="用途">
            <el-input v-model="form.purpose" type="textarea" :rows="2"
                      maxlength="200" show-word-limit placeholder="会议主题，如：项目周会" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submit">提交预约</el-button>
            <el-button v-if="feedback.type === 'success'" type="success" plain
                       @click="$router.push('/bookings/mine')">查看我的预约</el-button>
            <el-button @click="$router.back()">返回</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getTimeSlots, createBooking } from '../api'

const route = useRoute()
const roomName = ref(route.query.roomName || '')
const slots = ref([])
const submitting = ref(false)
// 常驻提示：冲突等失败信息不会像 toast 一样 3 秒消失
const feedback = ref({ type: '', message: '' })

function pad(n) {
  return String(n).padStart(2, '0')
}

function dateStr(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function todayStr() {
  return dateStr(new Date())
}

function nowHHmm() {
  const d = new Date()
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const form = ref({
  roomId: Number(route.query.roomId),
  // 默认明天，避免演示时误选今天已过的时段
  date: route.query.date || dateStr(new Date(Date.now() + 86400000)),
  timeSlotId: null,
  purpose: ''
})

function isDateDisabled(d) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return d.getTime() < today.getTime()
}

function isPastSlot(slot) {
  // 日期早于今天，或今天且结束时间已过，都算已过
  return `${form.value.date} ${slot.endTime}` <= `${todayStr()} ${nowHHmm()}`
}

function slotLabel(slot) {
  return `${slot.slotName}（${slot.startTime} - ${slot.endTime}）${isPastSlot(slot) ? ' 已过' : ''}`
}

watch(() => [form.value.date, form.value.timeSlotId], () => {
  feedback.value = { type: '', message: '' }
})

onMounted(async () => {
  if (!form.value.roomId) {
    ElMessage.warning('请先从会议室详情页进入')
    router.push('/rooms')
    return
  }
  // 调 GET /api/time-slots，加载可选预置时段
  const res = await getTimeSlots()
  if (res.data.code === 0) {
    slots.value = res.data.data
  }
})

async function submit() {
  if (!form.value.date || !form.value.timeSlotId || !form.value.purpose.trim()) {
    ElMessage.warning('请完整填写日期、时段和用途')
    return
  }
  const selected = slots.value.find(s => s.id === form.value.timeSlotId)
  if (selected && isPastSlot(selected)) {
    ElMessage.warning('该时段已过，请选择未来时段')
    return
  }
  feedback.value = { type: '', message: '' }
  submitting.value = true
  try {
    // 调 POST /api/bookings，参数：roomId、date、timeSlotId、purpose
    const res = await createBooking(form.value)
    if (res.data.code === 0) {
      // 不自动跳转：停留在本页，方便紧接着再提交一次演示冲突提示
      feedback.value = {
        type: 'success',
        message: `提交成功！预约编号：${res.data.data.bookingNo || res.data.data.bookingId}，状态：${res.data.data.status}`
      }
    }
  } catch (e) {
    // 400 时段已过/信息不完整；409 该时段已被占用
    feedback.value = {
      type: 'error',
      message: e.response?.data?.msg || '提交失败，请稍后重试'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.booking-form {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}
</style>
