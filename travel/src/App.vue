<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const showTabbar = computed(() => !route.meta.hideTabbar)

// ===== 开屏动画 =====
/** 开屏时长（毫秒） */
const SPLASH_DURATION = 1800
/** 进度条动画时长：留出 200ms 起始延迟，正好在开屏结束时走完 */
const PROGRESS_DURATION = SPLASH_DURATION - 200
const showSplash = ref(false)
let splashTimer = null

// 主标题分两段：前缀常规色，后缀用品牌渐变强调
const titlePrefix = ['智', '能', '旅', '游', '，']
const titleSuffix = ['尽', '在', '爱', '游']

onMounted(() => {
  showSplash.value = true
  splashTimer = setTimeout(() => {
    showSplash.value = false
  }, SPLASH_DURATION)
})

onUnmounted(() => {
  if (splashTimer) clearTimeout(splashTimer)
})

/** 点击“跳过”立即关闭开屏 */
const skipSplash = () => {
  if (splashTimer) {
    clearTimeout(splashTimer)
    splashTimer = null
  }
  showSplash.value = false
}
</script>

<template>
  <div id="app">
    <!-- 开屏动画 -->
    <transition name="splash-fade">
      <div v-if="showSplash" class="splash-overlay">
        <!-- 右上角跳过 -->
        <button class="splash-skip" type="button" aria-label="跳过开屏动画" @click.stop="skipSplash">
          跳过
          <span class="splash-skip__arrow">›</span>
        </button>

        <div class="splash-content">
          <!-- 品牌标识 -->
          <div class="splash-logo">
            <span class="splash-logo__ring"></span>
            <svg viewBox="0 0 24 24" width="40" height="40" fill="currentColor" aria-hidden="true">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z" />
            </svg>
          </div>

          <!-- 主标题：逐字入场 -->
          <h1 class="splash-title">
            <span
              v-for="(ch, i) in titlePrefix"
              :key="'p' + i"
              class="splash-title__char"
              :style="{ '--i': i }"
            >{{ ch }}</span>
            <span
              v-for="(ch, i) in titleSuffix"
              :key="'s' + i"
              class="splash-title__char splash-title__char--accent"
              :style="{ '--i': titlePrefix.length + i }"
            >{{ ch }}</span>
          </h1>

          <!-- 副标题 -->
          <p class="splash-subtitle">智能旅游平台，专业旅游规划</p>

          <!-- 底部进度指示 -->
          <div class="splash-progress"><i :style="{ animationDuration: PROGRESS_DURATION + 'ms' }"></i></div>
        </div>
      </div>
    </transition>

    <div class="page-content">
      <router-view />
    </div>
    <van-tabbar v-if="showTabbar" route fixed placeholder safe-area-inset-bottom>
      <van-tabbar-item to="/" icon="home-o">主页</van-tabbar-item>
      <van-tabbar-item to="/map" icon="location-o">地图</van-tabbar-item>
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

/* ============================================================
 * 开屏动画：浅色薄荷底 + 深青蓝标题 + 逐字入场
 * 配色取自设计参考图（#0C344C 深青蓝 / #44B48C-#4CC494 薄荷青绿）
 * ============================================================ */
.splash-overlay {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 750px;
  bottom: 0;
  z-index: 9999;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 顶部薄荷光晕 → 白色底 */
  background:
    radial-gradient(120% 78% at 50% 14%, #e6f9f1 0%, #f3fbf8 42%, #ffffff 100%);
}

/* 背景装饰：柔和的薄荷光斑 */
.splash-overlay::before,
.splash-overlay::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  filter: blur(38px);
  opacity: 0.55;
  pointer-events: none;
}

.splash-overlay::before {
  width: 240px;
  height: 240px;
  top: -70px;
  left: -60px;
  background: radial-gradient(circle, rgba(76, 196, 148, 0.55) 0%, rgba(76, 196, 148, 0) 70%);
  animation: splashFloat 3.2s ease-in-out infinite;
}

.splash-overlay::after {
  width: 280px;
  height: 280px;
  right: -90px;
  bottom: -110px;
  background: radial-gradient(circle, rgba(12, 52, 76, 0.28) 0%, rgba(12, 52, 76, 0) 70%);
  animation: splashFloat 3.6s ease-in-out infinite reverse;
}

@keyframes splashFloat {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(0, 14px, 0) scale(1.06); }
}

