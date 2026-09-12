<script setup>
/**
 * 目的地/景点库：城市列表 → 城市详情 → 景点列表/搜索
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon, Empty, Loading, Search, Tag } from 'vant'
import { listAttractions, listCities } from '../api/dest'

const TYPE_COLORS = { attraction: 'var(--tag-attraction)', food: 'var(--tag-food)', hotel: 'var(--tag-hotel)' }
const TYPE_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: 'attraction', label: '景点' },
  { value: 'food', label: '美食' },
  { value: 'hotel', label: '住宿' }
]

const route = useRoute()
const router = useRouter()

const cities = ref([])
const currentCity = ref(null)
const spots = ref([])
const activeType = ref('all')
const keyword = ref('')
const loading = ref(false)

const hotCities = computed(() => cities.value.filter((c) => c.hot))
const otherCities = computed(() => cities.value.filter((c) => !c.hot))

onMounted(async () => {
  try {
    cities.value = await listCities()
    const cityId = Number(route.query.cityId)
    const city = cities.value.find((c) => c.id === cityId) || cities.value[0]
    if (city) await selectCity(city)
  } catch (e) {
    // ignore
  }
})

async function selectCity(city) {
  currentCity.value = city
  keyword.value = ''
  activeType.value = 'all'
  await load()
}

async function load() {
  if (!currentCity.value) return
  loading.value = true
  try {
    spots.value = await listAttractions({
      cityId: currentCity.value.id,
      type: activeType.value,
      keyword: keyword.value.trim()
    })
  } finally {
    loading.value = false
  }
}

function changeType(v) {
  activeType.value = v
  load()
}

function onSearch() {
  load()
}

function goSpot(s) {
  router.push(`/dest/attraction/${s.id}`)
}

function goMap() {
  router.push({ path: '/map', query: { cityId: currentCity.value.id } })
}
</script>

<template>
  <div class="dest-page">
    <van-nav-bar title="目的地" left-arrow @click-left="$router.back()" />

    <!-- 当前城市横幅 -->
    <div v-if="currentCity" class="city-hero">
      <div class="city-hero__name">{{ currentCity.name }}</div>
      <div class="city-hero__province">{{ currentCity.province }} · 收录 {{ currentCity.spotCount }} 个地点</div>
      <div class="city-hero__desc">{{ currentCity.description }}</div>
      <div class="city-hero__map" @click="goMap"><Icon name="location-o" /> 在地图查看</div>
    </div>

    <!-- 城市切换 -->
    <div class="city-switch">
      <div class="city-switch__label">热门</div>
      <div class="city-chips">
        <span
          v-for="c in hotCities"
          :key="c.id"
          class="city-chip"
          :class="{ 'city-chip--active': currentCity && currentCity.id === c.id }"
          @click="selectCity(c)"
        >{{ c.name }}</span>
      </div>
    </div>
    <div class="city-switch">
      <div class="city-switch__label">更多</div>
      <div class="city-chips">
        <span
          v-for="c in otherCities"
          :key="c.id"
          class="city-chip"
          :class="{ 'city-chip--active': currentCity && currentCity.id === c.id }"
          @click="selectCity(c)"
        >{{ c.name }}</span>
      </div>
    </div>

    <!-- 搜索 + 类型 -->
    <div class="toolbar">
      <Search v-model="keyword" placeholder="搜索景点/美食" @search="onSearch" />
      <div class="type-chips">
        <span
          v-for="t in TYPE_OPTIONS"
          :key="t.value"
          class="type-chip"
          :class="{ 'type-chip--active': activeType === t.value }"
          @click="changeType(t.value)"
        >{{ t.label }}</span>
      </div>
    </div>

    <!-- 地点列表 -->
    <div v-if="loading" class="state-wrap"><Loading size="28" vertical>加载中...</Loading></div>
    <Empty v-else-if="!spots.length" description="没有找到相关地点" />
    <div v-else class="spot-list">
      <div v-for="s in spots" :key="s.id" class="spot-card" @click="goSpot(s)">
        <div class="spot-card__head">
          <span class="spot-card__name">{{ s.name }}</span>
          <Tag :color="TYPE_COLORS[s.type]" plain>{{ s.typeLabel }}</Tag>
        </div>
        <div class="spot-card__meta">
          <span v-if="s.rating" class="rating">★ {{ s.rating.toFixed(1) }}</span>
          <span v-if="s.ticket">{{ s.ticket }}</span>
          <span v-if="s.playTime">· {{ s.playTime }}</span>
        </div>
        <div class="spot-card__desc">{{ s.description }}</div>
      </div>
    </div>

    <div style="height: 30px"></div>
  </div>
</template>

<style scoped>
.dest-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 30px;
}

.city-hero {
  margin: 12px;
  padding: 18px;
  border-radius: 14px;
  background: var(--grad-dark);
  color: #fff;
  position: relative;
}

.city-hero__name {
  font-size: 22px;
  font-weight: 700;
}

.city-hero__province {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
}

.city-hero__desc {
  font-size: 13px;
  opacity: 0.92;
  margin-top: 10px;
  line-height: 1.6;
  padding-right: 70px;
}

.city-hero__map {
  position: absolute;
  right: 14px;
  bottom: 16px;
  background: rgba(255, 255, 255, 0.22);
  border-radius: 14px;
  padding: 4px 10px;
  font-size: 12px;
}

.city-switch {
  display: flex;
  gap: 8px;
  padding: 0 12px 8px;
  align-items: flex-start;
}

.city-switch__label {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-2);
  padding-top: 4px;
  width: 28px;
}

.city-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.city-chip {
  padding: 3px 12px;
  border-radius: 16px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text);
}

.city-chip--active {
  background: var(--grad-brand);
  border-color: transparent;
  color: var(--brand-ink);
  font-weight: 600;
}

.toolbar {
  padding: 4px 12px 0;
}

.type-chips {
  display: flex;
  gap: 8px;
  padding: 10px 0;
}

.type-chip {
  padding: 3px 12px;
  border-radius: 16px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text-2);
}

.type-chip--active {
  background: var(--ink);
  border-color: var(--text);
  color: #fff;
}

.state-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.spot-list {
  padding: 0 12px;
}

.spot-card {
  background: var(--surface);
  border-radius: 16px;
  padding: 14px;
  margin-bottom: 12px;
}

.spot-card__head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spot-card__name {
  font-size: 15px;
  font-weight: 600;
}

.spot-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-2);
}

.rating {
  color: var(--brand-deep);
  font-weight: 600;
}

.spot-card__desc {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-2);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
