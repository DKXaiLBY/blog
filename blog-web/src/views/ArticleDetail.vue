<template>
  <div class="detail-layout">
    <main class="detail-main" v-loading="loading">
      <template v-if="article">
        <a class="back-link" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </a>
        <h1 class="detail-title">{{ article.title }}</h1>
        <div class="detail-meta">
          <span>{{ fmtDate(article.createdAt) }}</span>
          <span>·</span>
          <span>{{ article.categoryName }}</span>
          <span>·</span>
          <span>{{ article.viewCount }} 阅读</span>
          <template v-if="article.authorName">
            <span>·</span>
            <span>{{ article.authorName }}</span>
          </template>
        </div>
        <div v-if="article.tagNames?.length" class="detail-tags">
          <el-tag v-for="t in article.tagNames" :key="t" size="small" effect="dark">{{ t }}</el-tag>
        </div>

        <!-- Like & Bookmark -->
        <div class="detail-actions">
          <button class="like-btn" :class="{ liked: liked }" @click="handleLike">
            <el-icon :size="18"><Star /></el-icon>
            <span>{{ article.likeCount || 0 }} 赞</span>
          </button>
          <button class="like-btn bookmark-btn" :class="{ liked: bookmarked }" @click="handleBookmark">
            <el-icon :size="18"><Collection /></el-icon>
            <span>{{ bookmarked ? '已收藏' : '收藏' }}</span>
          </button>
        </div>

        <div class="detail-content" v-fade-in v-code-copy v-lazy-img @click="onContentClick">
          <v-md-preview :text="article.content || ''" />
        </div>

        <!-- Series Navigation -->
        <div v-if="article.series && article.seriesArticles?.length" class="series-box">
          <div class="series-header">
            <el-icon :size="16"><Collection /></el-icon>
            <span>系列合集：<strong>{{ article.series }}</strong></span>
            <span class="series-progress">（第 {{ article.seriesIndex }} 篇 / 共 {{ article.seriesArticles.length }} 篇）</span>
          </div>
          <div class="series-timeline">
            <div
              v-for="(s, si) in article.seriesArticles"
              :key="s.id"
              class="series-item"
              :class="{ current: s.id === article.id }"
              @click="s.id !== article.id && $router.push(`/article/${s.id}`)"
            >
              <span class="series-dot"></span>
              <span class="series-item-title">{{ s.title }}</span>
              <span v-if="s.id === article.id" class="series-current-tag">当前</span>
            </div>
          </div>
        </div>

        <!-- Prev / Next -->
        <div class="prev-next">
          <div v-if="prevNext.prev" class="prev-next-item" @click="$router.push(`/article/${prevNext.prev.id}`)">
            <span class="pn-label">← 上一篇</span>
            <span class="pn-title">{{ prevNext.prev.title }}</span>
          </div>
          <div v-else class="prev-next-item disabled">
            <span class="pn-label">← 上一篇</span>
            <span class="pn-title">没有了</span>
          </div>
          <div v-if="prevNext.next" class="prev-next-item next" @click="$router.push(`/article/${prevNext.next.id}`)">
            <span class="pn-label">下一篇 →</span>
            <span class="pn-title">{{ prevNext.next.title }}</span>
          </div>
          <div v-else class="prev-next-item next disabled">
            <span class="pn-label">下一篇 →</span>
            <span class="pn-title">没有了</span>
          </div>
        </div>
      </template>

      <!-- Related Articles -->
      <section v-if="relatedArticles.length" class="related-section">
        <h3 class="related-heading">相关推荐</h3>
        <div class="related-grid">
          <div
            v-for="item in relatedArticles"
            :key="item.id"
            class="related-card"
            @click="$router.push(`/article/${item.id}`)"
          >
            <div class="related-cover" v-if="item.coverImage">
              <img :src="item.coverImage" alt="" loading="lazy" />
            </div>
            <div class="related-body">
              <h4 class="related-title">{{ item.title }}</h4>
              <span class="related-date">{{ fmtDate(item.createdAt) }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="comment-section">
        <h3>评论 ({{ comments.length }})</h3>
        <div v-if="comments.length" class="comment-list">
          <div v-for="c in topLevelComments" :key="c.id" class="comment-item">
            <div class="comment-header">
              <strong>{{ c.authorName }}</strong>
              <span class="comment-time">{{ fmtDate(c.createdAt) }}</span>
            </div>
            <p class="comment-body" v-html="renderEmoji(c.content)"></p>
            <span class="reply-btn" @click="startReply(c.id, c.authorName)">回复</span>

            <div v-if="c.replies?.length" class="reply-list">
              <div v-for="r in c.replies" :key="r.id" class="comment-item reply-item">
                <div class="comment-header">
                  <strong>{{ r.authorName }}</strong>
                  <span class="comment-time">{{ fmtDate(r.createdAt) }}</span>
                </div>
                <p class="comment-body" v-html="renderEmoji(r.content)"></p>
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
          <div class="captcha-row">
            <span class="captcha-question">{{ captchaQuestion }}</span>
            <el-input v-model="captchaAnswer" placeholder="答案" style="width:80px" size="small" />
            <el-button size="small" text @click="fetchCaptcha" :disabled="submitting">换一个</el-button>
          </div>
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

    <aside class="detail-sidebar">
      <TocAside />
    </aside>
  </div>

  <Lightbox ref="lightboxRef" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Star, Collection } from '@element-plus/icons-vue'