/* ---- 跳过按钮 ---- */
.splash-skip {
  position: absolute;
  top: calc(14px + env(safe-area-inset-top));
  right: 16px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 6px 13px;
  font-size: 13px;
  font-family: inherit;
  font-weight: 600;
  color: #33505f;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(12, 52, 76, 0.14);
  border-radius: 999px;
  backdrop-filter: blur(6px);
  cursor: pointer;
  opacity: 0;
  animation: splashSkipIn 0.32s ease-out 0.3s forwards;
}

.splash-skip:active {
  background: rgba(12, 52, 76, 0.1);
}

.splash-skip__arrow {
  font-size: 15px;
  line-height: 1;
  transform: translateY(-0.5px);
}

@keyframes splashSkipIn {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ---- 内容区 ---- */
.splash-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 32px;
}

/* 品牌标识 */
.splash-logo {
  position: relative;
  width: 84px;
  height: 84px;
  border-radius: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  background: linear-gradient(135deg, #44b48c 0%, #4cc494 100%);
  box-shadow: 0 10px 26px rgba(68, 180, 140, 0.34);
  opacity: 0;
  animation: splashLogoIn 0.5s cubic-bezier(0.22, 1.1, 0.36, 1) 0.08s forwards;
}

.splash-logo__ring {
  position: absolute;
  inset: -10px;
  border-radius: 32px;
  border: 1.5px solid rgba(68, 180, 140, 0.45);
  animation: splashRing 1.8s ease-out infinite;
}

@keyframes splashLogoIn {
  0% { opacity: 0; transform: scale(0.72) translateY(10px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}

@keyframes splashRing {
  0% { transform: scale(0.92); opacity: 0.75; }
  70% { transform: scale(1.16); opacity: 0; }
  100% { transform: scale(1.16); opacity: 0; }
}

/* 主标题 */
.splash-title {
  margin: 22px 0 0;
  font-size: 26px;
  line-height: 1.35;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: #0c344c;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  text-align: center;
}

.splash-title__char {
  display: inline-block;
  opacity: 0;
  transform: translateY(14px) scale(0.94);
  animation: splashCharIn 0.42s cubic-bezier(0.22, 1.2, 0.36, 1) forwards;
  animation-delay: calc(var(--i) * 60ms + 260ms);
}

/* “尽在爱游”用品牌渐变强调 */
.splash-title__char--accent {
  background: linear-gradient(135deg, #44b48c 0%, #4cc494 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

@keyframes splashCharIn {
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* 副标题 */
.splash-subtitle {
  margin: 12px 0 0;
  font-size: 13.5px;
  letter-spacing: 0.06em;
  color: #6b8090;
  opacity: 0;
  animation: splashSubIn 0.45s ease-out 0.78s forwards;
}

@keyframes splashSubIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 底部进度条：与开屏时长同步 */
.splash-progress {
  margin-top: 30px;
  width: 132px;
  height: 3px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(12, 52, 76, 0.1);
  opacity: 0;
  animation: splashSubIn 0.4s ease-out 0.6s forwards;
}

.splash-progress i {
  display: block;
  width: 0;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #44b48c 0%, #4cc494 100%);
  animation-name: splashProgress;
  animation-timing-function: ease-in-out;
  animation-delay: 0.2s;
  animation-fill-mode: forwards;
}

@keyframes splashProgress {
  from { width: 0; }
  to { width: 100%; }
}

/* ---- 淡出过渡 ---- */
.splash-fade-leave-active {
  transition: opacity 0.35s ease;
}

.splash-fade-leave-to {
  opacity: 0;
}

/* ---- 深色主题：深青蓝底 + 薄荷强调 ---- */
html.dark .splash-overlay {
  background: radial-gradient(120% 78% at 50% 14%, #10405a 0%, #0a2a3c 45%, #06202c 100%);
}

html.dark .splash-title {
  color: #eaf6f2;
}

html.dark .splash-subtitle {
  color: #93aab8;
}

html.dark .splash-skip {
  color: #d6e6ee;
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.18);
}

html.dark .splash-progress {
  background: rgba(255, 255, 255, 0.14);
}

/* ---- 无障碍：尊重系统「减少动态效果」 ---- */
@media (prefers-reduced-motion: reduce) {
  .splash-overlay::before,
  .splash-overlay::after,
  .splash-logo__ring {
    animation: none;
  }
  .splash-logo,
  .splash-title__char,
  .splash-subtitle,
  .splash-progress,
  .splash-skip {
    animation-duration: 0.01ms;
    animation-delay: 0ms;
  }
  .splash-progress i {
    animation-duration: 0.01ms;
  }
}
</style>
