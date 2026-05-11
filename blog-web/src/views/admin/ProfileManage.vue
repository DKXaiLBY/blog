<template>
  <div class="profile-manage" v-fade-in>
    <h2 class="page-title">个人设置</h2>

    <!-- Avatar + Logout -->
    <div class="section">
      <div class="avatar-row">
        <div class="avatar-left">
          <el-avatar :size="72" :src="form.avatar" class="avatar-preview">
            {{ form.nickname?.charAt(0) || 'A' }}
          </el-avatar>
          <div class="avatar-actions">
            <el-button size="small" @click="fileInput?.click()">选择图片</el-button>
            <el-button size="small" text @click="form.avatar = ''">移除头像</el-button>
            <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleUpload" />
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          退出登录
        </button>
      </div>
    </div>

    <!-- Basic Info -->
    <div class="section">
      <h3>基本信息</h3>
      <div class="form-row">
        <label>昵称</label>
        <el-input v-model="form.nickname" maxlength="20" placeholder="输入昵称" />
      </div>
      <div class="form-row">
        <label>一句话简介</label>
        <el-input v-model="form.tagline" maxlength="100" placeholder="简短的一句话，显示在关于页昵称下方" />
      </div>
      <div class="form-row">
        <label>个人介绍</label>
        <el-input v-model="form.bio" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="写一段关于你的介绍..." />
      </div>
      <div class="form-row">
        <label>全站公告</label>
        <el-input v-model="form.announcement" placeholder="留空则不显示公告栏" maxlength="200" show-word-limit />
      </div>
      <el-button type="primary" :loading="saving" @click="saveProfile">保存设置</el-button>
    </div>

    <!-- Skills (collapsible) -->
    <div class="section collapsible" :class="{ collapsed: !showSkills }">
      <h3 class="section-toggle" @click="showSkills = !showSkills">
        技能列表
        <span class="toggle-arrow">{{ showSkills ? '▴' : '▾' }}</span>
      </h3>
      <div class="collapse-body" v-show="showSkills">
        <div class="item-list">
          <div v-for="(skill, i) in skills" :key="i" class="item-row">
            <el-input v-model="skill.name" placeholder="技能名" size="small" style="width:180px" />
            <el-input v-model="skill.color" placeholder="颜色代码" size="small" style="width:120px" />
            <span class="color-dot" :style="{ background: skill.color || '#ccc' }"></span>
            <el-button size="small" text type="danger" @click="skills.splice(i, 1)">删除</el-button>
          </div>
          <el-button size="small" @click="skills.push({ name: '', color: 'var(--accent)' })">+ 添加技能</el-button>
        </div>
      </div>
    </div>

    <!-- Contacts (collapsible) -->
    <div class="section collapsible" :class="{ collapsed: !showContacts }">
      <h3 class="section-toggle" @click="showContacts = !showContacts">
        联系方式
        <span class="toggle-arrow">{{ showContacts ? '▴' : '▾' }}</span>
      </h3>
      <div class="collapse-body" v-show="showContacts">
        <div class="item-list">
          <div v-for="(c, i) in contacts" :key="i" class="item-row">
            <el-input v-model="c.label" placeholder="名称" size="small" style="width:120px" />
            <el-select v-model="c.icon" size="small" style="width:100px">
              <el-option label="GitHub" value="github" />
              <el-option label="邮箱" value="email" />
              <el-option label="RSS" value="rss" />
            </el-select>
            <el-input v-model="c.href" placeholder="链接" size="small" style="width:200px" />
            <el-button size="small" text type="danger" @click="contacts.splice(i, 1)">删除</el-button>
          </div>
          <el-button size="small" @click="contacts.push({ label: '', icon: 'github', href: '' })">+ 添加联系方式</el-button>
        </div>
      </div>
    </div>

    <!-- Projects (collapsible) -->
    <div class="section collapsible" :class="{ collapsed: !showProjects }">
      <h3 class="section-toggle" @click="showProjects = !showProjects">
        项目列表
        <span class="toggle-arrow">{{ showProjects ? '▴' : '▾' }}</span>
      </h3>
      <div class="collapse-body" v-show="showProjects">
        <p class="section-hint">管理展示在"项目"页面的项目列表</p>
        <div class="projects-editor">
          <div v-for="(p, i) in projects" :key="i" class="project-edit-card">
            <div class="form-row">
              <label>项目名称</label>
              <el-input v-model="p.name" placeholder="项目名称" />
            </div>
            <div class="form-row">
              <label>项目链接</label>
              <el-input v-model="p.url" placeholder="https://github.com/..." />
            </div>
            <div class="form-row">
              <label>简介</label>
              <el-input v-model="p.description" placeholder="一句话描述" />
            </div>
            <div class="form-row">
              <label>标签</label>
              <el-input v-model="p.tagsStr" placeholder="逗号分隔，如 Vue,Java" @change="parseTags(i)" />
            </div>
            <div class="form-row">
              <label>Stars</label>
              <el-input-number v-model="p.stars" :min="0" size="small" />
            </div>
            <el-button size="small" text type="danger" @click="projects.splice(i, 1)">删除</el-button>
          </div>
          <el-button size="small" @click="addProject">+ 添加项目</el-button>
        </div>
      </div>
    </div>

    <!-- Password (collapsible) -->
    <div class="section collapsible" :class="{ collapsed: !showPassword }">
      <h3 class="section-toggle" @click="showPassword = !showPassword">
        修改密码
        <span class="toggle-arrow">{{ showPassword ? '▴' : '▾' }}</span>
      </h3>
      <div class="collapse-body" v-show="showPassword">
        <div class="form-row">
          <label>原密码</label>
          <el-input v-model="pwForm.oldPassword" type="password" show-password placeholder="输入原密码" />
        </div>
        <div class="form-row">
          <label>新密码</label>
          <el-input v-model="pwForm.newPassword" type="password" show-password placeholder="输入新密码（至少6位）" />
        </div>
        <div class="form-row">
          <label>确认密码</label>
          <el-input v-model="pwForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </div>
        <el-button type="warning" :loading="changingPw" @click="savePassword">修改密码</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUserProfile, updateAvatar, updateProfile, changePassword } from '@/api/user'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const fileInput = ref(null)
