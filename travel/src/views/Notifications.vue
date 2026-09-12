<script setup>
/**
 * 消息通知中心：点赞/评论/协作/系统通知，支持已读、删除、清空
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, showConfirmDialog, showToast } from 'vant'
import {
  clearNotifications,
  deleteNotification,
  listNotifications,
  markAllRead,
  markRead
} from '../api/notify'
import { isLoggedIn } from '../utils/auth'

const router = useRouter()

const TYPE_STYLE = {
  like: { icon: 'like-o', color: '#e5484d' },
  comment: { icon: 'chat-o', color: '#0c0c0c' },
  collab: { icon: 'friends-o', color: '#4cccf4' },
  system: { icon: 'volume-o', color: '#4cccf4' }
}

const list = ref([])
const unreadOnly = ref(false)
const loading = ref(true)

onMounted(load)

async function load() {
  if (!isLoggedIn()) {
    showToast('请先登录')
    router.replace('/login')
    return
  }
  loading.value = true
  try {
    list.value = await listNotifications(unreadOnly.value)
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function switchFilter(v) {
  unreadOnly.value = v
  load()
}

async function open(n) {
  try {
    if (!n.read) {
      await markRead(n.id)
      n.read = true
    }
  } catch (e) {
    // 忽略
  }
  if (n.link) {
    if (n.link.startsWith('/')) router.push(n.link)
    else window.open(n.link, '_blank')
  }
}

async function remove(n) {
  try {
    await deleteNotification(n.id)
    list.value = list.value.filter((x) => x.id !== n.id)
    showToast('已删除')
  } catch (e) {
    showToast(e.message || '删除失败')
  }
}

async function readAll() {
  try {
    await markAllRead()
    list.value.forEach((n) => (n.read = true))
    showToast('已全部标为已读')
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

async function clearAll() {
  try {
    await showConfirmDialog({ title: '提示', message: '确定清空所有通知吗？' })
    await clearNotifications()
    list.value = []
    showToast('已清空')
  } catch (e) {
    // 取消
  }
}

function styleOf(type) {
  return TYPE_STYLE[type] || TYPE_STYLE.system
}
</script>

<template>
  <div class="notify-page">
    <van-nav-bar title="消息通知" left-arrow @click-left="$router.back()">
      <template #right>
        <Icon name="clear" size="18" @click="clearAll" />
      </template>
    </van-nav-bar>

    <!-- 筛选 -->
    <div class="filter-bar">
      <span class="filter-chip" :class="{ 'filter-chip--active': !unreadOnly }" @click="switchFilter(false)">全部</span>
      <span class="filter-chip" :class="{ 'filter-chip--active': unreadOnly }" @click="switchFilter(true)">未读</span>
      <div class="spacer"></div>
      <span class="read-all" @click="readAll">全部已读</span>
    </div>

    <van-loading v-if="loading" class="loading-wrap" size="26" vertical>加载中...</van-loading>
    <van-empty v-else-if="!list.length" description="暂无通知" />

    <div v-else class="notify-list">
      <div v-for="n in list" :key="n.id" class="notify-item" :class="{ 'notify-item--read': n.read }" @click="open(n)">
        <div class="notify-icon" :style="{ background: styleOf(n.type).color + '1a', color: styleOf(n.type).color }">
          <Icon :name="styleOf(n.type).icon" />
        </div>
        <div class="notify-main">
          <div class="notify-title">
            <span v-if="!n.read" class="unread-dot"></span>{{ n.title }}
            <span class="type-tag">{{ n.typeLabel }}</span>
          </div>
          <div v-if="n.content" class="notify-content">{{ n.content }}</div>
          <div class="notify-time">{{ n.fromName ? n.fromName + ' · ' : '' }}{{ n.createTime }}</div>
        </div>
        <Icon name="cross" class="notify-del" @click.stop="remove(n)" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.notify-page {
  min-height: 100vh;
  background: var(--bg);
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
}

.filter-chip {
  padding: 3px 14px;
  border-radius: 13px;
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

.spacer {
  flex: 1;
}

.read-all {
  font-size: 12px;
  color: var(--brand-deep);
}

.loading-wrap {
  padding: 60px 0;
  text-align: center;
}

.notify-list {
  padding: 0 12px;
}

.notify-item {
  display: flex;
  gap: 10px;
  background: var(--surface);
  border-radius: 16px;
  padding: 14px;
  margin-bottom: 10px;
}

.notify-item--read {
  opacity: 0.72;
}

.notify-icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  flex-shrink: 0;
}

.notify-main {
  flex: 1;
  min-width: 0;
}

.notify-title {
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.unread-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #d81b3f;
  flex-shrink: 0;
}

.type-tag {
  font-size: 10px;
  color: var(--text-2);
  background: var(--bg);
  border-radius: 8px;
  padding: 1px 6px;
  font-weight: 400;
}

.notify-content {
  font-size: 13px;
  color: var(--text-2);
  margin-top: 4px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notify-time {
  font-size: 11px;
  color: var(--text-2);
  margin-top: 6px;
}

.notify-del {
  color: var(--text-4);
  font-size: 14px;
  flex-shrink: 0;
}
</style>
