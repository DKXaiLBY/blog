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

    <el-table :data="articles" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <span class="table-title" @click="goDetail(row.id)">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isTop ? 'danger' : 'success'" size="small">
            {{ row.isTop ? '置顶' : '已发布' }}
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

    <!-- 编辑/新建对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑文章' : '写文章'"
      width="900px"
      top="5vh"
      destroy-on-close
    >
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="文章标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要（可选）" maxlength="300" />
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
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">发布</el-radio>
          </el-radio-group>
          <el-checkbox v-model="form.isTop" style="margin-left:16px" :true-value="1" :false-value="0">置顶</el-checkbox>
        </el-form-item>
        <el-form-item label="内容">
          <v-md-editor v-model="form.content" height="400px" />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticles, getArticleDetail, createArticle, updateArticle, deleteArticle, searchArticles } from '@/api/article'
import { getCategories, getTags } from '@/api/comment'
import { ElMessage } from 'element-plus'

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
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const defaultForm = () => ({
  title: '',
  summary: '',
  content: '',
  categoryId: null,
  tagIds: [],
  status: 1,
  isTop: 0
})
const form = ref(defaultForm())

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const fetchArticles = async () => {
  loading.value = true
  try {
    let res
    if (keyword.value.trim()) {
      res = await searchArticles({ keyword: keyword.value.trim(), page: page.value, size })
    } else {
      res = await getArticles({ page: page.value, size })
    }
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

const openCreate = () => {
  isEdit.value = false
  editingId.value = null
  form.value = defaultForm()
  dialogVisible.value = true
}

const openEdit = async (row) => {
  isEdit.value = true
  editingId.value = row.id
  const res = await getArticleDetail(row.id)
  const detail = res.data
  form.value = {
    title: detail.title,
    summary: detail.summary || '',
    content: detail.content || '',
    categoryId: null,
    tagIds: [],
    status: 1,
    isTop: detail.isTop ?? 0
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
