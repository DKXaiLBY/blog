<template>
  <div class="detail-layout">
    <main class="detail-main" v-loading="loading">
      <template v-if="article">
        <h1 class="detail-title">{{ article.title }}</h1>
        <div class="detail-meta">
          <span>{{ fmtDate(article.createdAt) }}</span>
          <span>·</span>
          <span>{{ article.categoryName }}</span>
          <span>·</span>
          <span>{{ article.viewCount }} 阅读</span>
        </div>
        <div v-if="article.tagNames?.length" class="detail-tags">
          <el-tag v-for="t in article.tagNames" :key="t" size="small" effect="dark">{{ t }}</el-tag>
        </div>
        <div class="detail-content">
          <v-md-preview :text="article.content || ''" />
        </div>
      </template>

      <section class="comment-section">
        <h3>评论 ({{ comments.length }})</h3>
        <div v-if="comments.length" class="comment-list">
          <div v-for="c in topLevelComments" :key="c.id" class="comment-item">
            <div class="comment-header">
              <strong>{{ c.authorName }}</strong>
              <span class="comment-time">{{ fmtDate(c.createdAt) }}</span>
            </div>
            <p class="comment-body">{{ c.content }}</p>
            <span class="reply-btn" @click="startReply(c.id, c.authorName)">回复</span>

            <div v-if="c.replies?.length" class="reply-list">
              <div v-for="r in c.replies" :key="r.id" class="comment-item reply-item">
                <div class="comment-header">
                  <strong>{{ r.authorName }}</strong>
                  <span class="comment-time">{{ fmtDate(r.createdAt) }}</span>
                </div>
                <p class="comment-body">{{ r.content }}</p>
                <span class="reply-btn" @click="startReply(c.id, r.authorName)">回复</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评论，来抢沙发吧" />

        <div class="comment-form">
          <el-input v-model="form.authorName" placeholder="昵称" style="margin-bottom:10px" maxlength="20" />
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="3"
            :placeholder="replyHint"
            maxlength="500"
            show-word-limit
          />
          <div class="form-actions">
            <span v-if="form.parentId" class="replying-to">
              回复 @{{ replyToName }}
              <el-button size="small" text @click="cancelReply">取消</el-button>
            </span>
            <el-button type="primary" size="small" @click="submitComment" :loading="submitting">发表评论</el-button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '@/api/article'
import { getComments, addComment } from '@/api/comment'
import { ElMessage } from 'element-plus'

const route = useRoute()
const article = ref(null)
const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const form = ref({ authorName: '', content: '', parentId: null })
const replyToName = ref('')

const topLevelComments = computed(() =>
  comments.value.filter((c) => !c.parentId)
)

const replyHint = computed(() =>
  form.value.parentId ? `回复 @${replyToName.value}` : '写下你的评论...'
)

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const fetchArticle = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
  } catch { /* handled */ }
  finally { loading.value = false }
}

const fetchComments = async () => {
  const res = await getComments(route.params.id)
  comments.value = res.data
}

const startReply = (parentId, name) => {
  form.value.parentId = parentId
  replyToName.value = name
}

const cancelReply = () => {
  form.value.parentId = null
  replyToName.value = ''
}

const submitComment = async () => {
  if (!form.value.authorName.trim() || !form.value.content.trim()) {
    ElMessage.warning('请填写昵称和评论内容')
    return
  }
  submitting.value = true
  try {
    await addComment({
      articleId: Number(route.params.id),
      parentId: form.value.parentId,
      authorName: form.value.authorName.trim(),
      content: form.value.content.trim()
    })
    ElMessage.success('评论成功')
    form.value.content = ''
    form.value.parentId = null
    replyToName.value = ''
    fetchComments()
  } catch { /* handled */ }
  finally { submitting.value = false }
}

onMounted(() => {
  fetchArticle()
  fetchComments()
})
</script>

<style scoped>
.detail-layout {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px;
}

.detail-title {
  font-size: 30px;
  margin-bottom: 12px;
}

.detail-meta {
  font-size: 13px;
  color: var(--text-muted);
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.detail-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 24px;
}

.detail-content {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 32px;
  line-height: 1.8;
}

.comment-section { margin-top: 24px; }
.comment-section h3 { margin-bottom: 16px; }

.comment-item {
  border-bottom: 1px solid var(--border);
  padding: 14px 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.comment-header strong { color: var(--text-primary); font-size: 14px; }
.comment-time { font-size: 12px; color: var(--text-muted); }

.comment-body {
  font-size: 14px;
  margin: 0 0 8px;
}

.reply-btn {
  font-size: 12px;
  color: var(--accent);
  cursor: pointer;
}
.reply-btn:hover { text-decoration: underline; }

.reply-list { margin-left: 28px; }

.comment-form {
  margin-top: 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 16px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.replying-to {
  font-size: 13px;
  color: var(--accent);
}

@media (max-width: 768px) {
  .detail-layout { padding: 16px; }
  .detail-title { font-size: 22px; }
}
</style>
