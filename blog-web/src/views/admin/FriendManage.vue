<template>
  <div class="friend-manage">
    <div class="page-header">
      <h2>
        友链管理
        <el-badge v-if="pendingCount" :value="pendingCount" class="pending-badge" />
      </h2>
      <el-button type="primary" size="small" @click="openDialog()">添加友链</el-button>
    </div>

    <el-table :data="friends" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" width="140" />
      <el-table-column prop="url" label="链接" min-width="200">
        <template #default="{ row }">
          <a :href="row.url" target="_blank" style="color: var(--accent)">{{ row.url }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" width="180" />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="warning" size="small">待审核</el-tag>
          <el-tag v-else-if="row.status === 1" type="success" size="small">已通过</el-tag>
          <el-tag v-else type="info" size="small">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" text type="success" @click="handleApprove(row.id)">通过</el-button>
            <el-button size="small" text type="warning" @click="handleReject(row.id)">拒绝</el-button>
          </template>
          <el-button size="small" text type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑友链' : '添加友链'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="网站名称" />
        </el-form-item>
        <el-form-item label="链接">
          <el-input v-model="form.url" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="简短描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusBool" active-text="显示" inactive-text="隐藏" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminFriends, createFriend, updateFriend, deleteFriend, updateFriendStatus } from '@/api/friend'

const friends = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const pendingCount = computed(() => friends.value.filter(f => f.status === 0).length)

const form = reactive({
  name: '',
  url: '',
  description: '',
  sortOrder: 0,
  statusBool: true
})

const fetchFriends = async () => {
  const res = await getAdminFriends()
  friends.value = res.data || []
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    form.name = row.name
    form.url = row.url
    form.description = row.description || ''
    form.sortOrder = row.sortOrder || 0
    form.statusBool = row.status === 1
  } else {
    isEdit.value = false
    editId.value = null
    form.name = ''
    form.url = ''
    form.description = ''
    form.sortOrder = 0
    form.statusBool = true
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  const data = {
    name: form.name,
    url: form.url,
    description: form.description,
    sortOrder: form.sortOrder,
    status: form.statusBool ? 1 : 0
  }
  if (isEdit.value) {
    await updateFriend(editId.value, data)
    ElMessage.success('已更新')
  } else {
    await createFriend(data)
    ElMessage.success('已添加')
  }
  dialogVisible.value = false
  fetchFriends()
}

const handleDelete = async (id) => {
  await deleteFriend(id)
  ElMessage.success('已删除')
  fetchFriends()
}

const handleApprove = async (id) => {
  await updateFriendStatus(id, 1)
  ElMessage.success('已通过')
  fetchFriends()
}

const handleReject = async (id) => {
  await updateFriendStatus(id, 2)
  ElMessage.success('已拒绝')
  fetchFriends()
}

onMounted(() => fetchFriends())
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.pending-badge {
  margin-top: 0;
}
</style>
