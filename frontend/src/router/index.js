import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/ingredients',
    name: 'Ingredients',
    component: () => import('../views/Ingredients.vue'),
    meta: { title: '原材料管理' }
  },
  {
    path: '/dishes',
    name: 'Dishes',
    component: () => import('../views/Dishes.vue'),
    meta: { title: '菜品管理' }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: { title: '订单管理' }
  },
  {
    path: '/pos',
    name: 'POS',
    component: () => import('../views/POS.vue'),
    meta: { title: '前台售卖' }
  },
  {
    path: '/purchases',
    name: 'Purchases',
    component: () => import('../views/Purchases.vue'),
    meta: { title: '采购管理' }
  },
  {
    path: '/wastes',
    name: 'Wastes',
    component: () => import('../views/Wastes.vue'),
    meta: { title: '损耗管理' }
  },
  {
    path: '/settlements',
    name: 'Settlements',
    component: () => import('../views/Settlements.vue'),
    meta: { title: '日结管理' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 餐饮门店管理系统` : '餐饮门店管理系统'
  next()
})

export default router