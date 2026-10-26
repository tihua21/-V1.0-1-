<template>
  <div>
    <NavBar />
    <div class="booking-form">
      <el-page-header content="提交预约" @back="$router.back()" />
      <el-card style="margin-top: 20px; max-width: 560px;">
        <el-form :model="form" label-width="90px">
          <el-form-item label="会议室">
            <el-input :model-value="roomName" disabled />
          </el-form-item>
          <el-form-item label="预约日期">
            <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD"
                            placeholder="选择日期" :disabled-date="d => d.getTime() < Date.now() - 86400000" />
          </el-form-item>
          <el-form-item label="预置时段">
            <el-select v-model="form.timeSlotId" placeholder="选择时段" style="width: 100%;">
              <el-option v-for="s in slots" :key="s.id" :value="s.id"
                         :label="`${s.slotName}（${s.startTime} - ${s.endTime}）`" />
            </el-select>
          </el-form-item>
          <el-form-item label="用途">
            <el-input v-model="form.purpose" type="textarea" :rows="2"
                      maxlength="200" show-word-limit placeholder="会议主题，如：项目周会" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submit">提交预约</el-button>
            <el-button @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getTimeSlots, createBooking } from '../api'

const route = useRoute()
const router = useRouter()
const roomName = ref(route.query.roomName || '')
const slots = ref([])
const submitting = ref(false)

const form = ref({
  roomId: Number(route.query.roomId),
  date: '',
  timeSlotId: null,
  purpose: ''
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
  submitting.value = true
  try {
    // 调 POST /api/bookings，参数：roomId、date、timeSlotId、purpose
    const res = await createBooking(form.value)
    if (res.data.code === 0) {
      ElMessage.success(`提交成功，预约号（记录ID）：${res.data.data.bookingId}，状态：${res.data.data.status}`)
      router.push('/bookings/mine')
    }
  } catch (e) {
    // 400 预约信息不完整或时段不合法 / 409 该会议室已停用或该时段已被占用
    ElMessage.error(e.response?.data?.msg || '提交失败，请稍后重试')
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
