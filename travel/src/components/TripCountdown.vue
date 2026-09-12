<script setup>
/**
 * 首页行程概览卡片：距最近一次出行倒计时 + 最近行程摘要
 * 依赖已保存行程（trip.startDate），未登录/无行程时不渲染
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from 'vant'
import { listTrips } from '../api/trip'
import { isLoggedIn } from '../utils/auth'

const router = useRouter()

const trips = ref([])
const loaded = ref(false)

onMounted(async () => {
  if (!isLoggedIn()) {
    loaded.value = true
    return
  }
  try {
    trips.value = await listTrips()
  } catch (e) {
    // 忽略
  } finally {
    loaded.value = true
  }
})

/** 有出发日期的行程优先，其次按创建时间（后端已倒序） */
const upcoming = computed(() => {
  const withDate = trips.value
    .filter((t) => t.startDate)
    .map((t) => ({ ...t, ts: new Date(t.startDate + 'T00:00:00').getTime() }))
    .filter((t) => !Number.isNaN(t.ts))
    .sort((a, b) => a.ts - b.ts)
  const today = new Date().setHours(0, 0, 0, 0)
  const future = withDate.filter((t) => t.ts >= today)
  if (future.length) return future[0]
  if (withDate.length) return withDate[withDate.length - 1]
  return trips.value[0] || null
})

const daysLeft = computed(() => {
  if (!upcoming.value?.startDate) return null
  const diff = new Date(upcoming.value.startDate + 'T00:00:00').setHours(0, 0, 0, 0) - new Date().setHours(0, 0, 0, 0)
  return Math.round(diff / 86400000)
})

const countdownText = computed(() => {
  const d = daysLeft.value
  if (d === null) return null
  if (d > 0) return `还有 ${d} 天`
  if (d === 0) return '就是今天出发'
  return `已出发 ${Math.abs(d)} 天`
})

const hasTrips = computed(() => trips.value.length > 0)

function goTrips() {
  router.push('/trips')
}

function goDetail() {
  if (upcoming.value?.id) {
    router.push({ path: '/trip-detail', query: { id: upcoming.value.id } })
  } else {
    goTrips()
  }
}
</script>

<template>
  <!-- 未登录：提示登录后展示行程概览 -->
  <div v-if="loaded && !isLoggedIn()" class="trip-widget trip-widget--hint" @click="router.push('/login')">
    <Icon name="calendar-o" size="20" color="#0c0c0c" />
    <span>登录后可查看行程倒计时与行程卡片</span>
    <Icon name="arrow" size="14" color="#5a6165" />
  </div>

  <div
    v-else-if="loaded && hasTrips"
    class="trip-widget"
    @click="goDetail"
  >
    <div class="trip-widget__left">
      <div class="trip-widget__dest">
        <Icon name="location-o" /> {{ upcoming.destination }}
      </div>
      <div class="trip-widget__meta">{{ upcoming.days }}天 · 预算 ¥{{ upcoming.budget }}</div>
      <div class="trip-widget__count" v-if="countdownText">{{ countdownText }}</div>
      <div class="trip-widget__count trip-widget__count--muted" v-else>未设置出发日期</div>
    </div>
    <div class="trip-widget__right">
      <template v-if="countdownText">
        <div class="cd-num" v-if="daysLeft > 0">{{ daysLeft }}</div>
        <div class="cd-unit" v-if="daysLeft > 0">天后出发</div>
        <div class="cd-unit" v-else>{{ countdownText }}</div>
      </template>
      <van-button size="mini" round plain type="primary" @click.stop="goTrips">全部行程</van-button>
    </div>
  </div>
</template>

<style scoped>
.trip-widget {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #fbf7e6, #fbf7e6);
  border: 1px solid #efe9d0;
}

.trip-widget--hint {
  font-size: 13px;
  color: var(--text-2);
  justify-content: flex-start;
  gap: 8px;
}

.trip-widget__dest {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
  display: flex;
  align-items: center;
  gap: 4px;
}

.trip-widget__meta {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 4px;
}

.trip-widget__count {
  font-size: 12px;
  color: var(--brand-deep);
  font-weight: 600;
  margin-top: 6px;
}

.trip-widget__count--muted {
  color: var(--text-2);
  font-weight: 400;
}

.trip-widget__right {
  text-align: center;
  flex-shrink: 0;
}

.cd-num {
  font-size: 26px;
  font-weight: 700;
  color: var(--brand-deep);
  line-height: 1.1;
}

.cd-unit {
  font-size: 11px;
  color: var(--text-2);
  margin-bottom: 6px;
}
</style>
