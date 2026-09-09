<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const showTabbar = computed(() => !route.meta.hideTabbar)

// 开屏动画
const showSplash = ref(false)

onMounted(() => {
  // 首次进入或刷新页面时显示开屏动画
  showSplash.value = true
  setTimeout(() => {
    showSplash.value = false
  }, 2800)
})
</script>

<template>
  <div id="app">
    <!-- 开屏动画 -->
    <transition name="splash-fade">
      <div v-if="showSplash" class="splash-overlay">
        <div class="splash-content">
          <div class="splash-logo">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z"/>
            </svg>
          </div>
          <div class="splash-text-vertical">
            <span style="--i:0">欢</span>
            <span style="--i:1">迎</span>
            <span style="--i:2">使</span>
            <span style="--i:3">用</span>
            <span style="--i:4">A</span>
            <span style="--i:5">I</span>
            <span style="--i:6">旅</span>
            <span style="--i:7">游</span>
            <span style="--i:8">助</span>
            <span style="--i:9">手</span>
          </div>
        </div>
      </div>
    </transition>

    <div class="page-content">
      <router-view />
    </div>
    <van-tabbar v-if="showTabbar" route fixed placeholder safe-area-inset-bottom>
      <van-tabbar-item to="/" icon="home-o">主页</van-tabbar-item>
      <van-tabbar-item to="/chat" icon="chat-o">对话</van-tabbar-item>
      <van-tabbar-item to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>
.page-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

:deep(.van-tabbar--fixed) {
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 750px;
}

/* ===== 开屏动画 ===== */
.splash-overlay {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 750px;
  bottom: 0;
  z-index: 9999;
  background: linear-gradient(160deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.splash-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.splash-logo {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  animation: logoPulse 1.6s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.1); opacity: 1; }
}

/* 竖排文字 */
.splash-text-vertical {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 4px;
  line-height: 1.6;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.splash-text-vertical span {
  opacity: 0;
  transform: translateY(20px);
  animation: textReveal 0.5s ease forwards;
  animation-delay: calc(var(--i) * 0.15s + 0.3s);
}

@keyframes textReveal {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 淡出过渡 */
.splash-fade-leave-active {
  transition: opacity 0.6s ease;
}

.splash-fade-leave-to {
  opacity: 0;
}
</style>
