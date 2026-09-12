<script setup>
/**
 * 社区游记列表：最新/最热切换、城市与关键词筛选、瀑布流卡片（上拉分页加载）
 */
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, List, Search, showToast } from 'vant'
import { listPosts } from '../api/social'

const HOT_CITIES = ['北京', '上海', '杭州', '成都', '西安', '厦门']
const PAGE_SIZE = 10

const router = useRouter()

const posts = ref([])
const sort = ref('new')
const city = ref('')
const keyword = ref('')
const loading = ref(false)
const finished = ref(false)
const page = ref(0)
// 筛选条件变化时递增，强制重建 List 组件以重新加载
const queryKey = ref(0)

async function onLoad() {
  try {
    const data = await listPosts({
      sort: sort.value,
      city: city.value || undefined,
      keyword: keyword.value.trim() || undefined,
      page: page.value,
      size: PAGE_SIZE
    })
    const list = data.list || []
    posts.value = page.value === 0 ? list : posts.value.concat(list)
    page.value += 1
    finished.value = !data.hasMore
  } catch (e) {
    showToast(e.message || '加载失败')
    finished.value = true
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 0
  posts.value = []
  finished.value = false
  loading.value = false
  queryKey.value += 1
}

function switchSort(s) {
  sort.value = s
  reload()
}

function pickCity(c) {
  city.value = city.value === c ? '' : c
  reload()
}

function goDetail(p) {
  router.push(`/posts/${p.id}`)
}

const empty = computed(() => !loading.value && finished.value && !posts.value.length)
</script>

<template>
  <div class="posts-page">
    <van-nav-bar title="社区游记" left-arrow @click-left="$router.back()">
      <template #right>
        <Icon name="edit" size="18" @click="router.push('/posts/publish')" />
      </template>
    </van-nav-bar>

    <!-- 搜索 + 排序 -->
    <div class="toolbar">
      <Search v-model="keyword" placeholder="搜索游记标题/内容" @search="reload" />
      <div class="sort-tabs">
        <span
          class="sort-tab"
          :class="{ 'sort-tab--active': sort === 'new' }"
          @click="switchSort('new')"
        >最新</span>
        <span
          class="sort-tab"
          :class="{ 'sort-tab--active': sort === 'hot' }"
          @click="switchSort('hot')"
        >最热</span>
      </div>
    </div>

    <!-- 城市筛选 -->
    <div class="city-bar">
      <span
        v-for="c in HOT_CITIES"
        :key="c"
        class="city-chip"
        :class="{ 'city-chip--active': city === c }"
        @click="pickCity(c)"
      >{{ c }}</span>
    </div>

    <!-- 列表（上拉分页加载） -->
    <van-empty v-if="empty" description="还没有游记，来发布第一篇吧" />
    <van-list
      v-else
      :key="queryKey"
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      class="post-list"
      @load="onLoad"
    >
      <div v-for="p in posts" :key="p.id" class="post-card" @click="goDetail(p)">
        <div class="post-card__head">
          <div class="avatar">
            <van-image round width="32" height="32" :src="p.authorAvatar">
              <template #error><Icon name="user-o" /></template>
            </van-image>
          </div>
          <span class="author">{{ p.authorName || '旅行者' }}</span>
          <span v-if="p.city" class="post-city"><Icon name="location-o" /> {{ p.city }}</span>
        </div>
        <div class="post-card__title">{{ p.title }}</div>
        <div class="post-card__summary">{{ p.summary }}</div>
        <div v-if="p.tagList && p.tagList.length" class="post-tags">
          <span v-for="t in p.tagList" :key="t" class="post-tag">#{{ t }}</span>
        </div>
        <div class="post-card__foot">
          <span class="foot-time">{{ p.createTime }}</span>
          <div class="foot-stats">
            <span><Icon name="eye-o" /> {{ p.viewCount }}</span>
            <span :class="{ liked: p.liked }"><Icon :name="p.liked ? 'like' : 'like-o'" /> {{ p.likeCount }}</span>
            <span><Icon name="chat-o" /> {{ p.commentCount }}</span>
          </div>
        </div>
      </div>
    </van-list>

    <div style="height: 40px"></div>
  </div>
</template>

<style scoped>
.posts-page {
  min-height: 100vh;
  background: var(--bg);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px 0;
}

.toolbar :deep(.van-search) {
  flex: 1;
  padding: 0;
  background: transparent;
}

.sort-tabs {
  display: flex;
  background: var(--surface);
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid var(--line);
  flex-shrink: 0;
}

.sort-tab {
  padding: 5px 12px;
  font-size: 12px;
  color: var(--text-2);
}

.sort-tab--active {
  background: #0e7490;
  color: #fff;
  font-weight: 600;
}

.city-bar {
  display: flex;
  gap: 8px;
  padding: 10px 12px;
  overflow-x: auto;
}

.city-chip {
  flex-shrink: 0;
  padding: 3px 12px;
  border-radius: 16px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text);
}

.city-chip--active {
  background: var(--ink);
  border-color: var(--text);
  color: #fff;
}

.loading-wrap {
  padding: 60px 0;
  text-align: center;
}

.post-list {
  padding: 0 12px;
}

.post-card {
  background: var(--surface);
  border-radius: 16px;
  padding: 14px;
  margin-bottom: 12px;
}

.post-card__head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--line);
  color: var(--text-2);
  overflow: hidden;
}

.author {
  font-size: 13px;
  color: var(--text);
}

.post-city {
  margin-left: auto;
  font-size: 12px;
  color: var(--brand-deep);
}

.post-card__title {
  font-size: 16px;
  font-weight: 600;
  margin-top: 10px;
}

.post-card__summary {
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.6;
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.post-tag {
  font-size: 12px;
  color: var(--brand-deep);
}

.post-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid var(--line);
}

.foot-time {
  font-size: 11px;
  color: var(--text-2);
}

.foot-stats {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--text-2);
}

.foot-stats .liked {
  color: var(--danger);
}
</style>
