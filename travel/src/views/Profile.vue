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
    <div v-else class="profile-header" @click="router.push('/profile-edit')">
      <van-image round width="64" height="64" :src="user.avatar || defaultAvatar">
        <template #loading>
          <van-loading />
        </template>
      </van-image>
      <div class="profile-info">
        <h3>{{ user.nickname || user.username }}</h3>
        <p v-if="user.bio">{{ user.bio }}</p>
        <p v-else>点击编辑资料 ›</p>
        <p class="points-line" v-if="user.points !== undefined">
          <span class="points">{{ user.points }} 积分</span>
          <span v-if="user.city" class="city"> · 常居 {{ user.city }}</span>
        </p>
      </div>
    </div>

    <!-- 菜单：登录后可见 -->
    <template v-if="user">
      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="每日签到" icon="points" is-link @click="go('signin')">
          <template #value>
            <span class="cell-hint">赚积分</span>
          </template>
        </van-cell>
        <van-cell title="消息通知" icon="bell" is-link @click="go('notifications')">
          <template #value>
            <span v-if="unread" class="badge">{{ unread > 99 ? '99+' : unread }}</span>
          </template>
        </van-cell>
      </van-cell-group>
      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="我的行程" icon="calendar-o" is-link @click="go('trips')" />
        <van-cell title="我的足迹" icon="location-o" is-link @click="go('footprint')" />
        <van-cell title="旅行记账" icon="gold-coin-o" is-link @click="go('expense')" />
        <van-cell title="打包清单" icon="checked" is-link @click="go('packing')" />
      </van-cell-group>
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
import { getUser, setUser } from '../utils/auth'
import { logout } from '../api/auth'
import { getProfile } from '../api/profile'
import { unreadCount } from '../api/notify'

const router = useRouter()
const user = ref(null)
const unread = ref(0)
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
    unread.value = 0
  } catch (e) {
    // 用户取消
  }
}

onMounted(async () => {
  user.value = getUser()
  if (!user.value) return
  // 拉取最新资料（含积分/签名/常居地）与未读通知数
  try {
    const p = await getProfile()
    user.value = p
    setUser(p)
  } catch (e) {
    // 忽略
  }
  try {
    const res = await unreadCount()
    unread.value = res?.count || 0
  } catch (e) {
    // 忽略
  }
})
</script>

<style scoped>
.profile {
  padding-bottom: 20px;
  min-height: 100vh;
  background: var(--bg);
}

/* 未登录头部 */
.guest-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 16px 32px;
  background: var(--grad-dark);
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
  background: var(--grad-dark);
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

.points-line {
  font-size: 12px !important;
}

.points {
  color: #7ff0cf;
  font-weight: 600;
}

.city {
  opacity: 0.9;
}

.cell-hint {
  font-size: 12px;
  color: var(--success);
}

.badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #d81b3f;
  color: #fff;
  font-size: 11px;
  text-align: center;
}
</style>
