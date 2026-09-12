<script setup>
/**
 * 地点详情页：基础信息 + 收藏 + 同城附近推荐
 */
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon, showToast } from 'vant'
import { getAttraction, nearby } from '../api/dest'
import { addFavorite, deleteFavorite, listFavorites } from '../api/favorite'
import { isLoggedIn } from '../utils/auth'
import ReviewSection from '../components/ReviewSection.vue'

const TYPE_COLORS = {
  attraction: ['#0e3b3a', '#0b1420'],
  food: ['#5a3a0f', '#2b1a05'],
  hotel: ['#0e4f3a', '#06251b']
}

const route = useRoute()
const router = useRouter()

const spot = ref(null)
const error = ref('')
const nearSpots = ref([])
const favoriteId = ref(null)
const favLoading = ref(false)

onMounted(async () => {
  await load()
})

async function load() {
  error.value = ''
  nearSpots.value = []
  favoriteId.value = null
  try {
    spot.value = await getAttraction(route.params.id)
    loadNearby()
    if (isLoggedIn()) {
      try {
        const list = await listFavorites()
        const hit = (list || []).find(
          (f) => f.targetType === 'spot' && Number(f.targetId) === Number(spot.value.id)
        )
        favoriteId.value = hit ? hit.id : null
      } catch (e) {
        // 忽略收藏状态查询失败
      }
    }
  } catch (e) {
    error.value = e.message || '加载失败'
  }
}

async function loadNearby() {
  try {
    const list = await nearby({
      lat: spot.value.latitude,
      lng: spot.value.longitude,
      radiusKm: 50,
      type: 'all'
    })
    nearSpots.value = list.filter((s) => s.id !== spot.value.id).slice(0, 5)
  } catch (e) {
    // 附近推荐失败不影响主内容
  }
}

const gradient = () => {
  const colors = TYPE_COLORS[spot.value?.type] || TYPE_COLORS.attraction
  return `linear-gradient(135deg, ${colors[0]}, ${colors[1]})`
}

const onBack = () => router.back()

const viewOnMap = () => {
  if (!spot.value) return
  router.push({ path: '/map', query: { cityId: spot.value.cityId, focus: spot.value.id } })
}

const viewCity = () => {
  if (!spot.value) return
  router.push({ path: '/dest', query: { cityId: spot.value.cityId } })
}

async function toggleFavorite() {
  if (!isLoggedIn()) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  favLoading.value = true
  try {
    if (favoriteId.value) {
      await deleteFavorite(favoriteId.value)
      favoriteId.value = null
      showToast('已取消收藏')
    } else {
      const vo = await addFavorite({
        targetType: 'spot',
        targetId: spot.value.id,
        title: spot.value.name,
        description: spot.value.description
      })
      favoriteId.value = vo && vo.id ? vo.id : true
      showToast('已收藏')
    }
  } catch (e) {
    showToast(e.message || '操作失败')
  } finally {
    favLoading.value = false
  }
}
</script>

<template>
  <div class="attraction-detail">
    <van-nav-bar title="地点详情" left-arrow @click-left="onBack">
      <template #right>
        <Icon
          :name="favoriteId ? 'star' : 'star-o'"
          :color="favoriteId ? '#c98a00' : '#16160f'"
          size="20"
          @click="toggleFavorite"
        />
      </template>
    </van-nav-bar>

    <div v-if="error" class="state-wrap">
      <van-empty image="error" :description="error" />
    </div>

    <template v-else-if="spot">
      <!-- 顶部横幅 -->
      <div class="hero" :style="{ background: gradient() }">
        <div class="hero__badge">{{ spot.typeLabel }}</div>
        <h1 class="hero__name">{{ spot.name }}</h1>
        <div class="hero__city">
          <Icon name="location-o" /> {{ spot.cityName }}
          <span v-if="spot.rating" class="hero__rating">★ {{ spot.rating.toFixed(1) }}</span>
        </div>
        <div class="hero__take" @click="viewCity">查看{{ spot.cityName }}全部景点 ›</div>
      </div>

      <!-- 基础信息 -->
      <div class="info-card">
        <div class="info-row">
          <Icon name="gold-coin-o" class="info-icon" />
          <span class="info-label">门票/消费</span>
          <span class="info-value">{{ spot.ticket || '免费' }}</span>
        </div>
        <div class="info-row">
          <Icon name="clock-o" class="info-icon" />
          <span class="info-label">开放时间</span>
          <span class="info-value">{{ spot.openTime || '全天开放' }}</span>
        </div>
        <div class="info-row">
          <Icon name="underway-o" class="info-icon" />
          <span class="info-label">建议游玩</span>
          <span class="info-value">{{ spot.playTime || '—' }}</span>
        </div>
      </div>

      <!-- 简介 -->
      <div class="desc-card">
        <div class="card-title">景点简介</div>
        <p class="desc-text">{{ spot.description }}</p>
      </div>

      <!-- 评价区 -->
      <ReviewSection :attraction-id="spot.id" />

      <!-- 附近推荐 -->
      <div v-if="nearSpots.length" class="desc-card near-card">
        <div class="card-title">附近推荐</div>
        <div
          v-for="s in nearSpots"
          :key="s.id"
          class="near-item"
          @click="router.push(`/dest/attraction/${s.id}`)"
        >
          <div class="near-main">
            <span class="near-name">{{ s.name }}</span>
            <span class="near-type">{{ s.typeLabel }}</span>
          </div>
          <span class="near-dist">{{ s.distanceKm }}km</span>
        </div>
      </div>

      <div class="actions">
        <van-button type="primary" block round plain @click="viewOnMap">
          <Icon name="location-o" /> 在地图中查看
        </van-button>
      </div>
    </template>

    <div v-else class="state-wrap">
      <van-loading type="spinner" size="32">加载中...</van-loading>
    </div>
  </div>
</template>

<style scoped>
.attraction-detail {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 30px;
}

.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 24px;
}

.hero {
  padding: 28px 20px;
  color: #fff;
}

.hero__badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.22);
  border-radius: 10px;
  font-size: 12px;
  padding: 2px 10px;
  margin-bottom: 10px;
}

.hero__name {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
}

.hero__city {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  opacity: 0.95;
}

.hero__rating {
  margin-left: 12px;
  font-weight: 600;
}

.hero__take {
  margin-top: 12px;
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 3px 12px;
  font-size: 12px;
}

.near-card {
  margin-top: 12px;
}

.near-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--line);
}

.near-item:last-child {
  border-bottom: none;
}

.near-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.near-name {
  font-size: 14px;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.near-type {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--text-2);
  background: var(--bg);
  border-radius: 8px;
  padding: 1px 6px;
}

.near-dist {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--brand-deep);
}

.info-card {
  background: var(--surface);
  border-radius: 16px;
  margin: 12px;
  padding: 4px 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
  border-bottom: 1px solid var(--line);
  font-size: 14px;
}

.info-row:last-child {
  border-bottom: none;
}

.info-icon {
  color: var(--brand-deep);
  font-size: 16px;
}

.info-label {
  color: var(--text-2);
  width: 76px;
}

.info-value {
  flex: 1;
  text-align: right;
  color: var(--text);
  font-weight: 500;
}

.desc-card {
  background: var(--surface);
  border-radius: 16px;
  margin: 0 12px;
  padding: 14px 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.desc-text {
  margin: 0;
  font-size: 14px;
  color: var(--text-2);
  line-height: 1.7;
}

.actions {
  padding: 16px 12px 0;
}
</style>
