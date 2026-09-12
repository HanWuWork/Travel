<script setup>
/**
 * 足迹打卡：地图上按颜色区分 去过/想去/计划中 三种城市状态，支持打卡与统计
 */
import { computed, onMounted, ref } from 'vue'
import { Icon, showToast } from 'vant'
import MapContainer from '../components/MapContainer.vue'
import { listCities } from '../api/dest'
import { checkinStats, listCheckins, markCheckin, removeCheckin } from '../api/checkin'
import { isLoggedIn } from '../utils/auth'

const STATUS = [
  { value: 'visited', label: '去过', color: '#076b50' },
  { value: 'wish', label: '想去', color: '#1f6fbf' },
  { value: 'planned', label: '计划中', color: '#0e7490' }
]

const STATUS_COLORS = {
  visited: '#076b50',
  wish: '#1f6fbf',
  planned: '#0e7490'
}

const checkins = ref([])
const stats = ref(null)
const cities = ref([])
const activeStatus = ref('all')
const selected = ref(null)
const mapRef = ref(null)

const filtered = computed(() =>
  activeStatus.value === 'all'
    ? checkins.value
    : checkins.value.filter((c) => c.status === activeStatus.value)
)

const markers = computed(() =>
  filtered.value.map((c) => ({
    id: c.cityId,
    lat: c.latitude,
    lng: c.longitude,
    title: c.cityName,
    color: STATUS_COLORS[c.status],
    badge: c.cityName.slice(0, 1),
    active: selected.value && selected.value.cityId === c.cityId,
    origin: c
  }))
)

onMounted(load)

async function load() {
  if (!isLoggedIn()) return
  try {
    const [list, st] = await Promise.all([listCheckins(), checkinStats()])
    checkins.value = list
    stats.value = st
  } catch (e) {
    showToast(e.message || '足迹加载失败')
  }
}

function onSelect(c) {
  selected.value = c
  mapRef.value?.flyTo(c.latitude, c.longitude, 6)
}

function onMarkerClick(m) {
  selected.value = m.origin
}

async function mark(cityId, status) {
  try {
    await markCheckin(cityId, status)
    showToast('已更新')
    selected.value = null
    await load()
  } catch (e) {
    showToast(e.message || '打卡失败')
  }
}

