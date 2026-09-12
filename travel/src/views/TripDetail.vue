<script setup>
/**
 * 行程详情页（已保存行程的完整展示，含地图路线）
 * 支持两种模式：
 * - owner：我的行程（/api/trip）
 * - shared：协作行程（/api/collab），带版本轮询自动同步
 */
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Card, Icon, showToast } from 'vant'
import MapContainer from '../components/MapContainer.vue'
import TripExport from '../components/TripExport.vue'
import TripRefine from '../components/TripRefine.vue'
import TripCollab from '../components/TripCollab.vue'
import { getTrip } from '../api/trip'
import { collabTripDetail } from '../api/collab'
import { isLoggedIn } from '../utils/auth'

const DAY_COLORS = ['#0c0c0c', '#1f6fbf', '#076b50', '#0e7490', '#4a3f7a', '#155e75', '#334155', '#1e40af']

const route = useRoute()
const router = useRouter()

const plan = ref(null)
const error = ref('')
const activePoi = ref(null)
const viewMode = ref('list')
const showExport = ref(false)
const showRefine = ref(false)
const showCollab = ref(false)

const isShared = computed(() => route.query.shared === '1')
const version = ref(0)
const role = ref('owner')
const collabInfo = ref(null)
let pollTimer = null

function onRefined(newPlan) {
  plan.value = newPlan
  activePoi.value = null
}

const mapMarkers = computed(() => {
  const pois = (plan.value?.pois || []).filter((p) => p.latitude && p.longitude)
  return pois.map((p) => ({
    id: p.attractionId || p.seq,
    lat: p.latitude,
    lng: p.longitude,
    title: p.name,
    badge: p.seq,
    color: DAY_COLORS[((p.day || 1) - 1) % DAY_COLORS.length],
    active: activePoi.value && activePoi.value.seq === p.seq,
    origin: p
  }))
})

const mapPath = computed(() =>
  (plan.value?.pois || [])
    .filter((p) => p.latitude && p.longitude)
    .sort((a, b) => (a.seq || 0) - (b.seq || 0))
    .map((p) => ({ lat: p.latitude, lng: p.longitude }))
)

const mapCenter = computed(() => {
  const m = mapMarkers.value
  return m.length ? [m[0].lat, m[0].lng] : [35, 105]
})

onMounted(async () => {
  await loadTrip()
  // 协作行程：每 8 秒轮询版本号，成员修改后自动刷新
  if (isShared.value) {
    pollTimer = setInterval(async () => {
      try {
        const c = await collabTripDetail(route.query.id)
        if (c.version && c.version !== version.value) {
          version.value = c.version
          plan.value = c.plan
          showToast('行程已被协作成员更新')
        }
      } catch (e) {
        // 轮询失败忽略
      }
    }, 8000)
  }
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})

async function loadTrip() {
  try {
    if (isShared.value) {
      const c = await collabTripDetail(route.query.id)
      collabInfo.value = c
      version.value = c.version || 0
      role.value = c.role || 'editor'
      plan.value = c.plan
    } else {
      plan.value = await getTrip(route.query.id)
      role.value = 'owner'
    }
  } catch (e) {
    error.value = e.message || '加载失败'
  }
}

function onPoiClick(m) {
  activePoi.value = m.origin
}

const onBack = () => router.back()
</script>

