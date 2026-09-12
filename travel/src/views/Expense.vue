<script setup>
/**
 * 旅行记账本：总额/预算进度 + 分类饼图 + 按日柱状图 + 明细增删
 */
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Icon, Popup, showConfirmDialog, showToast } from 'vant'
import * as echarts from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { addExpense, deleteExpense, expenseStats, listExpenses } from '../api/expense'
import { listTrips } from '../api/trip'
import { isLoggedIn } from '../utils/auth'

echarts.use([PieChart, BarChart, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const CATEGORIES = [
  { name: '交通', color: '#0c0c0c' },
  { name: '住宿', color: '#2fe0a8' },
  { name: '餐饮', color: '#2fe0a8' },
  { name: '门票', color: '#04dc9c' },
  { name: '购物', color: '#e5484d' },
  { name: '其他', color: '#4cccf4' }
]

const trips = ref([])
const activeTripId = ref(null)
const stats = ref(null)
const items = ref([])
const showForm = ref(false)
const submitting = ref(false)
const pieEl = ref(null)
const barEl = ref(null)
let pieChart = null
let barChart = null

const form = ref({
  category: '餐饮',
  amount: '',
  note: '',
  expenseDate: new Date().toISOString().slice(0, 10)
})

onMounted(async () => {
  if (!isLoggedIn()) {
    showToast('请先登录')
    return
  }
  try {
    trips.value = await listTrips()
  } catch (e) {
    // 行程列表失败不影响记账
  }
  await load()
})

onBeforeUnmount(() => {
  pieChart && pieChart.dispose()
  barChart && barChart.dispose()
})

async function load() {
  try {
    const [s, list] = await Promise.all([
      expenseStats(activeTripId.value),
      listExpenses(activeTripId.value)
    ])
    stats.value = s
    items.value = list
    await nextTick()
    renderCharts()
  } catch (e) {
    showToast(e.message || '加载失败')
  }
}

function selectTrip(id) {
  activeTripId.value = id
  load()
}

function renderCharts() {
  // 分类饼图
  if (pieEl.value) {
    pieChart = pieChart || echarts.init(pieEl.value)
    const data = (stats.value?.byCategory || []).map((c) => ({
      name: c.category,
      value: c.amount,
      itemStyle: { color: c.color }
    }))
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
      series: [{
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        label: { formatter: '{b}\n{d}%', fontSize: 11 },
        data
      }]
    }, true)
  }
  // 按日柱状图
  if (barEl.value) {
    barChart = barChart || echarts.init(barEl.value)
    const days = stats.value?.byDate || []
    barChart.setOption({
      tooltip: { trigger: 'axis', formatter: '{b}\n¥{c}' },
      grid: { left: 44, right: 12, top: 16, bottom: 24 },
      xAxis: {
        type: 'category',
        data: days.map((d) => (d.date || '').slice(5)),
        axisLabel: { fontSize: 10 }
      },
      yAxis: { type: 'value', axisLabel: { fontSize: 10 } },
      series: [{
        type: 'bar',
        data: days.map((d) => d.amount),
        barMaxWidth: 26,
        itemStyle: { color: '#0c0c0c', borderRadius: [4, 4, 0, 0] }
      }]
    }, true)
  }
}

watch(showForm, (v) => {
  if (v && !form.value.amount) form.value.amount = ''
})

