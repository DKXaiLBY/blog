<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.not-found {
  text-align: center;
  padding: 120px 24px;
}
.not-found h1 {
  font-size: 72px;
  color: var(--accent);
  margin-bottom: 12px;
}
.not-found p {
  font-size: 16px;
  color: var(--text-muted);
  margin-bottom: 24px;
}
</style>
