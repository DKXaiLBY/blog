<template>
  <div class="admin-layout">
    <div class="admin-header">
      <div class="admin-tabs">
        <router-link to="/admin/dashboard" class="admin-tab" active-class="active">仪表盘</router-link>
        <router-link to="/admin/articles" class="admin-tab" active-class="active">文章管理</router-link>
        <router-link to="/admin/comments" class="admin-tab" active-class="active">评论管理</router-link>
        <router-link to="/admin/friends" class="admin-tab" active-class="active">友链管理</router-link>
        <router-link to="/admin/profile" class="admin-tab" active-class="active">个人设置</router-link>
      </div>
    </div>
    <router-view />
  </div>
</template>

<script setup>
</script>

<style scoped>
.admin-layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.admin-header {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 2px solid var(--border);
}

.admin-tabs {
  display: flex;
  gap: 0;
}

.admin-tab {
  padding: 10px 20px;
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: color 0.2s, border-color 0.2s;
}
.admin-tab:hover { color: var(--text-primary); }
.admin-tab.active {
  color: var(--accent);
  border-bottom-color: var(--accent);
}

@media (max-width: 768px) {
  .admin-layout { padding: 16px; }
  .admin-tab { padding: 8px 14px; font-size: 13px; }
}
</style>
