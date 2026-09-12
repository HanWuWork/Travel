<script setup>
/**
 * 猜你喜欢：基于收藏/行程/足迹的个性化推荐（未登录不展示）
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, Tag } from 'vant'
import { getRecommend } from '../api/recommend'
import { isLoggedIn } from '../utils/auth'

const router = useRouter()

const data = ref(null)

onMounted(async () => {
  if (!isLoggedIn()) return
  try {
    data.value = await getRecommend()
  } catch (e) {
    // 推荐失败静默
  }
})

function goCity(c) {
  router.push({ path: '/dest', query: { cityId: c.id } })
}

function goSpot(s) {
  router.push(`/dest/attraction/${s.id}`)
}
</script>

<template>
  <div v-if="data && (data.cities?.length || data.attractions?.length)" class="reco-section">
    <div class="reco-head">
      <div class="reco-title"><Icon name="fire-o" color="#4cccf4" /> 猜你喜欢</div>
      <span class="reco-basis">{{ data.basis }}</span>
    </div>

    <!-- 推荐城市 -->
    <div v-if="data.cities?.length" class="city-scroll">
      <div v-for="c in data.cities" :key="c.id" class="city-card" @click="goCity(c)">
        <div class="city-card__name">
          {{ c.name }}
          <span v-if="c.hot" class="hot">🔥</span>
        </div>
        <div class="city-card__province">{{ c.province }}</div>
        <div class="city-card__reason">{{ c.reason }}</div>
        <div class="city-card__desc">{{ c.description }}</div>
      </div>
    </div>

    <!-- 推荐景点 -->
    <div v-if="data.attractions?.length" class="spot-list">
      <div class="spot-list__title">就在你收藏的城市</div>
      <div v-for="s in data.attractions" :key="s.id" class="spot-row" @click="goSpot(s)">
        <div class="spot-main">
          <span class="spot-name">{{ s.name }}</span>
          <Tag plain size="medium" color="#0c0c0c">{{ s.cityName }}</Tag>
        </div>
        <div class="spot-meta">
          <span v-if="s.rating" class="rating">★ {{ s.rating.toFixed(1) }}</span>
          <span v-if="s.ticket" class="ticket">{{ s.ticket }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reco-section {
  margin: 12px;
  background: var(--surface);
  border-radius: 16px;
  padding: 14px;
}

.reco-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 10px;
}

.reco-title {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.reco-basis {
  font-size: 11px;
  color: var(--text-2);
}

.city-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.city-card {
  flex-shrink: 0;
  width: 168px;
  border-radius: 10px;
  background: linear-gradient(135deg, #fdfaf0, #fbf7e6);
  border: 1px solid #efe9d0;
  padding: 12px;
}

.city-card__name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
}

.hot {
  font-size: 12px;
}

.city-card__province {
  font-size: 11px;
  color: var(--text-2);
  margin-top: 2px;
}

.city-card__reason {
  font-size: 11px;
  color: var(--brand-deep);
  margin-top: 8px;
  line-height: 1.4;
}

.city-card__desc {
  font-size: 11px;
  color: var(--text-2);
  margin-top: 4px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.spot-list {
  margin-top: 12px;
  border-top: 1px solid var(--line);
  padding-top: 10px;
}

.spot-list__title {
  font-size: 13px;
  color: var(--text-2);
  margin-bottom: 6px;
}

.spot-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--line);
}

.spot-row:last-child {
  border-bottom: none;
}

.spot-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.spot-name {
  font-size: 14px;
  color: var(--text);
}

.spot-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--text-2);
  flex-shrink: 0;
}

.rating {
  color: var(--brand-deep);
  font-weight: 600;
}
</style>
