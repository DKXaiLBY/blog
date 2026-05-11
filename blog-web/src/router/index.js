import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/token'

const routes = [
  { path: '/', name: 'Welcome', component: () => import('@/views/Welcome.vue') },
  { path: '/blog', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/about', redirect: '/resume' },
  { path: '/archive', name: 'Archive', component: () => import('@/views/Archive.vue') },
  { path: '/resume', name: 'Resume', component: () => import('@/views/Resume.vue') },
  { path: '/friends', name: 'Friends', component: () => import('@/views/Friends.vue') },
  { path: '/projects', name: 'Projects', component: () => import('@/views/Projects.vue') },
  { path: '/article/:id', name: 'ArticleDetail', component: () => import('@/views/ArticleDetail.vue') },
  { path: '/search', name: 'Search', component: () => import('@/views/Search.vue') },
  { path: '/tags', name: 'Tags', component: () => import('@/views/Tags.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'articles', name: 'AdminArticles', component: () => import('@/views/admin/ArticleManage.vue') },
      { path: 'comments', name: 'AdminComments', component: () => import('@/views/admin/CommentManage.vue') },
      { path: 'friends', name: 'AdminFriends', component: () => import('@/views/admin/FriendManage.vue') },
      { path: 'profile', name: 'AdminProfile', component: () => import('@/views/admin/ProfileManage.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !getToken()) {
    next('/login')
  } else if (to.path === '/login' && getToken()) {
    next('/admin')
  } else {
    next()
  }
})

export default router