async function cancel(cityId) {
  try {
    await removeCheckin(cityId)
    showToast('已取消打卡')
    selected.value = null
    await load()
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

function statusLabel(s) {
  return STATUS.find((x) => x.value === s)?.label || s
}
</script>

<template>
  <div class="footprint-page">
    <van-nav-bar title="我的足迹" left-arrow @click-left="$router.back()" />

    <!-- 统计卡 -->
    <div v-if="stats" class="stats-card">
      <div class="stat">
        <div class="stat__num" style="color: var(--success)">{{ stats.visitedCount }}</div>
        <div class="stat__label">去过</div>
      </div>
      <div class="stat">
        <div class="stat__num" style="color: var(--danger)">{{ stats.wishCount }}</div>
        <div class="stat__label">想去</div>
      </div>
      <div class="stat">
        <div class="stat__num" style="color: var(--brand-deep)">{{ stats.plannedCount }}</div>
        <div class="stat__label">计划中</div>
      </div>
      <div class="stat">
        <div class="stat__num" style="color: var(--brand-deep)">{{ (stats.visitedProvinces || []).length }}</div>
        <div class="stat__label">覆盖省份</div>
      </div>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <div
        class="filter-chip"
        :class="{ 'filter-chip--active': activeStatus === 'all' }"
        @click="activeStatus = 'all'"
      >全部</div>
      <div
        v-for="s in STATUS"
        :key="s.value"
        class="filter-chip"
        :class="{ 'filter-chip--active': activeStatus === s.value }"
        :style="activeStatus === s.value ? { background: s.color, borderColor: s.color } : {}"
        @click="activeStatus = s.value"
      >{{ s.label }}</div>
    </div>

    <!-- 足迹地图 -->
    <div class="map-wrap">
      <MapContainer
        ref="mapRef"
        :markers="markers"
        :center="[35, 105]"
        :zoom="4"
        @marker-click="onMarkerClick"
      />
      <div class="map-hint" v-if="markers.length">{{ markers.length }} 个打卡城市</div>
      <div class="map-empty" v-else>还没有足迹，去打卡吧！</div>
    </div>

    <!-- 选中城市操作面板 -->
    <transition name="slide-up">
      <div v-if="selected" class="action-panel">
        <div class="action-panel__title">
          {{ selected.cityName }}
          <span class="action-panel__cur">{{ statusLabel(selected.status) }}</span>
        </div>
        <div class="action-panel__btns">
          <van-button
            v-for="s in STATUS"
            :key="s.value"
            size="small"
            round
            :type="selected.status === s.value ? 'primary' : 'default'"
            @click="mark(selected.cityId, s.value)"
          >{{ s.label }}</van-button>
          <van-button size="small" round plain type="danger" @click="cancel(selected.cityId)">
            取消
          </van-button>
        </div>
      </div>
    </transition>

    <!-- 快捷打卡入口：热门城市 -->
    <div v-if="cities.length" class="quick-mark">
      <div class="quick-mark__title"><Icon name="plus" /> 快捷打卡</div>
      <div class="quick-mark__list">
        <span
          v-for="c in cities.slice(0, 12)"
          :key="c.id"
          class="quick-chip"
          @click="onSelect({ ...c, status: 'none', cityName: c.name })"
        >{{ c.name }}</span>
      </div>
    </div>

    <!-- 足迹列表 -->
    <div v-if="checkins.length" class="foot-list">
      <div
        v-for="c in checkins"
        :key="c.id"
        class="foot-item"
        @click="onSelect(c)"
      >
        <i class="foot-dot" :style="{ background: STATUS_COLORS[c.status] }"></i>
        <div class="foot-main">
          <span class="foot-name">{{ c.cityName }}</span>
          <span class="foot-meta">
            {{ statusLabel(c.status) }}
            <template v-if="c.visitCount">· 去过 {{ c.visitCount }} 次</template>
            <template v-if="c.note"> · {{ c.note }}</template>
          </span>
        </div>
        <Icon name="arrow" color="#5a6165" />
      </div>
    </div>

    <div style="height: 30px"></div>
  </div>
</template>

<style scoped>
.footprint-page {
  min-height: 100vh;
  background: var(--bg);
}

.stats-card {
  display: flex;
  background: var(--grad-dark);
  margin: 12px;
  border-radius: 16px;
  padding: 16px 0;
}

.stat {
  flex: 1;
  text-align: center;
  color: #fff;
}

.stat__num {
  font-size: 22px;
  font-weight: 700;
}

.stat__label {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 2px;
}

.filter-bar {
  display: flex;
  gap: 8px;
  padding: 0 12px 8px;
}

.filter-chip {
  padding: 3px 12px;
  border-radius: 16px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text-2);
}

.filter-chip--active {
  background: var(--ink);
  border-color: var(--text);
  color: #fff;
}

.map-wrap {
  position: relative;
  margin: 0 12px;
  height: 300px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.map-hint {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 10px;
  z-index: 500;
}

.map-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--text-2);
  font-size: 13px;
  background: rgba(255, 255, 255, 0.85);
  padding: 8px 14px;
  border-radius: 8px;
  z-index: 500;
}

.action-panel {
  margin: 10px 12px 0;
  background: var(--surface);
  border-radius: 16px;
  padding: 12px;
}

.action-panel__title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-panel__cur {
  font-size: 11px;
  color: var(--text-2);
  font-weight: 400;
}

.action-panel__btns {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.25s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.quick-mark {
  margin: 12px 12px 0;
  background: var(--surface);
  border-radius: 16px;
  padding: 12px;
}

.quick-mark__title {
  font-size: 13px;
  color: var(--text-2);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.quick-mark__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-chip {
  padding: 3px 12px;
  border-radius: 16px;
  background: var(--bg);
  font-size: 12px;
  color: var(--text);
}

.foot-list {
  margin: 12px;
  background: var(--surface);
  border-radius: 16px;
  overflow: hidden;
}

.foot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid var(--line);
}

.foot-item:last-child {
  border-bottom: none;
}

.foot-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.foot-main {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.foot-name {
  font-size: 14px;
  font-weight: 600;
}

.foot-meta {
  font-size: 12px;
  color: var(--text-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
