<template>
  <div class="admin-layout">
    <div class="admin-header">
      <h2>文章管理</h2>
      <el-button type="primary" @click="openCreate">+ 写文章</el-button>
    </div>

    <div class="admin-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索文章..."
        style="width:240px"
        clearable
        @keyup.enter="fetchArticles"
        @clear="fetchArticles"
      />
      <el-select v-model="statusFilter" placeholder="状态" style="width:120px; margin-left:8px" clearable @change="fetchArticles">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
    </div>

    <!-- Batch actions -->
    <div v-if="selectedIds.length" class="batch-bar">
      <span>已选 {{ selectedIds.length }} 篇</span>
      <el-button size="small" type="success" @click="batchPublish">批量发布</el-button>
      <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
      <el-button size="small" text @click="selectedIds = []">取消选择</el-button>
    </div>

    <el-table :data="articles" stripe v-loading="loading" style="width:100%" @selection-change="onSelectionChange" ref="tableRef">
      <el-table-column type="selection" width="42" />
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <span class="table-title" @click="goDetail(row.id)">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="字数" width="70">
        <template #default="{ row }">
          <span style="font-size:12px;color:var(--text-muted)">{{ wordCount(row.content) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'info' : row.isTop ? 'danger' : 'success'" size="small">
            {{ row.status === 0 ? '草稿' : row.isTop ? '置顶' : '已发布' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除?" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="admin-pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchArticles"
      />
    </div>

    <!-- Edit/Create Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑文章' : '写文章'"
      width="900px"
      top="5vh"
      destroy-on-close
      @opened="onDialogOpened"
      @close="onDialogClosed"
    >
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="文章标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要（可选）" maxlength="300" />
        </el-form-item>
        <el-form-item label="封面">
          <div style="display:flex; gap:8px; width:100%">
            <el-input v-model="form.coverImage" placeholder="封面图片URL（可选）" maxlength="500" style="flex:1" />
            <el-upload
              :show-file-list="false"
              :http-request="handleUpload"
              accept="image/*"
            >
              <el-button :loading="uploading">上传</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" clearable>
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagIds" multiple placeholder="选择标签" clearable>
            <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="系列">
          <el-input v-model="form.series" placeholder="系列名称（可选，同系列文章将自动关联）" maxlength="100" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">发布</el-radio>
          </el-radio-group>
          <el-checkbox v-model="form.isTop" style="margin-left:16px" :true-value="1" :false-value="0">置顶</el-checkbox>
        </el-form-item>
        <el-form-item v-if="form.status === 0" label="定时发布">
          <el-date-picker
            v-model="form.scheduledPublishAt"
            type="datetime"
            placeholder="选择定时发布时间（可选）"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width:280px"
          />
        </el-form-item>
        <el-form-item label="内容">
          <v-md-editor ref="editorRef" v-model="form.content" height="400px" @upload-image="handleEditorUpload" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ isEdit ? '保存修改' : '发布文章' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminArticles, getAdminArticleDetail, createArticle, updateArticle, deleteArticle, batchDeleteArticles, batchUpdateStatus } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAutoSave } from '@/composables/useAutoSave'

const router = useRouter()
const articles = ref([])
const categories = ref([])
const allTags = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const statusFilter = ref(null)
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const editorRef = ref(null)
const tableRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const selectedIds = ref([])

const DRAFT_KEY = 'blog-draft-article'
const draft = useAutoSave(form, DRAFT_KEY, 30000)

const defaultForm = () => ({
  title: '',
  summary: '',
  content: '',
  coverImage: '',
  categoryId: null,
  tagIds: [],
  series: '',
  status: 1,
  isTop: 0,
  scheduledPublishAt: ''
})
const form = ref(defaultForm())

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const wordCount = (content) => {
  if (!content) return 0
  return content.replace(/[#*\-\s`>\[\]()!|~\\n]/g, '').length
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (statusFilter.value !== null && statusFilter.value !== '') params.status = statusFilter.value
    const res = await getAdminArticles(params)
    articles.value = res.data.records
    total.value = res.data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

const fetchMeta = async () => {
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  allTags.value = tagRes.data
}

const goDetail = (id) => router.push(`/article/${id}`)

const onDialogOpened = () => {
  draft.start()
  const onKeyDown = (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault()
      handleSave()
    }
  }
  document.addEventListener('keydown', onKeyDown)
  form.value._keydownCleanup = () => document.removeEventListener('keydown', onKeyDown)
}

const onDialogClosed = () => {
  draft.stop()
  if (form.value._keydownCleanup) {
    form.value._keydownCleanup()
    delete form.value._keydownCleanup
  }
}

const openCreate = () => {
  isEdit.value = false
  editingId.value = null
  form.value = defaultForm()
  dialogVisible.value = true
}

const openEdit = async (row) => {
  isEdit.value = true
  editingId.value = row.id
  const res = await getAdminArticleDetail(row.id)
  const detail = res.data
  form.value = {
    title: detail.title,
    summary: detail.summary || '',
    content: detail.content || '',
    coverImage: detail.coverImage || '',
    categoryId: detail.categoryId ?? null,
    tagIds: detail.tagIds ?? [],
    series: detail.series || '',
    status: detail.status ?? 1,
    isTop: detail.isTop ?? 0,
    scheduledPublishAt: detail.scheduledPublishAt || ''
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateArticle(editingId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await createArticle(form.value)
      ElMessage.success('发布成功')
    }
    draft.clear()
    dialogVisible.value = false
    fetchArticles()
  } catch { /* handled */ }
  finally { saving.value = false }
}

const handleDelete = async (id) => {
  try {
    await deleteArticle(id)
    ElMessage.success('删除成功')
    fetchArticles()
  } catch { /* handled */ }
}

const onSelectionChange = (rows) => {
  selectedIds.value = rows.map(r => r.id)
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 篇文章?`, '批量删除', { type: 'warning' })
    await batchDeleteArticles(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchArticles()
  } catch { /* cancelled or error */ }
}

const batchPublish = async () => {
  try {
    await batchUpdateStatus(selectedIds.value, 1)
    ElMessage.success('批量发布成功')
    selectedIds.value = []
    fetchArticles()
  } catch { /* handled */ }
}

const handleUpload = async (option) => {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', option.file)
    const res = await request.post('/admin/upload', formData)
    if (res.data?.url) {
      form.value.coverImage = res.data.url
      ElMessage.success('上传成功')
    }
  } catch {
    ElMessage.error('上传失败')
  }
  finally { uploading.value = false }
}

const handleEditorUpload = async (event, insertImage, files) => {
  const file = files instanceof File ? files : (files?.[0] || event?.clipboardData?.files?.[0])
  if (!file) return
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await request.post('/admin/upload', formData)
    if (res.data?.url) {
      insertImage({ url: res.data.url })
    }
  } catch {
    ElMessage.error('图片上传失败')
  }
}

watch(dialogVisible, (val) => {
  if (val && !isEdit.value && draft.restore()) {
    ElMessageBox.confirm('检测到未保存的草稿，是否恢复?', '恢复草稿', {
      confirmButtonText: '恢复',
      cancelButtonText: '忽略',
      type: 'info'
    }).catch(() => {
      draft.clear()
      form.value = defaultForm()
    })
  }
})

onMounted(() => {
  fetchArticles()
  fetchMeta()
})

</script>

<style scoped>
.admin-layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.admin-toolbar {
  display: flex;
  margin-bottom: 16px;
}

.batch-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: var(--accent-bg);
  border: 1px solid var(--accent);
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--accent);
}

.table-title {
  color: var(--accent);
  cursor: pointer;
}
.table-title:hover { text-decoration: underline; }

.admin-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .admin-layout { padding: 16px; }
  .admin-toolbar { flex-wrap: wrap; gap: 8px; }
}
</style>