<template>
  <div class="trip-detail">
    <van-nav-bar :title="isShared ? '协作行程' : '行程详情'" left-arrow @click-left="onBack">
      <template #right>
        <span v-if="isShared" class="role-badge">{{ role === 'owner' ? '创建者' : '协作者' }}</span>
        <Icon v-if="plan" name="friends-o" size="18" style="margin-right: 14px" @click="showCollab = true" />
        <Icon v-if="plan" name="edit" size="17" style="margin-right: 14px" @click="showRefine = true" />
        <Icon v-if="plan" name="share-o" size="18" @click="showExport = true" />
      </template>
    </van-nav-bar>

    <div v-if="error" class="state-wrap"><van-empty image="error" :description="error" /></div>

    <div v-else-if="plan" class="plan-content">
      <div class="mode-switch">
        <div class="mode-btn" :class="{ 'mode-btn--active': viewMode === 'list' }" @click="viewMode = 'list'">
          <Icon name="orders-o" /> 行程列表
        </div>
        <div class="mode-btn" :class="{ 'mode-btn--active': viewMode === 'map' }" @click="viewMode = 'map'">
          <Icon name="location-o" /> 地图路线
        </div>
      </div>

      <div v-if="viewMode === 'map'" class="map-mode">
        <div v-if="mapMarkers.length" class="plan-map">
          <MapContainer :markers="mapMarkers" :path="mapPath" :center="mapCenter" :zoom="12" @marker-click="onPoiClick" />
          <div class="map-legend">
            <span v-for="(c, i) in DAY_COLORS.slice(0, plan.days)" :key="i" class="legend-item">
              <i :style="{ background: c }"></i>第{{ i + 1 }}天
            </span>
          </div>
        </div>
        <van-empty v-else image="search" description="该行程未匹配到地图坐标" />
      </div>

      <template v-else>
        <Card class="summary-card">
          <template #title>
            <div class="card-title"><Icon name="todo-list-o" /> <span>{{ plan.destination }}</span></div>
          </template>
          <template #desc>
            <div class="summary-info">
              <div class="info-row"><span class="info-label">目的地</span><span class="info-value highlight">{{ plan.destination }}</span></div>
              <div class="info-row"><span class="info-label">预算</span><span class="info-value">¥{{ plan.budget }} 元</span></div>
              <div class="info-row"><span class="info-label">天数</span><span class="info-value">{{ plan.days }} 天</span></div>
            </div>
          </template>
        </Card>

        <Card class="itinerary-card">
          <template #title>
            <div class="card-title"><Icon name="clock-o" /> <span>每日行程</span></div>
          </template>
          <template #desc>
            <div class="itinerary-list">
              <div v-for="item in plan.itinerary" :key="item.day" class="itinerary-item">
                <div class="day-badge">第{{ item.day }}天</div>
                <div class="day-content">
                  <div class="day-title">{{ item.title }}</div>
                  <div class="day-desc">{{ item.description }}</div>
                  <div class="day-tip" v-if="item.tip"><Icon name="info-o" /> {{ item.tip }}</div>
                </div>
              </div>
            </div>
          </template>
        </Card>

        <Card class="budget-card">
          <template #title>
            <div class="card-title"><Icon name="gold-coin-o" /> <span>预算分配</span></div>
          </template>
          <template #desc>
            <div class="budget-list">
              <div v-for="(item, index) in plan.budgetBreakdown" :key="index" class="budget-item">
                <span class="budget-label">{{ item.label }}</span>
                <div class="budget-bar-wrap">
                  <div class="budget-bar" :style="{ width: item.percent + '%', background: item.color }"></div>
                </div>
                <span class="budget-amount">¥{{ item.amount }}</span>
              </div>
            </div>
          </template>
        </Card>
      </template>
    </div>

    <div v-else class="state-wrap"><van-loading type="spinner" size="32">加载中...</van-loading></div>

    <TripExport v-if="plan" v-model:show="showExport" :plan="plan" />
    <TripRefine v-if="plan" v-model:show="showRefine" :plan="plan" :mode="isShared ? 'shared' : 'owner'" @applied="onRefined" />
    <TripCollab v-if="plan" v-model:show="showCollab" :trip-id="plan.id" />
  </div>
</template>

<style scoped>
.trip-detail {
  min-height: 100vh;
  background: var(--bg);
}

.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 24px;
}

.role-badge {
  font-size: 11px;
  color: var(--brand-deep);
  background: var(--tint);
  border-radius: 9px;
  padding: 2px 8px;
}

.plan-content {
  padding: 12px;
  padding-bottom: 30px;
}

.mode-switch {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.mode-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 0;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 13px;
  color: var(--text-2);
}

.mode-btn--active {
  background: var(--grad-brand);
  border-color: transparent;
  color: var(--brand-ink);
  font-weight: 600;
}

.plan-map {
  position: relative;
  height: 380px;
  border-radius: 16px;
  overflow: hidden;
}

.map-legend {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 8px;
  padding: 4px 8px;
  display: flex;
  gap: 8px;
  z-index: 500;
  flex-wrap: wrap;
  max-width: 80%;
}

.legend-item {
  font-size: 11px;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.legend-item i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.summary-card,
.itinerary-card,
.budget-card {
  margin-bottom: 12px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
}

.summary-info {
  padding: 8px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--line);
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: var(--text-2);
  font-size: 14px;
}

.info-value {
  font-size: 15px;
  font-weight: 500;
}

.highlight {
  color: var(--brand-deep);
}

.itinerary-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--line);
}

.itinerary-item:last-child {
  border-bottom: none;
}

.day-badge {
  flex-shrink: 0;
  width: 56px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  background: var(--grad-brand);
  color: var(--brand-ink);
  font-size: 13px;
  border-radius: 14px;
}

.day-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.day-desc {
  font-size: 13px;
  color: var(--text-2);
  margin-bottom: 6px;
}

.day-tip {
  font-size: 12px;
  color: var(--brand-deep);
  display: flex;
  align-items: center;
  gap: 4px;
}

.budget-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.budget-label {
  width: 50px;
  font-size: 13px;
  color: var(--text-2);
  flex-shrink: 0;
}

.budget-bar-wrap {
  flex: 1;
  height: 10px;
  background: var(--line);
  border-radius: 5px;
  overflow: hidden;
}

.budget-bar {
  height: 100%;
  border-radius: 5px;
}

.budget-amount {
  width: 70px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: var(--danger);
}
</style>
