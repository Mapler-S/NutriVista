/**
 * Vue Router 4 路由配置
 */

import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据仪表盘', icon: 'DataLine' },
      },
      {
        path: 'meal-logger',
        name: 'MealLogger',
        component: () => import('@/views/MealLogger.vue'),
        meta: { title: '饮食记录', icon: 'Fork' },
      },
      {
        path: 'calendar',
        name: 'MealCalendar',
        component: () => import('@/views/MealCalendar.vue'),
        meta: { title: '饮食日历', icon: 'Calendar' },
      },
      {
        path: 'food-library',
        name: 'FoodLibrary',
        component: () => import('@/views/FoodLibrary.vue'),
        meta: { title: '食物库', icon: 'Grid' },
      },
      {
        path: 'recipes',
        name: 'RecipeBrowser',
        component: () => import('@/views/RecipeBrowser.vue'),
        meta: { title: '菜谱浏览', icon: 'Bowl' },
      },
      {
        path: 'data',
        name: 'DataManagement',
        component: () => import('@/views/DataManagement.vue'),
        meta: { title: '数据管理', icon: 'FolderOpened' },
      },
    ],
  },
  // 404 fallback
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0, behavior: 'smooth' }
  },
})

// 路由守卫：更新页面标题
router.afterEach((to) => {
  const title = to.meta?.title
  document.title = title ? `${title} — NutriVista` : 'NutriVista'
})

export default router
