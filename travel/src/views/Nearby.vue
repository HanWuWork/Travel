<script setup>
/**
 * 周边探索：基于浏览器定位搜索附近景点/美食/住宿，点击可查看详情
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, showToast, Tag } from 'vant'
import MapContainer from '../components/MapContainer.vue'
import { nearby } from '../api/dest'

const TYPE_COLORS = { attraction: '#0c0c0c', food: '#1f6fbf', hotel: '#076b50' }
/** 标签用亮色（暗色底），地图标记用上面的深色（浅色底图） */
const TAG_COLORS = { attraction: 'var(--tag-attraction)', food: 'var(--tag-food)', hotel: 'var(--tag-hotel)' }
const TYPE_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: 'attraction', label: '景点' },
  { value: 'food', label: '美食' },
  { value: 'hotel', label: '住宿' }
]

const router = useRouter()

const myPos = ref(null)
const spots = ref([])
const activeType = ref('all')
const loading = ref(false)
const located = ref(false)
const locateError = ref('')
const selected = ref(null)
const mapRef = ref(null)

const markers = computed(() =>
  spots.value.map((s) => ({
    id: s.id,
    lat: s.latitude,
    lng: s.longitude,
    title: s.name,
    color: TYPE_COLORS[s.type] || '#0c0c0c',
    badge: '',
    active: selected.value && selected.value.id === s.id,
    origin: s
  }))
)

onMounted(() => {
  if (!navigator.geolocation) {
    locateError.value = '当前浏览器不支持定位'
    return
  }
  loading.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      myPos.value = { lat: pos.coords.latitude, lng: pos.coords.longitude }
      located.value = true
      loading.value = false
      search()
    },
    (err) => {
      locateError.value = '定位失败，请检查浏览器定位权限'
      loading.value = false
    },
    { timeout: 8000 }
  )
})

async function search() {
  if (!myPos.value) return
  loading.value = true
  try {
    spots.value = await nearby({
      lat: myPos.value.lat,
      lng: myPos.value.lng,
      radiusKm: 20,
      type: activeType.value
    })
  } catch (e) {
    showToast(e.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

function changeType(v) {
  activeType.value = v
  search()
}

function select(s) {
  selected.value = s
  mapRef.value?.flyTo(s.latitude, s.longitude, 13)
}

function goDetail() {
  if (selected.value) router.push(`/dest/attraction/${selected.value.id}`)
}
</script>

<template>
  <div class="nearby-page">
    <van-nav-bar title="周边探索" left-arrow @click-left="$router.back()" />

    <!-- 定位状态 -->
    <div class="locate-bar">
      <template v-if="located">
        <Icon name="location-o" color="#04dc9c" />
        <span>已定位（{{ myPos.lat.toFixed(3) }}, {{ myPos.lng.toFixed(3) }}）· 半径20km</span>
      </template>
      <template v-else-if="loading">
        <van-loading size="14" />
        <span>正在定位...</span>
      </template>
      <template v-else>
        <Icon name="warning-o" color="#4cccf4" />
        <span>{{ locateError || '尚未定位' }}</span>
      </template>
    </div>

    <!-- 类型筛选 -->
    <div class="type-bar">
      <div
        v-for="t in TYPE_OPTIONS"
        :key="t.value"
        class="type-chip"
        :class="{ 'type-chip--active': activeType === t.value }"
        @click="changeType(t.value)"
      >{{ t.label }}</div>
    </div>

    <!-- 地图 -->
    <div v-if="located" class="map-wrap">
      <MapContainer
        ref="mapRef"
        :markers="markers"
        :center="[myPos.lat, myPos.lng]"
        :zoom="12"
        @marker-click="(m) => select(m.origin)"
      />
      <div class="map-hint">{{ spots.length }} 个附近地点</div>
    </div>

    <!-- 列表 -->
    <div v-if="located && spots.length" class="spot-list">
      <div
        v-for="s in spots"
        :key="s.id"
        class="spot-item"
        :class="{ 'spot-item--active': selected && selected.id === s.id }"
        @click="select(s)"
      >
        <div class="spot-main">
          <div class="spot-name">{{ s.name }}</div>
          <div class="spot-meta">
            <Tag :color="TAG_COLORS[s.type]" plain size="medium">{{ s.typeLabel }}</Tag>
            <span v-if="s.rating" class="rating">★ {{ s.rating.toFixed(1) }}</span>
            <span v-if="s.ticket" class="ticket">{{ s.ticket }}</span>
          </div>
        </div>
        <span class="spot-dist">{{ s.distanceKm }} km</span>
      </div>
    </div>
    <van-empty
      v-else-if="located && !loading && !spots.length"
      description="20 公里内暂无收录地点，可换个城市看看"
    />

    <div v-if="selected" class="detail-bar">
      <div class="detail-bar__name">{{ selected.name }}</div>
      <van-button type="primary" size="small" round @click="goDetail">查看详情</van-button>
    </div>
    <div style="height: 30px"></div>
  </div>
</template>

<style scoped>
.nearby-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 30px;
}

.locate-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  font-size: 12px;
  color: var(--text-2);
}

.type-bar {
  display: flex;
  gap: 8px;
  padding: 0 12px 10px;
}

.type-chip {
  padding: 3px 14px;
  border-radius: 13px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text-2);
}

.type-chip--active {
  background: var(--grad-brand);
  border-color: transparent;
  color: var(--brand-ink);
  font-weight: 600;
}

.map-wrap {
  position: relative;
  margin: 0 12px;
  height: 260px;
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

.spot-list {
  margin: 12px;
  background: var(--surface);
  border-radius: 16px;
  overflow: hidden;
}

.spot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid var(--line);
}

.spot-item:last-child {
  border-bottom: none;
}

.spot-item--active {
  background: var(--tint);
}

.spot-main {
  flex: 1;
  min-width: 0;
}

.spot-name {
  font-size: 14px;
  font-weight: 600;
}

.spot-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-2);
}

.rating {
  color: var(--brand-deep);
  font-weight: 600;
}

.spot-dist {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--brand-deep);
  font-weight: 600;
}

.detail-bar {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 60px);
  max-width: 690px;
  background: var(--surface);
  border-radius: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.16);
  padding: 10px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  z-index: 900;
}

.detail-bar__name {
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
