<script setup>
/**
 * 目的地地图总览：选择城市 → 地图上展示该城市的景点/美食/住宿标记
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon, Loading, Tag } from 'vant'
import MapContainer from '../components/MapContainer.vue'
import { listAttractions, listCities } from '../api/dest'

const TYPE_OPTIONS = [
  { value: 'all', label: '全部', color: '#0c0c0c' },
  { value: 'attraction', label: '景点', color: '#0c0c0c' },
  { value: 'food', label: '美食', color: '#1f6fbf' },
  { value: 'hotel', label: '住宿', color: '#076b50' }
]

const TYPE_COLORS = {
  attraction: '#0c0c0c',
  food: '#1f6fbf',
  hotel: '#076b50'
}

/** 标签用亮色（暗色底卡片），地图标记用上面的深色（浅色底图） */
const TAG_COLORS = { attraction: 'var(--tag-attraction)', food: 'var(--tag-food)', hotel: 'var(--tag-hotel)' }

const route = useRoute()
const router = useRouter()

const cities = ref([])
const spots = ref([])
const currentCity = ref(null)
const activeType = ref('all')
const selected = ref(null)
const loadingSpots = ref(false)
const mapRef = ref(null)

const markers = computed(() =>
  spots.value.map((s) => ({
    id: s.id,
    lat: s.latitude,
    lng: s.longitude,
    title: s.name,
    color: TYPE_COLORS[s.type] || '#0c0c0c',
    active: selected.value && selected.value.id === s.id,
    origin: s
  }))
)

onMounted(async () => {
  try {
    cities.value = await listCities()
    const cityId = Number(route.query.cityId) || cities.value[0]?.id
    const city = cities.value.find((c) => c.id === cityId) || cities.value[0]
    if (city) {
      await selectCity(city)
    }
  } catch (e) {
    // 城市列表加载失败时保持空地图
  }
})

async function selectCity(city) {
  currentCity.value = city
  selected.value = null
  mapRef.value?.flyTo(city.latitude, city.longitude, 12)
  await loadSpots()
}

async function changeType(type) {
  activeType.value = type
  await loadSpots()
}

async function loadSpots() {
  if (!currentCity.value) return
  loadingSpots.value = true
  try {
    spots.value = await listAttractions({
      cityId: currentCity.value.id,
      type: activeType.value
    })
    // 支持从景点详情页跳转定位：/map?cityId=x&focus=景点id
    const focusId = Number(route.query.focus)
    if (focusId) {
      const target = spots.value.find((s) => s.id === focusId)
      if (target) selectSpot(target)
    }
  } finally {
    loadingSpots.value = false
  }
}

function selectSpot(spot) {
  selected.value = spot
  mapRef.value?.flyTo(spot.latitude, spot.longitude, 14)
}

function onMarkerClick(marker) {
  selectSpot(marker.origin)
}

function goDetail() {
  if (!selected.value) return
  router.push(`/dest/attraction/${selected.value.id}`)
}

function resetView() {
  mapRef.value?.fitMarkers()
}
</script>

<template>
  <div class="map-page">
    <van-nav-bar title="目的地地图" />

    <!-- 城市选择 -->
    <div class="city-bar">
      <div
        v-for="c in cities"
        :key="c.id"
        class="city-chip"
        :class="{ 'city-chip--active': currentCity && currentCity.id === c.id }"
        @click="selectCity(c)"
      >
        <span v-if="c.hot" class="hot-dot">🔥</span>{{ c.name }}
      </div>
    </div>

    <!-- 类型筛选 -->
    <div class="type-bar">
      <div
        v-for="t in TYPE_OPTIONS"
        :key="t.value"
        class="type-chip"
        :class="{ 'type-chip--active': activeType === t.value }"
        :style="activeType === t.value ? { background: t.color, borderColor: t.color } : {}"
        @click="changeType(t.value)"
      >
        {{ t.label }}
      </div>
      <div class="type-bar__spacer"></div>
      <div class="map-tool" @click="resetView"><Icon name="expand-o" /></div>
    </div>

    <!-- 地图 -->
    <div class="map-wrap">
      <MapContainer
        ref="mapRef"
        :markers="markers"
        :center="currentCity ? [currentCity.latitude, currentCity.longitude] : [35, 105]"
        :zoom="11"
        @marker-click="onMarkerClick"
      />
      <div v-if="loadingSpots" class="map-loading">
        <Loading size="24" vertical>加载中...</Loading>
      </div>
      <div class="map-hint">{{ spots.length }} 个地点 · 点击标记查看详情</div>
    </div>

    <!-- 底部地点卡片 -->
    <transition name="card-slide">
      <div v-if="selected" class="spot-card">
        <div class="spot-card__main">
          <div class="spot-card__title-row">
            <span class="spot-card__title">{{ selected.name }}</span>
            <Tag :color="TAG_COLORS[selected.type]" plain>{{ selected.typeLabel }}</Tag>
          </div>
          <div class="spot-card__meta">
            <span v-if="selected.rating" class="rating">★ {{ selected.rating.toFixed(1) }}</span>
            <span v-if="selected.ticket">{{ selected.ticket }}</span>
            <span v-else-if="selected.openTime">{{ selected.openTime }}</span>
          </div>
          <div class="spot-card__desc">{{ selected.description }}</div>
        </div>
        <van-button type="primary" round size="small" @click="goDetail">详情</van-button>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.map-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 50px);
  background: var(--bg);
}

.city-bar {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 12px 6px;
  flex-shrink: 0;
  scrollbar-width: none;
}

.city-bar::-webkit-scrollbar {
  display: none;
}

.city-chip {
  flex-shrink: 0;
  padding: 4px 12px;
  border-radius: 14px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 13px;
  color: var(--text);
}

.city-chip--active {
  background: var(--grad-brand);
  border-color: transparent;
  color: var(--brand-ink);
  font-weight: 600;
}

.hot-dot {
  font-size: 11px;
  margin-right: 2px;
}

.type-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 10px;
  flex-shrink: 0;
}

.type-chip {
  padding: 2px 10px;
  border-radius: 10px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text-2);
}

.type-chip--active {
  color: #fff;
  font-weight: 600;
}

.type-bar__spacer {
  flex: 1;
}

.map-tool {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--surface);
  border: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-2);
  font-size: 15px;
}

.map-wrap {
  flex: 1;
  position: relative;
  min-height: 0;
}

.map-loading {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.92);
  border-radius: 8px;
  padding: 6px 14px;
  z-index: 500;
}

.map-hint {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 11px;
  border-radius: 10px;
  padding: 2px 10px;
  z-index: 500;
  white-space: nowrap;
}

.spot-card {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 14px;
  background: var(--surface);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.14);
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 600;
}

.spot-card__main {
  flex: 1;
  min-width: 0;
}

.spot-card__title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.spot-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}

.spot-card__meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--text-2);
  margin-top: 4px;
}

.rating {
  color: var(--brand-deep);
  font-weight: 600;
}

.spot-card__desc {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-slide-enter-active,
.card-slide-leave-active {
  transition: transform 0.25s ease, opacity 0.25s ease;
}

.card-slide-enter-from,
.card-slide-leave-to {
  transform: translateY(30px);
  opacity: 0;
}
</style>
