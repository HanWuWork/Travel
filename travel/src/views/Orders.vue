<template>
  <div class="orders-page">
    <van-nav-bar title="我的订单" left-arrow @click-left="onBack" />

    <div class="orders-content">
      <div v-if="loading" class="state-wrap">
        <van-loading type="spinner">加载中...</van-loading>
      </div>

      <van-empty v-else-if="!orders.length" description="暂无订单" image="order" />

      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-dest">
              <van-icon name="location-o" /> {{ order.destination }}
            </span>
            <span class="order-status" :class="'status-' + order.status">
              {{ statusText(order.status) }}
            </span>
          </div>
          <div class="order-meta">
            <span>行程：{{ order.days }} 天</span>
            <span>预算：¥{{ order.budget }}</span>
          </div>
          <div class="order-footer">
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
            <van-button size="mini" type="danger" plain @click="onDelete(order)">删除</van-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showFailToast } from 'vant'
import { listOrders, deleteOrder } from '../api/order'

const router = useRouter()
const orders = ref([])
const loading = ref(false)

const onBack = () => router.back()

const loadOrders = async () => {
  loading.value = true
  try {
    orders.value = await listOrders()
  } catch (e) {
    showFailToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onDelete = async (order) => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定删除该订单吗？' })
    await deleteOrder(order.id)
    showToast('已删除')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') showFailToast(e.message || '删除失败')
  }
}

const statusText = (s) => ({ pending: '待出行', completed: '已完成', cancelled: '已取消' }[s] || s)

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: var(--bg);
}

.orders-content {
  padding: 12px;
}

.state-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  background: var(--surface);
  border-radius: 10px;
  padding: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-dest {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
  display: flex;
  align-items: center;
  gap: 4px;
}

.order-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.status-pending { background: rgba(76, 204, 244, 0.16); color: #4cccf4; }
.status-completed { background: rgba(4, 220, 156, 0.16); color: #04dc9c; }
.status-cancelled { background: var(--bg); color: var(--text-2); }

.order-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: var(--text-2);
  margin-bottom: 12px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--line);
}

.order-time {
  font-size: 12px;
  color: var(--text-2);
}
</style>