const saving = ref(false)
const changingPw = ref(false)

const form = ref({ nickname: '', avatar: '', tagline: '', bio: '', announcement: '' })
const skills = ref([])
const contacts = ref([])
const projects = ref([])
const showProjects = ref(false)

const showSkills = ref(false)
const showContacts = ref(false)
const showPassword = ref(false)

const pwForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

onMounted(async () => {
  try {
    const res = await getUserProfile()
    if (res.data) {
      form.value.nickname = res.data.nickname || ''
      form.value.avatar = res.data.avatar || ''
      form.value.tagline = res.data.tagline || ''
      form.value.bio = res.data.bio || ''
      form.value.announcement = res.data.announcement || ''
      try { skills.value = JSON.parse(res.data.skills || '[]') } catch { skills.value = [] }
      try { contacts.value = JSON.parse(res.data.contacts || '[]') } catch { contacts.value = [] }
      try {
        const rawProjects = JSON.parse(res.data.projects || '[]')
        projects.value = rawProjects.map(p => ({ ...p, tagsStr: (p.tags || []).join(', ') }))
      } catch { projects.value = [] }
    }
  } catch { /* ignore */ }
})

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

const handleUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await request.post('/admin/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200) {
      const url = res.data.data.url
      form.value.avatar = url
      await updateAvatar({ avatar: url })
      ElMessage.success('头像已更新')
    }
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}

const saveProfile = async () => {
  saving.value = true
  try {
    await updateProfile({
      nickname: form.value.nickname,
      tagline: form.value.tagline,
      bio: form.value.bio,
      skills: JSON.stringify(skills.value),
      contacts: JSON.stringify(contacts.value),
      projects: JSON.stringify(projects.value.map(p => {
        const { tagsStr, ...rest } = p
        return { ...rest, tags: tagsStr ? tagsStr.split(',').map(s => s.trim()).filter(Boolean) : [] }
      })),
      announcement: form.value.announcement
    })
    ElMessage.success('设置已保存')
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

const addProject = () => {
  projects.value.push({ name: '', url: '', description: '', tagsStr: '', stars: 0 })
}

const savePassword = async () => {
  if (!pwForm.value.oldPassword || !pwForm.value.newPassword) {
    ElMessage.warning('请填写密码')
    return
  }
  if (pwForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (pwForm.value.newPassword !== pwForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  changingPw.value = true
  try {
    await changePassword({
      oldPassword: pwForm.value.oldPassword,
      newPassword: pwForm.value.newPassword
    })
    ElMessage.success('密码已修改，请重新登录')
    pwForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    showPassword.value = false
  } catch { ElMessage.error('修改失败，请检查原密码') }
  finally { changingPw.value = false }
}
</script>

<style scoped>
.page-title {
  font-size: 22px;
  margin-bottom: 24px;
}

.section {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 16px;
}
.section h3 {
  font-size: 15px;
  margin-bottom: 14px;
}

/* Avatar row */
.avatar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.avatar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.avatar-preview {
  border: 3px solid var(--accent);
}
.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg-primary);
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.logout-btn:hover {
  border-color: #ef4444;
  color: #ef4444;
  background: rgba(239,68,68,0.06);
}

/* Form rows */
.form-row {
  margin-bottom: 14px;
}
.form-row label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

/* Collapsible sections */
.collapsible .section-toggle {
  cursor: pointer;
  user-select: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
  transition: margin 0.25s;
}
.collapsible.collapsed .section-toggle {
  margin-bottom: 0;
}
.collapsible:not(.collapsed) .section-toggle {
  margin-bottom: 14px;
}
.toggle-arrow {
  font-size: 12px;
  color: var(--text-muted);
  transition: transform 0.2s;
}
.collapse-body {
  overflow: hidden;
}

/* Item list */
.item-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.color-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* Project editor */
.section-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0 0 14px;
}
.project-edit-card {
  background: var(--bg-primary);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.projects-editor {
  display: flex;
  flex-direction: column;
}

@media (max-width: 640px) {
  .avatar-row {
    flex-direction: column;
    gap: 14px;
    align-items: flex-start;
  }
  .item-row {
    flex-wrap: wrap;
  }
}
</style>
