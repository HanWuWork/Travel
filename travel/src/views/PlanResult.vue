<template>
  <div class="plan-result">
    <van-nav-bar
      title="行程规划"
      left-arrow
      @click-left="onBack"
    >
      <template #right>
        <div class="nav-actions">
          <Icon name="share-o" size="18" style="margin-right: 12px" @click="showExport = true" />
          <van-button
            size="small"
            round
            :type="saved ? 'success' : 'primary'"
            :loading="saving"
            :disabled="saved"
            @click="onSaveTrip"
          >
            {{ saved ? '已保存' : '保存行程' }}
          </van-button>
        </div>
      </template>
    </van-nav-bar>

    <div class="plan-content">
      <!-- 加载中 -->
      <div v-if="loading" class="state-wrap">
        <van-loading type="spinner" size="32" color="#0c0c0c">正在为你生成行程规划...</van-loading>
      </div>

      <!-- 错误 -->
      <div v-else-if="error" class="state-wrap">
        <van-empty image="error" :description="error" />
        <van-button round type="primary" @click="fetchPlan">重新生成</van-button>
      </div>

      <!-- 规划结果 -->
      <template v-else-if="plan">
        <!-- 列表/地图模式切换 -->
        <div class="mode-switch">
          <div
            class="mode-btn"
            :class="{ 'mode-btn--active': viewMode === 'list' }"
            @click="viewMode = 'list'"
          >
            <Icon name="orders-o" /> 行程列表
          </div>
          <div
            class="mode-btn"
            :class="{ 'mode-btn--active': viewMode === 'map' }"
            @click="viewMode = 'map'"
          >
            <Icon name="location-o" /> 地图路线
            <span v-if="!mapMarkers.length" class="mode-tip">无坐标</span>
          </div>
        </div>

        <!-- 地图模式 -->
        <div v-if="viewMode === 'map'" class="map-mode">
          <div v-if="mapMarkers.length" class="plan-map">
            <MapContainer
              :markers="mapMarkers"
              :path="mapPath"
              :center="mapCenter"
              :zoom="12"
              @marker-click="onPoiClick"
            />
            <div class="map-legend">
              <span
                v-for="(c, i) in DAY_COLORS.slice(0, plan.days)"
                :key="i"
                class="legend-item"
              >
                <i :style="{ background: c }"></i>第{{ i + 1 }}天
              </span>
            </div>
          </div>
          <van-empty v-else image="search" description="AI 行程中的地点未匹配到地图坐标" />
          <div v-if="activePoi" class="poi-card">
            <span class="poi-card__seq">{{ activePoi.seq }}</span>
            <div class="poi-card__main">
              <div class="poi-card__name">{{ activePoi.name }}</div>
              <div class="poi-card__day">第 {{ activePoi.day || '-' }} 天行程点</div>
            </div>
          </div>
        </div>

        <!-- 列表模式 -->
        <template v-else>
        <!-- 天气 -->
        <Card v-if="weather && weather.daily && weather.daily.length" class="weather-card">
          <template #title>
            <div class="card-title">
              <Icon name="cloudy" />
              <span>{{ weather.city }}天气</span>
            </div>
          </template>
          <template #desc>
            <div class="weather-strip">
              <div v-for="(d, i) in weather.daily" :key="d.date" class="weather-day">
                <div class="weather-week">{{ i === 0 ? '今天' : d.weekday }}</div>
                <div class="weather-icon">{{ d.icon }}</div>
                <div class="weather-temp">{{ d.tempMin }}~{{ d.tempMax }}°</div>
                <div class="weather-text">{{ d.text }}</div>
              </div>
            </div>
          </template>
        </Card>

        <!-- 行程概要 -->
        <Card class="summary-card">
          <template #title>
            <div class="card-title">
              <Icon name="todo-list-o" />
              <span>行程概要</span>
            </div>
          </template>
          <template #desc>
            <div class="summary-info">
              <div class="info-row">
                <span class="info-label">目的地</span>
                <span class="info-value highlight">{{ plan.destination || '未设置' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">预算</span>
                <span class="info-value">¥{{ plan.budget || 0 }} 元</span>
              </div>
              <div class="info-row">
                <span class="info-label">天数</span>
                <span class="info-value">{{ plan.days || 0 }} 天</span>
              </div>
            </div>
          </template>
        </Card>

        <!-- 推荐行程 -->
        <Card class="itinerary-card">
          <template #title>
            <div class="card-title">
              <Icon name="clock-o" />
              <span>推荐行程</span>
              <span v-if="currentRoute" class="route-name-tag">{{ currentRoute.name }}</span>
            </div>
          </template>
          <template #desc>
            <div v-if="currentRoute && currentRoute.summary" class="route-summary">{{ currentRoute.summary }}</div>
            <div class="itinerary-list">
              <div
                v-for="(item, index) in currentItinerary"
                :key="index"
                class="itinerary-item"
              >
                <div class="day-badge">第{{ item.day }}天</div>
                <div class="day-content">
                  <div class="day-title">{{ item.title }}</div>
                  <!-- 上午/中午/下午/晚上 四段 -->
                  <div v-if="item.periods && item.periods.length" class="day-periods">
                    <div v-for="p in item.periods" :key="p.slot" class="period-row">
                      <span class="period-slot">{{ p.slot }}</span>
                      <span class="period-content">{{ p.content }}</span>
                    </div>
                  </div>
                  <div v-else class="day-desc">{{ item.description }}</div>
                  <div class="day-tip" v-if="item.tip">
                    <Icon name="info-o" /> {{ item.tip }}
                  </div>
                </div>
              </div>
            </div>
          </template>
        </Card>

        <!-- 预算分配 -->
        <Card class="budget-card">
          <template #title>
            <div class="card-title">
              <Icon name="gold-coin-o" />
              <span>预算分配</span>
            </div>
          </template>
          <template #desc>
            <div class="budget-list">
              <div
                v-for="(item, index) in plan.budgetBreakdown"
                :key="index"
                class="budget-item"
              >
                <span class="budget-label">{{ item.label }}</span>
                <div class="budget-bar-wrap">
                  <div class="budget-bar" :style="{ width: item.percent + '%', background: item.color }"></div>
                </div>
                <span class="budget-amount">¥{{ item.amount }}</span>
              </div>
            </div>
          </template>
        </Card>

        <!-- 温馨提示 -->
        <Card class="tips-card">
          <template #title>
            <div class="card-title">
              <Icon name="warning-o" />
              <span>温馨提示</span>
            </div>
          </template>
          <template #desc>
            <div class="tips-list">
              <div v-for="(tip, index) in plan.tips" :key="index" class="tip-item">
                <span class="tip-dot"></span>
                <span>{{ tip }}</span>
              </div>
            </div>
          </template>
        </Card>
        </template>
      </template>
    </div>

    <!-- 保存行程：设置出发日期（可选，用于倒计时） -->
    <Popup v-model:show="showSavePopup" position="bottom" round :style="{ padding: '18px 16px 24px' }">
      <div class="save-title">设置出发日期</div>
      <div class="save-sub">设置后可在首页看到出行倒计时（也可跳过）</div>
      <DatePicker
        v-model="pickerValue"
        title=""
        :min-date="minDate"
        :columns-type="['year', 'month', 'day']"
      />
      <div class="save-actions">
        <van-button round plain @click="doSave('')" :loading="saving">跳过，直接保存</van-button>
        <van-button round type="primary" @click="confirmSaveWithDate" :loading="saving">确定并保存</van-button>
      </div>
    </Popup>

    <!-- 底部悬浮路线切换 -->
    <div v-if="routes.length > 1" class="route-switch">
      <div class="route-switch__panel">
        <div
          v-for="(r, i) in routes"
          :key="r.index != null ? r.index : i"
          class="route-pill"
          :class="{ 'route-pill--active': i === activeRouteIndex }"
          @click="activeRouteIndex = i"
        >
          {{ r.name }}
        </div>
      </div>
    </div>

    <TripExport v-if="plan" v-model:show="showExport" :plan="plan" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Card, DatePicker, Icon, Popup, showToast } from 'vant'
import { createTravelPlan } from '../api/plan'
import { saveTrip } from '../api/trip'
import { weatherByCity } from '../api/weather'
import { isLoggedIn } from '../utils/auth'
import MapContainer from '../components/MapContainer.vue'
import TripExport from '../components/TripExport.vue'

const route = useRoute()
const router = useRouter()

const plan = ref(null)
const loading = ref(false)
const error = ref('')
const viewMode = ref('list')
const saving = ref(false)
const saved = ref(false)
const activePoi = ref(null)
const activeRouteIndex = ref(0)
const weather = ref(null)
const showSavePopup = ref(false)
const showExport = ref(false)
const pickerValue = ref([String(new Date().getFullYear()), String(new Date().getMonth() + 1), String(new Date().getDate())])
const minDate = new Date()

const destination = () => route.query.destination || ''
const budget = () => parseInt(route.query.budget) || 0
const days = () => parseInt(route.query.days) || 0

/** 多条路线方案与当前选中路线 */
const routes = computed(() => plan.value?.routes || [])
const currentRoute = computed(() => routes.value[activeRouteIndex.value] || null)
const currentItinerary = computed(() => currentRoute.value?.itinerary || plan.value?.itinerary || [])

/** 地图标记：有坐标的 POI 按天着色 */
const DAY_COLORS = ['#0c0c0c', '#1f6fbf', '#076b50', '#0e7490', '#4a3f7a', '#155e75', '#334155', '#1e40af']
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

function onPoiClick(marker) {
  activePoi.value = marker.origin
}

const onBack = () => {
  router.push('/')
}

async function onSaveTrip() {
  if (!plan.value) return
  if (!isLoggedIn()) {
    showToast('请先登录后再保存行程')
    router.push('/login')
    return
  }
  showSavePopup.value = true
}

/** 保存（startDate 可空） */
async function doSave(startDate) {
  saving.value = true
  try {
    const savedPlan = await saveTrip({ startDate: startDate || '', plan: plan.value })
    if (savedPlan) {
      plan.value.id = savedPlan.id
      plan.value.startDate = savedPlan.startDate
    }
    saved.value = true
    showSavePopup.value = false
    showToast('行程已保存，可在"我的-我的行程"查看')
  } catch (e) {
    showToast(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function confirmSaveWithDate() {
  const [y, m, d] = pickerValue.value
  const startDate = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  doSave(startDate)
}

const fetchPlan = async () => {
  const dest = destination()
  const bud = budget()
  const d = days()

  if (!dest || !bud || !d) {
    error.value = '缺少规划参数，请返回主页重新填写'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const data = await createTravelPlan({
      destination: dest,
      budget: bud,
      days: d
    })
    plan.value = data
    loadWeather(dest, d)
  } catch (e) {
    error.value = e.message || '生成行程失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(fetchPlan)

/** 加载目的地天气（失败静默处理，不影响行程展示） */
async function loadWeather(dest, dayCount) {
  try {
    const days = Math.max(3, Math.min(dayCount || 3, 7))
    weather.value = await weatherByCity(dest, days)
  } catch (e) {
    weather.value = null
  }
}
</script>

<style scoped>
.plan-result {
  min-height: 100vh;
  background-color: var(--surface-2);
}

.plan-content {
  padding: 12px;
  padding-bottom: 96px;
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

.mode-tip {
  font-size: 10px;
  color: #4cccf4;
}

.map-mode {
  position: relative;
}

.plan-map {
  position: relative;
  height: 380px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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
  color: var(--text);
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

.poi-card {
  margin-top: 10px;
  background: var(--surface);
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.poi-card__seq {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #0e7490;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.poi-card__name {
  font-size: 14px;
  font-weight: 600;
}

.poi-card__day {
  font-size: 12px;
  color: var(--text-2);
}

.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 80px 24px;
}

.weather-card {
  margin-bottom: 12px;
}

.weather-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 6px 0;
}

.weather-day {
  flex-shrink: 0;
  min-width: 62px;
  text-align: center;
  background: var(--tint);
  border-radius: 10px;
  padding: 8px 6px;
}

.weather-week {
  font-size: 12px;
  font-weight: 600;
  color: var(--text);
}

.weather-icon {
  font-size: 20px;
  margin: 4px 0;
}

.weather-temp {
  font-size: 11px;
  color: var(--brand-deep);
  font-weight: 600;
}

.weather-text {
  font-size: 10px;
  color: var(--text-2);
  margin-top: 2px;
}

.save-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
}

.save-sub {
  font-size: 12px;
  color: var(--text-2);
  text-align: center;
  margin: 6px 0 4px;
}

.save-actions {
  display: flex;
  gap: 12px;
}

.save-actions .van-button {
  flex: 1;
}

.nav-actions {
  display: flex;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
}

.summary-card,
.itinerary-card,
.budget-card,
.tips-card {
  margin-bottom: 12px;
}

.summary-info {
  padding: 8px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  font-size: 16px;
  font-weight: 600;
}

.itinerary-list {
  padding: 8px 0;
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

.day-content {
  flex: 1;
  min-width: 0;
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

.route-name-tag {
  margin-left: 6px;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--brand-deep);
  background: var(--tint);
  border-radius: 12px;
}

.route-summary {
  font-size: 13px;
  color: var(--text-2);
  padding: 2px 0 6px;
  line-height: 1.5;
}

.day-periods {
  margin: 4px 0 2px;
}

.period-row {
  display: flex;
  gap: 8px;
  padding: 3px 0;
  align-items: flex-start;
}

.period-slot {
  flex-shrink: 0;
  width: 34px;
  padding-top: 1px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-3);
}

.period-content {
  flex: 1;
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.55;
}

.budget-list {
  padding: 8px 0;
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
  background-color: var(--line);
  border-radius: 5px;
  overflow: hidden;
}

.budget-bar {
  height: 100%;
  border-radius: 5px;
  transition: width 0.3s ease;
}

.budget-amount {
  width: 70px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: var(--danger);
  flex-shrink: 0;
}

.tips-list {
  padding: 8px 0;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.6;
}

.tip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--brand-deep);
  margin-top: 7px;
  flex-shrink: 0;
}

/* 底部悬浮路线切换胶囊 */
.route-switch {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  padding: 10px 12px calc(14px + env(safe-area-inset-bottom));
}

.route-switch__panel {
  max-width: 400px;
  margin: 0 auto;
  display: flex;
  gap: 6px;
  padding: 6px;
  border-radius: 26px;
  background: rgba(5, 18, 26, 0.92);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.22);
}

.route-pill {
  flex: 1;
  min-width: 0;
  text-align: center;
  padding: 9px 8px;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: rgba(255, 255, 255, 0.78);
  border-radius: 20px;
  transition: all 0.25s ease;
  cursor: pointer;
}

.route-pill--active {
  background: var(--grad-brand);
  color: var(--brand-ink);
  font-weight: 600;
}
</style>
