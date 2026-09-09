<template>
  <div class="profile">
    <van-nav-bar title="我的" />

    <!-- 未登录 -->
    <div v-if="!user" class="guest-header">
      <div class="guest-avatar">
        <svg viewBox="0 0 24 24" width="40" height="40" fill="#fff">
          <path d="M12 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10Zm0 2c-4.42 0-8 2.69-8 6v2h16v-2c0-3.31-3.58-6-8-6Z" />
        </svg>
      </div>
      <div class="guest-text">登录后享受更多服务</div>
      <div class="guest-actions">
        <van-button type="primary" round @click="goLogin">登录</van-button>
        <van-button round plain @click="goRegister">注册</van-button>
      </div>
    </div>

    <!-- 已登录：用户信息头部 -->
    <div v-else class="profile-header">
      <van-image round width="64" height="64" :src="user.avatar || defaultAvatar">
        <template #loading>
          <van-loading />
        </template>
      </van-image>
      <div class="profile-info">
        <h3>{{ user.nickname || user.username }}</h3>
        <p>ID: {{ user.id }}</p>
      </div>
    </div>

    <!-- 菜单：登录后可见 -->
    <template v-if="user">
      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="我的订单" icon="orders-o" is-link @click="goOrders" />
        <van-cell title="我的收藏" icon="star-o" is-link @click="goFavorites" />
        <van-cell title="浏览历史" icon="clock-o" is-link @click="go('history')" />
        <van-cell title="优惠券" icon="coupon-o" is-link @click="go('coupons')" />
      </van-cell-group>
      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="设置" icon="setting-o" is-link @click="go('settings')" />
        <van-cell title="帮助与反馈" icon="question-o" is-link @click="go('help')" />
        <van-cell title="关于我们" icon="info-o" is-link @click="go('about')" />
      </van-cell-group>
      <div style="padding: 24px 16px;">
        <van-button type="danger" block round @click="onLogout">退出登录</van-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getUser } from '../utils/auth'
import { logout } from '../api/auth'

const router = useRouter()
const user = ref(null)
const defaultAvatar = '/avatar.png'

const goLogin = () => router.push('/login')
const goRegister = () => router.push('/register')
const goOrders = () => router.push('/orders')
const goFavorites = () => router.push('/favorites')
const go = (name) => router.push({ name })

const onLogout = async () => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定退出登录吗？' })
    await logout()
    showToast('已退出登录')
    user.value = null
  } catch (e) {
    // 用户取消
  }
}

onMounted(() => {
  user.value = getUser()
})
</script>

<style scoped>
.profile {
  padding-bottom: 20px;
  min-height: 100vh;
  background: #f7f8fa;
}

/* 未登录头部 */
.guest-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 16px 32px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.guest-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.guest-text {
  font-size: 15px;
  margin-bottom: 20px;
  opacity: 0.95;
}

.guest-actions {
  display: flex;
  gap: 12px;
}

.guest-actions .van-button {
  width: 120px;
}

/* 已登录头部 */
.profile-header {
  display: flex;
  align-items: center;
  padding: 24px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.profile-info {
  margin-left: 16px;
}

.profile-info h3 {
  margin: 0;
  font-size: 18px;
}

.profile-info p {
  margin: 4px 0 0;
  font-size: 14px;
  opacity: 0.8;
}
</style>
