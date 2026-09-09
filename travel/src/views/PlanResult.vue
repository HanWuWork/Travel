<template>
  <div class="plan-result">
    <van-nav-bar
      title="行程规划"
      left-arrow
      @click-left="onBack"
    />

    <div class="plan-content">
      <!-- 加载中 -->
      <div v-if="loading" class="state-wrap">
        <van-loading type="spinner" size="32" color="#1989fa">正在为你生成行程规划...</van-loading>
      </div>

      <!-- 错误 -->
      <div v-else-if="error" class="state-wrap">
        <van-empty image="error" :description="error" />
        <van-button round type="primary" @click="fetchPlan">重新生成</van-button>
      </div>

      <!-- 规划结果 -->
      <template v-else-if="plan">
        <!-- 行程概要 -->
        <Card class="summary-card">
          <template #title>
            <div class="card-title">
              <Icon name="todo-list-o" />
              <span>行程概要</span>
            </div>
          </template>
          <template #desc>
            <div class="summary-info">
              <div class="info-row">
                <span class="info-label">目的地</span>
                <span class="info-value highlight">{{ plan.destination || '未设置' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">预算</span>
                <span class="info-value">¥{{ plan.budget || 0 }} 元</span>
              </div>
              <div class="info-row">
                <span class="info-label">天数</span>
                <span class="info-value">{{ plan.days || 0 }} 天</span>
              </div>
            </div>
          </template>
        </Card>

        <!-- 推荐行程 -->
        <Card class="itinerary-card">
          <template #title>
            <div class="card-title">
              <Icon name="clock-o" />
              <span>推荐行程</span>
            </div>
          </template>
          <template #desc>
            <div class="itinerary-list">
              <div
                v-for="(item, index) in plan.itinerary"
                :key="index"
                class="itinerary-item"
              >
                <div class="day-badge">第{{ item.day }}天</div>
                <div class="day-content">
                  <div class="day-title">{{ item.title }}</div>
                  <div class="day-desc">{{ item.description }}</div>
                  <div class="day-tip" v-if="item.tip">
                    <Icon name="info-o" /> {{ item.tip }}
                  </div>
                </div>
              </div>
            </div>
          </template>
        </Card>

        <!-- 预算分配 -->
        <Card class="budget-card">
          <template #title>
            <div class="card-title">
              <Icon name="gold-coin-o" />
              <span>预算分配</span>
            </div>
          </template>
          <template #desc>
            <div class="budget-list">
              <div
                v-for="(item, index) in plan.budgetBreakdown"
                :key="index"
                class="budget-item"
              >
                <span class="budget-label">{{ item.label }}</span>
                <div class="budget-bar-wrap">
                  <div class="budget-bar" :style="{ width: item.percent + '%', background: item.color }"></div>
                </div>
                <span class="budget-amount">¥{{ item.amount }}</span>
              </div>
            </div>
          </template>
        </Card>

        <!-- 温馨提示 -->
        <Card class="tips-card">
          <template #title>
            <div class="card-title">
              <Icon name="warning-o" />
              <span>温馨提示</span>
            </div>
          </template>
          <template #desc>
            <div class="tips-list">
              <div v-for="(tip, index) in plan.tips" :key="index" class="tip-item">
                <span class="tip-dot"></span>
                <span>{{ tip }}</span>
              </div>
            </div>
          </template>
        </Card>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Card, Icon } from 'vant'
import { createTravelPlan } from '../api/plan'

const route = useRoute()
const router = useRouter()

const plan = ref(null)
const loading = ref(false)
const error = ref('')

const destination = () => route.query.destination || ''
const budget = () => parseInt(route.query.budget) || 0
const days = () => parseInt(route.query.days) || 0

const onBack = () => {
  router.push('/')
}

const fetchPlan = async () => {
  const dest = destination()
  const bud = budget()
  const d = days()

  if (!dest || !bud || !d) {
    error.value = '缺少规划参数，请返回主页重新填写'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const data = await createTravelPlan({
      destination: dest,
      budget: bud,
      days: d
    })
    plan.value = data
  } catch (e) {
    error.value = e.message || '生成行程失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(fetchPlan)
</script>

<style scoped>
.plan-result {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.plan-content {
  padding: 12px;
  padding-bottom: 30px;
}

.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 80px 24px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
}

.summary-card,
.itinerary-card,
.budget-card,
.tips-card {
  margin-bottom: 12px;
}

.summary-info {
  padding: 8px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f3f5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #969799;
  font-size: 14px;
}

.info-value {
  font-size: 15px;
  font-weight: 500;
}

.highlight {
  color: #1989fa;
  font-size: 16px;
  font-weight: 600;
}

.itinerary-list {
  padding: 8px 0;
}

.itinerary-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f2f3f5;
}

.itinerary-item:last-child {
  border-bottom: none;
}

.day-badge {
  flex-shrink: 0;
  width: 56px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  background: linear-gradient(135deg, #1989fa, #0079ff);
  color: white;
  font-size: 13px;
  border-radius: 14px;
}

.day-content {
  flex: 1;
  min-width: 0;
}

.day-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.day-desc {
  font-size: 13px;
  color: #646566;
  margin-bottom: 6px;
}

.day-tip {
  font-size: 12px;
  color: #ff9800;
  display: flex;
  align-items: center;
  gap: 4px;
}

.budget-list {
  padding: 8px 0;
}

.budget-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.budget-label {
  width: 50px;
  font-size: 13px;
  color: #646566;
  flex-shrink: 0;
}

.budget-bar-wrap {
  flex: 1;
  height: 10px;
  background-color: #f2f3f5;
  border-radius: 5px;
  overflow: hidden;
}

.budget-bar {
  height: 100%;
  border-radius: 5px;
  transition: width 0.3s ease;
}

.budget-amount {
  width: 70px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #ee0a24;
  flex-shrink: 0;
}

.tips-list {
  padding: 8px 0;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
  color: #646566;
  line-height: 1.6;
}

.tip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #1989fa;
  margin-top: 7px;
  flex-shrink: 0;
}
</style>
