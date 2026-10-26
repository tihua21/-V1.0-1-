<template>
  <div>
    <NavBar />
    <div class="room-list">
      <h2>会议室列表</h2>
      <!-- 筛选区：对应 GET /api/rooms 的 name / minCapacity / location / equipment -->
      <el-form inline class="filter">
        <el-form-item label="名称">
          <el-input v-model="query.name" placeholder="会议室名称" clearable />
        </el-form-item>
        <el-form-item label="容量≥">
          <el-input-number v-model="query.minCapacity" :min="1" :max="200" placeholder="人数" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="query.location" placeholder="如 A 座" />
        </el-form-item>
        <el-form-item label="设备">
          <el-input v-model="query.equipment" placeholder="如 投影" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRooms">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rooms" border stripe v-loading="loading">
        <el-table-column prop="roomNo" label="编号" width="90" />
        <el-table-column prop="name" label="会议室名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column label="设备">
          <template #default="{ row }">
            <el-tag v-for="f in row.facilities" :key="f" size="small" style="margin-right: 4px;">{{ f }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="goDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="!loading && rooms.length === 0" style="text-align: center; color: #999;">暂无符合条件的会议室</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getRooms } from '../api'

const router = useRouter()
const rooms = ref([])
const loading = ref(false)

const query = ref({ name: '', minCapacity: null, location: '', equipment: '' })

async function loadRooms() {
  loading.value = true
  try {
    // 调 GET /api/rooms，参数：name、minCapacity、location、equipment（均可选）
    const params = {}
    if (query.value.name) params.name = query.value.name
    if (query.value.minCapacity) params.minCapacity = query.value.minCapacity
    if (query.value.location) params.location = query.value.location
    if (query.value.equipment) params.equipment = query.value.equipment
    const res = await getRooms(params)
    if (res.data.code === 0) {
      rooms.value = res.data.data
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '会议室加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.value = { name: '', minCapacity: null, location: '', equipment: '' }
  loadRooms()
}

function goDetail(row) {
  // 把整行数据带给详情页，详情页无需再查接口
  router.push({ path: `/rooms/${row.id}`, state: { room: JSON.stringify(row) } })
}

onMounted(loadRooms)
</script>

<style scoped>
.room-list {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
}
.filter {
  margin-bottom: 10px;
}
</style>