async function submit() {
  const amount = Number(form.value.amount)
  if (!amount || amount <= 0) return showToast('请输入有效金额')
  submitting.value = true
  try {
    await addExpense({
      tripId: activeTripId.value,
      category: form.value.category,
      amount,
      note: form.value.note.trim(),
      expenseDate: form.value.expenseDate
    })
    showForm.value = false
    form.value.amount = ''
    form.value.note = ''
    showToast('已记录')
    await load()
  } catch (e) {
    showToast(e.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function remove(item) {
  try {
    await showConfirmDialog({ title: '提示', message: `删除这笔 ¥${item.amount} 的记录？` })
    await deleteExpense(item.id)
    showToast('已删除')
    await load()
  } catch (e) {
    // 取消
  }
}

const budgetPercent = () => {
  if (!stats.value?.budget) return 0
  return Math.min(100, Math.round((stats.value.total / stats.value.budget) * 100))
}

const catColor = (name) => CATEGORIES.find((c) => c.name === name)?.color || '#8a8a80'
</script>

<template>
  <div class="expense-page">
    <van-nav-bar title="旅行记账" left-arrow @click-left="$router.back()" />

    <!-- 行程筛选 -->
    <div class="trip-bar">
      <span
        class="trip-chip"
        :class="{ 'trip-chip--active': activeTripId === null }"
        @click="selectTrip(null)"
      >全部</span>
      <span
        v-for="t in trips"
        :key="t.id"
        class="trip-chip"
        :class="{ 'trip-chip--active': activeTripId === t.id }"
        @click="selectTrip(t.id)"
      >{{ t.destination }}</span>
    </div>

    <template v-if="stats">
      <!-- 汇总 -->
      <div class="summary-card">
        <div class="summary-label">总支出</div>
        <div class="summary-total">¥{{ stats.total.toFixed(2) }}</div>
        <div class="summary-count">共 {{ stats.count }} 笔</div>

        <div v-if="stats.budget" class="budget-box">
          <div class="budget-row">
            <span>预算 ¥{{ stats.budget }}</span>
            <span :class="stats.overspend ? 'over' : 'remain'">
              {{ stats.overspend ? `超支 ¥${Math.abs(stats.remaining).toFixed(0)}` : `剩余 ¥${stats.remaining.toFixed(0)}` }}
            </span>
          </div>
          <div class="budget-bar">
            <div
              class="budget-bar__fill"
              :style="{ width: budgetPercent() + '%', background: stats.overspend ? '#e5484d' : '#04dc9c' }"
            ></div>
          </div>
        </div>
      </div>

      <!-- 图表 -->
      <div class="chart-card">
        <div class="card-title">分类占比</div>
        <div ref="pieEl" class="pie-chart"></div>
        <van-empty v-if="!stats.byCategory.length" description="暂无数据" image-size="56" />
      </div>

      <div class="chart-card">
        <div class="card-title">每日花费</div>
        <div ref="barEl" class="bar-chart"></div>
      </div>

      <!-- 明细 -->
      <div class="list-card">
        <div class="card-title">消费明细</div>
        <div v-for="it in items" :key="it.id" class="expense-item">
          <i class="dot" :style="{ background: catColor(it.category) }"></i>
          <div class="item-main">
            <div class="item-top">
              <span class="item-cat">{{ it.category }}</span>
              <span class="item-amount">-¥{{ it.amount.toFixed(2) }}</span>
            </div>
            <div class="item-sub">
              <span>{{ it.expenseDate }}</span>
              <span v-if="it.note">· {{ it.note }}</span>
            </div>
          </div>
          <Icon name="delete-o" class="item-del" @click="remove(it)" />
        </div>
        <van-empty v-if="!items.length" description="还没有记账，点右下角记一笔" image-size="56" />
      </div>
    </template>

    <div style="height: 80px"></div>

    <!-- 记一笔 -->
    <div class="fab" @click="showForm = true"><Icon name="plus" size="22" /></div>

    <Popup v-model:show="showForm" position="bottom" round :style="{ padding: '20px 16px 28px' }">
      <div class="form-title">记一笔</div>
      <div class="form-row form-row--wrap">
        <span class="form-label">分类</span>
        <div class="form-cats">
          <span
            v-for="c in CATEGORIES"
            :key="c.name"
            class="form-cat"
            :class="{ 'form-cat--active': form.category === c.name }"
            :style="form.category === c.name ? { background: c.color, borderColor: c.color, color: '#fff' } : {}"
            @click="form.category = c.name"
          >{{ c.name }}</span>
        </div>
      </div>
      <van-field v-model="form.amount" type="number" label="金额" placeholder="0.00" />
      <van-field v-model="form.expenseDate" label="日期" placeholder="yyyy-MM-dd" />
      <van-field v-model="form.note" label="备注" placeholder="如：晚餐火锅（可空）" maxlength="50" />
      <van-button type="primary" block round style="margin-top: 14px" :loading="submitting" @click="submit">
        保存
      </van-button>
    </Popup>
  </div>
</template>

<style scoped>
.expense-page {
  min-height: 100vh;
  background: var(--bg);
}

.trip-bar {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 12px;
}

.trip-chip {
  flex-shrink: 0;
  padding: 4px 12px;
  border-radius: 13px;
  background: var(--surface);
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text);
}

.trip-chip--active {
  background: var(--grad-brand);
  border-color: transparent;
  color: var(--brand-ink);
  font-weight: 600;
}

.summary-card {
  margin: 0 12px 12px;
  border-radius: 14px;
  padding: 18px;
  background: var(--grad-dark);
  color: #fff;
  position: relative;
}

.summary-label {
  font-size: 13px;
  opacity: 0.9;
}

.summary-total {
  font-size: 32px;
  font-weight: 700;
  margin: 4px 0;
}

.summary-count {
  font-size: 12px;
  opacity: 0.85;
}

.budget-box {
  margin-top: 14px;
}

.budget-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 6px;
}

.remain {
  color: #b7ffd0;
}

.over {
  color: #ffd0d0;
  font-weight: 600;
}

.budget-bar {
  height: 7px;
  background: rgba(255, 255, 255, 0.28);
  border-radius: 4px;
  overflow: hidden;
}

.budget-bar__fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.chart-card,
.list-card {
  margin: 0 12px 12px;
  background: var(--surface);
  border-radius: 16px;
  padding: 14px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.pie-chart {
  height: 200px;
}

.bar-chart {
  height: 170px;
}

.expense-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--line);
}

.expense-item:last-child {
  border-bottom: none;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.item-main {
  flex: 1;
  min-width: 0;
}

.item-top {
  display: flex;
  justify-content: space-between;
}

.item-cat {
  font-size: 14px;
  font-weight: 600;
}

.item-amount {
  font-size: 14px;
  font-weight: 600;
  color: var(--danger);
}

.item-sub {
  display: flex;
  gap: 4px;
  font-size: 12px;
  color: var(--text-2);
  margin-top: 2px;
}

.item-del {
  color: var(--text-2);
  font-size: 16px;
  flex-shrink: 0;
}

.fab {
  position: fixed;
  right: 20px;
  bottom: 34px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: var(--grad-brand);
  color: var(--brand-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(25, 137, 250, 0.4);
  z-index: 900;
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 6px;
}

.form-row--wrap {
  align-items: flex-start;
}

.form-label {
  font-size: 14px;
  color: var(--text-2);
  flex-shrink: 0;
  line-height: 28px;
}

.form-cats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.form-cat {
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 16px;
  background: var(--bg);
  border: 1px solid transparent;
  color: var(--text-2);
}
</style>
