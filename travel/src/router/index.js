import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '../utils/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/Chat.vue')
  },
  {
    path: '/map',
    name: 'map',
    component: () => import('../views/MapView.vue')
  },
  {
    path: '/dest/attraction/:id',
    name: 'attraction-detail',
    component: () => import('../views/AttractionDetail.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/footprint',
    name: 'footprint',
    component: () => import('../views/Footprint.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/trips',
    name: 'trips',
    component: () => import('../views/MyTrips.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/trip-detail',
    name: 'trip-detail',
    component: () => import('../views/TripDetail.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/nearby',
    name: 'nearby',
    component: () => import('../views/Nearby.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/dest',
    name: 'dest',
    component: () => import('../views/DestinationLib.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/posts',
    name: 'posts',
    component: () => import('../views/PostList.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/posts/publish',
    name: 'post-publish',
    component: () => import('../views/PostPublish.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/posts/:id',
    name: 'post-detail',
    component: () => import('../views/PostDetail.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/weather',
    name: 'weather',
    component: () => import('../views/Weather.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/expense',
    name: 'expense',
    component: () => import('../views/Expense.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/packing',
    name: 'packing',
    component: () => import('../views/Packing.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/exchange',
    name: 'exchange',
    component: () => import('../views/Exchange.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/collab/:code',
    name: 'collab-join',
    component: () => import('../views/CollabJoin.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/profile-edit',
    name: 'profile-edit',
    component: () => import('../views/ProfileEdit.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/signin',
    name: 'signin',
    component: () => import('../views/SignIn.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'notifications',
    component: () => import('../views/Notifications.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue')
  },
  {
    path: '/plan',
    name: 'PlanResult',
    component: () => import('../views/PlanResult.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { hideTabbar: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/Favorites.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/history',
    name: 'history',
    component: () => import('../views/History.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/coupons',
    name: 'coupons',
    component: () => import('../views/Coupons.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'settings',
    component: () => import('../views/Settings.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/help',
    name: 'help',
    component: () => import('../views/Help.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/About.vue'),
    meta: { hideTabbar: true, requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：需要登录的页面未登录时跳转到登录页
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !isLoggedIn()) {
    next({ path: '/login' })
  } else {
    next()
  }
})

export default router
