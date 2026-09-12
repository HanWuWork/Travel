<template>
  <div class="favorites-page">
    <van-nav-bar title="我的收藏" left-arrow @click-left="onBack" />

    <div class="favorites-content">
      <div v-if="loading" class="state-wrap">
        <van-loading type="spinner">加载中...</van-loading>
      </div>

      <van-empty v-else-if="!favorites.length" description="暂无收藏" image="star" />

      <div v-else class="fav-list">
        <div v-for="fav in favorites" :key="fav.id" class="fav-card">
          <img v-if="fav.image" :src="fav.image" class="fav-img" />
          <div v-else class="fav-img fav-img--placeholder">
            <van-icon name="photo-o" size="28" color="#5a6165" />
          </div>
          <div class="fav-info">
            <div class="fav-title">{{ fav.title }}</div>
            <div v-if="fav.description" class="fav-desc">{{ fav.description }}</div>
            <div class="fav-meta">
              <span class="fav-type">{{ typeText(fav.targetType) }}</span>
              <span class="fav-time">{{ formatTime(fav.createTime) }}</span>
            </div>
          </div>
          <van-button size="mini" type="danger" plain @click="onDelete(fav)">取消</van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showFailToast } from 'vant'
import { listFavorites, deleteFavorite } from '../api/favorite'

const router = useRouter()
const favorites = ref([])
const loading = ref(false)

const onBack = () => router.back()

const loadFavorites = async () => {
  loading.value = true
  try {
    favorites.value = await listFavorites()
  } catch (e) {
    showFailToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onDelete = async (fav) => {
  try {
    await showConfirmDialog({ title: '提示', message: '取消收藏？' })
    await deleteFavorite(fav.id)
    showToast('已取消收藏')
    loadFavorites()
  } catch (e) {
    if (e !== 'cancel') showFailToast(e.message || '操作失败')
  }
}

const typeText = (t) => ({ spot: '景点', destination: '目的地', plan: '行程' }[t] || '收藏')

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: var(--bg);
}

.favorites-content {
  padding: 12px;
}

.state-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.fav-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fav-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--surface);
  border-radius: 10px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.fav-img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--line);
}

.fav-img--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.fav-info {
  flex: 1;
  min-width: 0;
}

.fav-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fav-desc {
  font-size: 12px;
  color: var(--text-2);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fav-meta {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: var(--text-2);
}

.fav-type {
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--tint);
  color: var(--brand-deep);
}
</style>