import { getArticleDetail, likeArticle, getPrevNext, getRelatedArticles } from '@/api/article'
import { getComments, addComment, getCaptcha } from '@/api/comment'
import { ElMessage } from 'element-plus'
import { useSEO } from '@/composables/useSEO'
import { useReadingHistory } from '@/composables/useReadingHistory'
import { useBookmarks } from '@/composables/useBookmarks'
import TocAside from '@/components/TocAside.vue'
import Lightbox from '@/components/Lightbox.vue'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const prevNext = ref({ prev: null, next: null })
const relatedArticles = ref([])
const { isBookmarked, toggleBookmark } = useBookmarks()
const liked = ref(false)
const bookmarked = ref(false)

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/blog')
  }
}
const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const form = ref({ authorName: '', content: '', parentId: null })
const replyToName = ref('')
const lightboxRef = ref(null)
const captchaKey = ref('')
const captchaQuestion = ref('')
const captchaAnswer = ref('')

const fetchCaptcha = async () => {
  try {
    const res = await getCaptcha()
    captchaKey.value = res.data.captchaKey
    captchaQuestion.value = res.data.question
    captchaAnswer.value = ''
  } catch { /* ignore */ }
}

const topLevelComments = computed(() =>
  comments.value.filter((c) => !c.parentId)
)

const replyHint = computed(() =>
  form.value.parentId ? `回复 @${replyToName.value}` : '写下你的评论...'
)

import { useEmoji } from '@/composables/useEmoji'
const { render: renderEmoji } = useEmoji()

const fmtDate = (s) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}

const { addArticle: recordHistory } = useReadingHistory()

const fetchArticle = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
    if (article.value) {
      useSEO({ title: article.value.title, description: article.value.summary })
      recordHistory({ id: article.value.id, title: article.value.title, coverImage: article.value.coverImage })
    }
    liked.value = sessionStorage.getItem(`liked-${route.params.id}`) === '1'
    bookmarked.value = isBookmarked(Number(route.params.id))
  } catch { /* handled */ }
  finally { loading.value = false }
}

const fetchPrevNext = async () => {
  try {
    const res = await getPrevNext(route.params.id)
    prevNext.value = res.data || { prev: null, next: null }
  } catch { /* ignore */ }
}

const fetchRelated = async () => {
  try {
    const res = await getRelatedArticles(route.params.id, 3)
    relatedArticles.value = res.data || []
  } catch { /* ignore */ }
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
      content: form.value.content.trim(),
      captchaKey: captchaKey.value,
      captchaAnswer: Number(captchaAnswer.value)
    })
    ElMessage.success('评论成功')
    form.value.content = ''
    form.value.parentId = null
    replyToName.value = ''
    fetchComments()
  } catch { /* handled */ }
  finally { submitting.value = false }
}

const handleLike = async () => {
  if (liked.value) return
  try {
    await likeArticle(route.params.id)
    if (article.value) {
      article.value.likeCount = (article.value.likeCount || 0) + 1
    }
    liked.value = true
    sessionStorage.setItem(`liked-${route.params.id}`, '1')
    ElMessage.success('已点赞')
  } catch { /* ignore */ }
}

