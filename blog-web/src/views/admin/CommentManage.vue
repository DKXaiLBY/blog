<template>
  <div>
    <div class="toolbar">
      <el-radio-group v-model="statusFilter" @change="onFilterChange" size="small">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已通过</el-radio-button>
        <el-radio-button :value="2">已拒绝</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="articleIdFilter"
        placeholder="按文章ID筛选（可选）"
        style="width:200px"
        clearable
        @keyup.enter="fetchComments"
        @clear="fetchComments"
      />
      <el-button @click="fetchComments">查询</el-button>
    </div>

    <el-table :data="comments" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="articleTitle" label="所属文章" min-width="160">
        <template #default="{ row }">
          <span class="link" @click="goArticle(row.articleId)">{{ row.articleTitle }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="authorName" label="评论者" width="100" />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="warning" size="small">待审</el-tag>
          <el-tag v-else-if="row.status === 1" type="success" size="small">通过</el-tag>
          <el-tag v-else-if="row.status === 2" type="danger" size="small">拒绝</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="150">
        <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" text type="success" @click="handleApprove(row.id)">通过</el-button>
            <el-button size="small" text type="danger" @click="handleReject(row.id)">拒绝</el-button>
          </template>
          <el-button size="small" text type="warning" v-if="row.status === 2" @click="handleApprove(row.id)">重新通过</el-button>
          <el-popconfirm title="确定删除?" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchComments"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminComments, deleteComment, updateCommentStatus } from '@/api/comment'
import { ElMessage } from 'element-plus'

const router = useRouter()
const comments = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const articleIdFilter = ref('')
const statusFilter = ref(null)
const loading = ref(false)

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const fetchComments = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (articleIdFilter.value.trim()) params.articleId = Number(articleIdFilter.value.trim())
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await getAdminComments(params)
    comments.value = res.data.records
    total.value = res.data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

const onFilterChange = () => {
  page.value = 1
  fetchComments()
}

const handleApprove = async (id) => {
  try {
    await updateCommentStatus(id, 1)
    ElMessage.success('已通过')
    fetchComments()
  } catch { /* handled */ }
}

const handleReject = async (id) => {
  try {
    await updateCommentStatus(id, 2)
    ElMessage.success('已拒绝')
    fetchComments()
  } catch { /* handled */ }
}

const handleDelete = async (id) => {
  try {
    await deleteComment(id)
    ElMessage.success('删除成功')
    fetchComments()
  } catch { /* handled */ }
}

const goArticle = (id) => router.push(`/article/${id}`)

onMounted(fetchComments)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.link {
  color: var(--accent);
  cursor: pointer;
}
.link:hover { text-decoration: underline; }
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
