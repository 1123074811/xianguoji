import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: () => import('../views/ForgotPassword.vue')
    },
    {
      path: '/',
      component: () => import('../layout/DefaultLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue')
        },
        {
          path: 'orders',
          name: 'Orders',
          component: () => import('../views/Orders.vue')
        },
        {
          path: 'orders/:id',
          name: 'OrderDetail',
          component: () => import('../views/OrderDetail.vue')
        },
        {
          path: 'goods',
          name: 'Goods',
          component: () => import('../views/Goods.vue')
        },
        {
          path: 'goods/edit/:id?',
          name: 'GoodsEdit',
          component: () => import('../views/GoodsEdit.vue')
        },
        {
          path: 'campaign',
          name: 'Campaign',
          component: () => import('../views/Campaign.vue')
        },
        {
          path: 'reviews',
          name: 'Reviews',
          component: () => import('../views/Reviews.vue')
        },
        {
          path: 'customers',
          name: 'Customers',
          component: () => import('../views/Customers.vue')
        },
        {
          path: 'shipping',
          name: 'Shipping',
          component: () => import('../views/Shipping.vue')
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('../views/Settings.vue')
        },
        {
          path: 'analysis',
          name: 'BusinessAnalysis',
          component: () => import('../views/BusinessAnalysis.vue')
        },
        {
          path: 'reports',
          name: 'ReportExport',
          component: () => import('../views/ReportExport.vue')
        },
        {
          path: 'messages',
          name: 'Messages',
          component: () => import('../views/Messages.vue')
        },
        {
          path: 'help',
          name: 'HelpCenter',
          component: () => import('../views/HelpCenter.vue')
        }
      ]
    }
  ]
})

export default router