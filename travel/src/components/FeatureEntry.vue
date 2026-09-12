<script setup>
/**
 * 首页功能入口宫格：地图/足迹/周边/工具箱/我的行程等新功能统一从这里进入
 */
import { Icon } from 'vant'
import { useRouter } from 'vue-router'

const router = useRouter()

const entries = [
  { name: '目的地地图', icon: 'location-o', color: '#04dc9c', to: '/map' },
  { name: '目的地库', icon: 'apps-o', color: '#4cccf4', to: '/dest' },
  { name: '我的足迹', icon: 'chart-trending-o', color: '#bce4fc', to: '/footprint' },
  { name: '周边探索', icon: 'aim', color: '#2fe0a8', to: '/nearby' },
  { name: '我的行程', icon: 'calendar-o', color: '#4cccf4', to: '/trips' },
  { name: '旅行记账', icon: 'gold-coin-o', color: '#e5484d', to: '/expense' },
  { name: '打包清单', icon: 'checked', color: '#04dc9c', to: '/packing' },
  { name: '汇率换算', icon: 'exchange', color: '#2fe0a8', to: '/exchange' },
  { name: '社区游记', icon: 'notes-o', color: '#bce4fc', to: '/posts' },
  { name: '目的地天气', icon: 'cloudy', color: '#4cccf4', to: '/weather' },
  { name: '每日签到', icon: 'points', color: '#04dc9c', to: '/signin' },
  { name: '消息通知', icon: 'bell', color: '#bce4fc', to: '/notifications' }
]

function go(entry) {
  router.push(entry.to).catch(() => {})
}
</script>

<template>
  <div class="entry-card">
    <div class="entry-grid">
      <div v-for="e in entries" :key="e.name" class="entry-item" @click="go(e)">
        <div class="entry-icon" :style="{ background: e.color + '1a', color: e.color }">
          <Icon :name="e.icon" />
        </div>
        <span class="entry-name">{{ e.name }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.entry-card {
  margin: 12px;
  background: var(--surface);
  border-radius: 16px;
  padding: 14px 8px;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px 4px;
}

.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.entry-icon {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.entry-name {
  font-size: 12px;
  color: var(--text);
}
</style>