const handleBookmark = () => {
  if (!article.value) return
  const result = toggleBookmark({
    id: article.value.id,
    title: article.value.title
  })
  bookmarked.value = result
  ElMessage.success(result ? '已收藏' : '已取消收藏')
}

const onContentClick = (e) => {
  const img = e.target.closest('img')
  if (!img || !img.src) return
  lightboxRef.value?.open(img.src, img.alt || '')
}

onMounted(() => {
  fetchArticle()
  fetchComments()
  fetchPrevNext()
  fetchRelated()
  fetchCaptcha()
})
</script>

<style scoped>
.detail-layout {
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  gap: 24px;
}

.detail-main {
  flex: 1;
  min-width: 0;
}

.detail-sidebar {
  width: 240px;
  flex-shrink: 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-muted);
  cursor: pointer;
  margin-bottom: 12px;
  transition: color 0.2s;
  user-select: none;
}
.back-link:hover { color: var(--accent); }

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
  margin-bottom: 16px;
}

.detail-actions {
  margin-bottom: 20px;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 20px;
  border: 1px solid var(--border);
  background: var(--bg-secondary);
  color: var(--text-muted);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.25s;
}
.like-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  transform: scale(1.05);
}
.like-btn.liked {
  background: var(--accent-bg);
  border-color: var(--accent);
  color: var(--accent);
  animation: likePop 0.4s ease;
}

@keyframes likePop {
  0% { transform: scale(1); }
  40% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.detail-content {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 24px;
  margin-bottom: 32px;
  line-height: 1.8;
}
.detail-content img {
  max-width: 100%;
  border-radius: 6px;
  cursor: zoom-in;
  transition: opacity 0.3s;
}

/* Series */
.series-box {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 24px;
}
.series-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 14px;
}
.series-header strong {
  color: var(--accent);
}
.series-progress {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: auto;
}
.series-timeline {
  position: relative;
  padding-left: 20px;
}
.series-timeline::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: var(--border);
}
.series-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
}
.series-item.current {
  cursor: default;
}
.series-item:not(.current):hover {
  color: var(--accent);
}
.series-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--border);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  transition: background 0.2s;
}
.series-item.current .series-dot {
  background: var(--accent);
}
.series-item-title {
  font-size: 14px;
  color: var(--text-primary);
}
.series-current-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  background: var(--accent-bg);
  color: var(--accent);
  margin-left: auto;
}

/* Prev / Next */
.prev-next {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}
.prev-next-item {
  flex: 1;
  padding: 16px 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.2s, transform 0.2s;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.prev-next-item:hover {
  border-color: var(--accent);
  transform: translateY(-2px);
}
.prev-next-item.disabled {
  cursor: default;
  opacity: 0.5;
}
.prev-next-item.disabled:hover {
  border-color: var(--border);
  transform: none;
}
.prev-next-item.next {
  text-align: right;
}
.pn-label {
  font-size: 12px;
  color: var(--text-muted);
}
.pn-title {
  font-size: 14px;
  color: var(--text-primary);
}

/* Related Articles */
.related-section {
  margin-bottom: 32px;
}
.related-heading {
  font-size: 17px;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 3px solid var(--accent);
}
.related-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.related-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s, transform 0.2s;
}
.related-card:hover {
  border-color: var(--accent);
  transform: translateY(-2px);
}
.related-cover {
  width: 100%;
  height: 120px;
  overflow: hidden;
  background: var(--bg-tertiary);
}
.related-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.related-body {
  padding: 12px 14px;
}
.related-title {
  font-size: 14px;
  color: var(--text-primary);
  margin: 0 0 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}
.related-date {
  font-size: 12px;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .related-grid { grid-template-columns: 1fr; }
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

.captcha-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}
.captcha-question {
  font-family: monospace;
  font-size: 16px;
  font-weight: bold;
  color: var(--text-primary);
  background: var(--bg-primary);
  padding: 2px 10px;
  border-radius: 4px;
  user-select: none;
}

@media (max-width: 900px) {
  .detail-sidebar { display: none; }
}
@media (max-width: 768px) {
  .detail-layout { padding: 16px; }
  .detail-title { font-size: 22px; }
  .prev-next { flex-direction: column; }
}
</style>
