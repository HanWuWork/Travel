<script setup>
/**
 * 打包清单：模板一键生成、分组勾选、进度统计
 */
import { onMounted, ref } from 'vue'
import { Icon, Popup, showConfirmDialog, showToast } from 'vant'
import {
  addPackingItem,
  applyPackingTemplate,
  clearPackingList,
  deletePackingItem,
  packingList,
  togglePackingItem
} from '../api/packing'
import { listTrips } from '../api/trip'
import { isLoggedIn } from '../utils/auth'

const CATEGORIES = ['证件', '衣物', '电子', '洗漱', '药品', '其他']

const trips = ref([])
const tripId = ref(null)
const summary = ref(null)
const showAdd = ref(false)
const adding = ref(false)
const newItem = ref({ name: '', category: '其他' })

onMounted(async () => {
  if (!isLoggedIn()) {
    showToast('请先登录')
    return
  }
  try {
    trips.value = await listTrips()
  } catch (e) {
    // ignore
  }
  await load()
})

async function load() {
  try {
    summary.value = await packingList(tripId.value)
  } catch (e) {
    showToast(e.message || '加载失败')
  }
}

function selectTrip(id) {
  tripId.value = id
  load()
}

async function useTemplate() {
  try {
    const res = await applyPackingTemplate(tripId.value)
    showToast(res.added > 0 ? `已添加 ${res.added} 项` : '模板物品已齐全')
    await load()
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

async function toggle(item) {
  try {
    await togglePackingItem(item.id)
    // 本地即时反馈，避免整页刷新
    item.packed = !item.packed
    recalc()
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

function recalc() {
  const groups = summary.value?.groups || []
  const all = groups.flatMap((g) => g.items)
  const packed = all.filter((i) => i.packed).length
  summary.value.total = all.length
  summary.value.packed = packed
  summary.value.percent = all.length ? Math.round((packed * 100) / all.length) : 0
}

async function addItem() {
  if (!newItem.value.name.trim()) return showToast('请输入物品名称')
  adding.value = true
  try {
    await addPackingItem({
      tripId: tripId.value,
      name: newItem.value.name.trim(),
      category: newItem.value.category
    })
    showAdd.value = false
    newItem.value = { name: '', category: '其他' }
    await load()
  } catch (e) {
    showToast(e.message || '添加失败')
  } finally {
    adding.value = false
  }
}

async function removeItem(item) {
  try {
    await deletePackingItem(item.id)
    await load()
  } catch (e) {
    showToast(e.message || '删除失败')
  }
}

async function clearAll() {
  try {
    await showConfirmDialog({ title: '提示', message: '确定清空当前清单吗？' })
    await clearPackingList(tripId.value)
    showToast('已清空')
    await load()
  } catch (e) {
    // 取消
  }
}
</script>

<template>
  <div class="packing-page">
    <van-nav-bar title="打包清单" left-arrow @click-left="$router.back()">
      <template #right>
        <Icon name="delete-o" size="18" @click="clearAll" />
      </template>
    </van-nav-bar>

    <!-- 行程筛选 -->
    <div class="trip-bar">
      <span class="trip-chip" :class="{ 'trip-chip--active': tripId === null }" @click="selectTrip(null)">通用</span>
      <span
        v-for="t in trips"
        :key="t.id"
        class="trip-chip"
        :class="{ 'trip-chip--active': tripId === t.id }"
        @click="selectTrip(t.id)"
      >{{ t.destination }}</span>
    </div>

    <!-- 进度 -->
    <div v-if="summary" class="progress-card">
      <div class="progress-head">
        <span>打包进度</span>
        <span class="progress-num">{{ summary.packed }}/{{ summary.total }}</span>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: summary.percent + '%' }"></div>
      </div>
      <div class="progress-percent">{{ summary.percent }}%</div>
    </div>

    <!-- 空状态 -->
    <div v-if="summary && !summary.total" class="empty-wrap">
      <van-empty description="清单还是空的" image-size="70" />
      <van-button type="primary" round @click="useTemplate">一键生成模板清单</van-button>
    </div>

    <!-- 分组清单 -->
    <div v-else-if="summary" class="groups">
      <div v-for="g in summary.groups" :key="g.category" class="group">
        <div class="group-title">{{ g.category }}</div>
        <div
          v-for="it in g.items"
          :key="it.id"
          class="pack-item"
          :class="{ 'pack-item--done': it.packed }"
          @click="toggle(it)"
        >
          <Icon :name="it.packed ? 'checked' : 'circle'" :color="it.packed ? '#04dc9c' : '#5a6165'" size="18" />
          <span class="pack-name">{{ it.name }}</span>
          <Icon name="cross" class="pack-del" @click.stop="removeItem(it)" />
        </div>
      </div>
    </div>

    <div style="height: 90px"></div>

    <!-- 操作 -->
    <div class="bottom-actions">
      <van-button plain round size="small" @click="useTemplate">套用模板</van-button>
      <van-button type="primary" round size="small" @click="showAdd = true">添加物品</van-button>
    </div>

    <Popup v-model:show="showAdd" position="bottom" round :style="{ padding: '20px 16px 28px' }">
      <div class="form-title">添加物品</div>
      <van-field v-model="newItem.name" label="名称" placeholder="如：防晒霜" maxlength="20" />
      <div class="form-row">
        <span class="form-label">分类</span>
        <div class="form-cats">
          <span
            v-for="c in CATEGORIES"
            :key="c"
            class="form-cat"
            :class="{ 'form-cat--active': newItem.category === c }"
            @click="newItem.category = c"
          >{{ c }}</span>
        </div>
      </div>
      <van-button type="primary" block round style="margin-top: 14px" :loading="adding" @click="addItem">添加</van-button>
    </Popup>
  </div>
</template>

<style scoped>
.packing-page {
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

.progress-card {
  margin: 0 12px 12px;
  background: var(--surface);
  border-radius: 16px;
  padding: 14px;
  position: relative;
}

.progress-head {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-2);
  margin-bottom: 8px;
}

.progress-num {
  font-weight: 600;
  color: var(--text);
}

.progress-bar {
  height: 8px;
  background: var(--line);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--grad-brand);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-percent {
  text-align: right;
  font-size: 12px;
  color: var(--success);
  font-weight: 600;
  margin-top: 6px;
}

.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 30px 0;
}

.groups {
  padding: 0 12px;
}

.group {
  background: var(--surface);
  border-radius: 16px;
  padding: 6px 14px;
  margin-bottom: 12px;
}

.group-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--brand-deep);
  padding: 8px 0 4px;
}

.pack-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 0;
  border-top: 1px solid var(--line);
}

.pack-name {
  flex: 1;
  font-size: 14px;
  color: var(--text);
}

.pack-item--done .pack-name {
  color: var(--text-2);
  text-decoration: line-through;
}

.pack-del {
  color: var(--text-4);
  font-size: 14px;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 750px;
  background: var(--surface);
  border-top: 1px solid var(--line);
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  display: flex;
  gap: 12px;
  justify-content: flex-end;
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
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.form-label {
  font-size: 14px;
  color: var(--text-2);
  flex-shrink: 0;
}

.form-cats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.form-cat {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 16px;
  background: var(--bg);
  color: var(--text-2);
}

.form-cat--active {
  background: #0e7490;
  color: #fff;
}
</style>
