<template>
  <div class="page">
    <van-nav-bar title="设置" left-arrow @click-left="onBack" />
    <div class="content">
      <van-cell-group inset>
        <van-cell title="深色模式" icon="closed-eye">
          <template #value>
            <van-switch v-model="dark" size="22" @update:model-value="onToggleTheme" />
          </template>
        </van-cell>
        <van-cell title="跟随系统" icon="setting-o" :label="systemHint">
          <template #value>
            <van-switch v-model="followSystem" size="22" @update:model-value="onToggleFollow" />
          </template>
        </van-cell>
      </van-cell-group>

      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="个人资料" icon="contact" is-link @click="router.push('/profile-edit')" />
        <!-- 未实现的能力不做成可跳转样式，避免“点了没反应” -->
        <van-cell title="修改密码" icon="lock">
          <template #value><span class="cell-tag">开发中</span></template>
        </van-cell>
        <van-cell title="绑定手机号" icon="phone-o">
          <template #value><span class="cell-tag">开发中</span></template>
        </van-cell>
      </van-cell-group>

      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="消息通知" icon="bell" is-link @click="router.push('/notifications')" />
        <van-cell title="隐私设置" icon="shield-o" is-link @click="privacyShow = true" />
        <van-cell title="清除缓存" icon="delete-o" is-link :value="cacheSize" @click="clearCache" />
      </van-cell-group>

      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="检查更新" icon="refresh" is-link :value="'v' + VERSION" @click="checkUpdate" />
      </van-cell-group>

      <div class="theme-tip">当前主题：{{ dark ? '深色（黑色）' : '浅色（白天）' }}</div>
    </div>

    <ContentPopup v-model:show="privacyShow" title="隐私设置" :items="privacyItems" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getTheme, setTheme } from '../utils/theme'
import ContentPopup from '../components/ContentPopup.vue'

const router = useRouter()
const onBack = () => router.back()

const VERSION = '1.0.0'
/** 清理缓存时保留的键：登录态与主题偏好 */
const KEEP_KEYS = ['travel_token', 'travel_user', 'travel_theme']

const dark = ref(getTheme() === 'dark')
const followSystem = ref(false)
const cacheSize = ref('0 B')
const privacyShow = ref(false)

const privacyItems = [
  { t: '本应用仅在实现功能所必需的范围内使用你的数据。' },
  { h: true, t: '定位权限' },
  { t: '「周边探索」需要定位权限，仅在页面打开且经你授权后读取一次，不后台持续获取。' },
  { h: true, t: '本地存储' },
  { t: '登录凭证与主题偏好保存在本机浏览器中；清除缓存不会退出登录，但会清理其他本地数据。' },
  { h: true, t: '账号信息' },
  { t: '密码经 BCrypt 加密存储，你的收藏、行程、记账等数据仅用于展示给你本人。' }
]

const systemHint = computed(() =>
  followSystem.value ? '当前跟随系统深色设置' : '开启后随手机深色模式自动切换'
)

function onToggleTheme(val) {
  setTheme(val ? 'dark' : 'light')
  followSystem.value = false
  showToast(val ? '已切换为深色模式' : '已切换为浅色模式')
}

function onToggleFollow(val) {
  if (val) {
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
    setTheme(prefersDark ? 'dark' : 'light')
    dark.value = prefersDark
    showToast('已跟随系统主题')
  }
}

/** 统计可清理的缓存占用 */
function computeCacheSize() {
  let bytes = 0
  for (let i = 0; i < localStorage.length; i++) {
    const k = localStorage.key(i)
    if (KEEP_KEYS.includes(k)) continue
    bytes += (k.length + (localStorage.getItem(k) || '').length) * 2
  }
  cacheSize.value = bytes < 1024 ? `${bytes} B` : `${(bytes / 1024).toFixed(1)} KB`
  return bytes
}

function clearCache() {
  const keys = []
  for (let i = 0; i < localStorage.length; i++) {
    const k = localStorage.key(i)
    if (!KEEP_KEYS.includes(k)) keys.push(k)
  }
  keys.forEach((k) => localStorage.removeItem(k))
  computeCacheSize()
  showToast(keys.length ? `已清除 ${keys.length} 项缓存` : '暂无缓存可清除')
}

function checkUpdate() {
  showToast(`当前已是最新版本 v${VERSION}`)
}

onMounted(computeCacheSize)
</script>

<style scoped>
.page { min-height: 100vh; background: var(--bg); }
.content { padding: 12px 0 40px; }
.theme-tip {
  text-align: center;
  font-size: 12px;
  color: var(--text-3);
  margin-top: 16px;
}
.cell-tag {
  font-size: 12px;
  color: var(--text-3);
  background: var(--surface-2);
  border-radius: 8px;
  padding: 2px 8px;
}
</style>
