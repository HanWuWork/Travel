<script setup>
/**
 * 我的行程列表：查看/删除已保存的 AI 行程，点击进入行程详情（含地图路线）
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, showConfirmDialog, showToast } from 'vant'
import { deleteTrip, listTrips } from '../api/trip'
import { listCollabTrips } from '../api/collab'

const router = useRouter()

const trips = ref([])
const collabTrips = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [mine, collab] = await Promise.all([
      listTrips().catch(() => []),
      listCollabTrips().catch(() => [])
    ])
    trips.value = mine || []
    collabTrips.value = collab || []
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
})

async function onDelete(trip) {
  try {
    await showConfirmDialog({ title: '提示', message: `确定删除「${trip.destination}」行程吗？` })
    await deleteTrip(trip.id)
    trips.value = trips.value.filter((t) => t.id !== trip.id)
    showToast('已删除')
  } catch (e) {
    // 用户取消
  }
}

function planText(trip) {
  const days = (trip.itinerary || []).length
  return days ? `${days} 天行程` : '行程详情'
}

function poiCount(trip) {
  return (trip.pois || []).length
}

/** 距出发的天数（null 表示未设置日期或日期无效） */
function countdown(trip) {
  if (!trip.startDate) return null
  const diff = new Date(trip.startDate + 'T00:00:00').setHours(0, 0, 0, 0) - new Date().setHours(0, 0, 0, 0)
  return Number.isNaN(diff) ? null : Math.round(diff / 86400000)
}

function countdownText(trip) {
  const d = countdown(trip)
  if (d === null) return ''
  if (d > 0) return `还有 ${d} 天`
  if (d === 0) return '今天出发'
  return `已出发 ${Math.abs(d)} 天`
}
</script>

<template>
  <div class="trips-page">
    <van-nav-bar title="我的行程" left-arrow @click-left="$router.back()" />

    <van-empty v-if="!loading && !trips.length && !collabTrips.length" description="还没有保存的行程，去主页生成一份吧" />

    <!-- 我参与的协作行程 -->
    <div v-if="collabTrips.length" class="collab-block">
      <div class="block-title"><Icon name="friends-o" /> 我参与的协作行程</div>
      <div v-for="c in collabTrips" :key="c.tripId" class="collab-card" @click="router.push({ path: '/trip-detail', query: { id: c.tripId, shared: '1' } })">
        <div class="collab-card__main">
          <div class="collab-card__dest">{{ c.destination }}</div>
          <div class="collab-card__meta">{{ c.days }}天 · 来自 {{ c.ownerName }} · {{ c.memberCount }}人协作</div>
        </div>
        <Icon name="arrow" color="#5a6165" />
      </div>
    </div>

    <!-- 我保存的行程 -->
    <div v-if="trips.length" class="trip-list">
      <div v-for="t in trips" :key="t.id" class="trip-card">
        <div class="trip-card__head">
          <span class="trip-card__dest">{{ t.destination }}</span>
          <span class="trip-card__meta">{{ t.days }}天 · 预算¥{{ t.budget }}</span>
        </div>
        <div v-if="t.startDate" class="trip-card__date">
          <Icon name="calendar-o" /> 出发：{{ t.startDate }}
          <span class="trip-countdown" v-if="countdown(t) !== null">{{ countdownText(t) }}</span>
        </div>
        <div class="trip-card__body" v-if="(t.itinerary || []).length">
          <div class="trip-day" v-for="d in t.itinerary.slice(0, 2)" :key="d.day">
            <span class="trip-day__badge">D{{ d.day }}</span>
            <span class="trip-day__title">{{ d.title }}</span>
          </div>
          <div v-if="t.itinerary.length > 2" class="trip-day__more">
            ...共 {{ t.itinerary.length }} 天
          </div>
        </div>
        <div class="trip-card__foot">
          <span v-if="poiCount(t)" class="trip-pois">{{ poiCount(t) }} 个地点已上地图</span>
          <span v-else class="trip-pois">{{ planText(t) }}</span>
          <div class="trip-card__actions">
            <van-button
              size="small"
              round
              type="primary"
              plain
              @click="router.push({ path: '/trip-detail', query: { id: t.id } })"
            >查看</van-button>
            <van-button size="small" round plain type="danger" @click="onDelete(t)">删除</van-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.trips-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 30px;
}

.collab-block {
  padding: 12px 12px 0;
}

.block-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.collab-card {
  display: flex;
  align-items: center;
  background: var(--surface);
  border-radius: 16px;
  padding: 14px;
  margin-bottom: 10px;
  border-left: 3px solid var(--brand-2);
}

.collab-card__main {
  flex: 1;
}

.collab-card__dest {
  font-size: 15px;
  font-weight: 600;
}

.collab-card__meta {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 4px;
}

.trip-list {
  padding: 12px;
}

.trip-card {
  background: var(--surface);
  border-radius: 16px;
  padding: 14px;
  margin-bottom: 12px;
}

.trip-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.trip-card__dest {
  font-size: 16px;
  font-weight: 700;
}

.trip-card__meta {
  font-size: 12px;
  color: var(--text-2);
}

.trip-card__date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-2);
  margin-bottom: 8px;
}

.trip-countdown {
  margin-left: 6px;
  color: var(--brand-deep);
  font-weight: 600;
}

.trip-card__body {
  margin-bottom: 8px;
}

.trip-day {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.trip-day__badge {
  flex-shrink: 0;
  width: 30px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  background: var(--tint);
  color: var(--brand-deep);
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.trip-day__title {
  font-size: 13px;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trip-day__more {
  font-size: 12px;
  color: var(--text-2);
  padding-left: 38px;
}

.trip-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid var(--line);
  padding-top: 8px;
}

.trip-pois {
  font-size: 12px;
  color: var(--success);
}

.trip-card__actions {
  display: flex;
  gap: 8px;
}
</style>
