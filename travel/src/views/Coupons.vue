<template>
  <div class="page">
    <van-nav-bar title="优惠券" left-arrow @click-left="onBack" />
    <div class="content">
      <div class="coupon-tabs">
        <span
          v-for="t in tabs"
          :key="t.value"
          class="tab"
          :class="{ active: active === t.value }"
          @click="active = t.value"
        >{{ t.label }}</span>
      </div>
      <van-empty image="coupon" :description="currentEmptyText" />
      <div class="tip">{{ currentTip }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const onBack = () => router.back()

const tabs = [
  { value: 'unused', label: '未使用', empty: '暂无可用优惠券', tip: '参与活动可获得优惠券' },
  { value: 'used', label: '已使用', empty: '暂无已使用的优惠券', tip: '使用过的优惠券会在这里留档' },
  { value: 'expired', label: '已过期', empty: '暂无已过期的优惠券', tip: '过期的优惠券会自动归档到此处' }
]

const active = ref('unused')
const current = computed(() => tabs.find((t) => t.value === active.value) || tabs[0])
const currentEmptyText = computed(() => current.value.empty)
const currentTip = computed(() => current.value.tip)
</script>

<style scoped>
.page { min-height: 100vh; background: var(--bg); }
.content { padding: 12px 16px 40px; }
.coupon-tabs {
  display: flex;
  gap: 24px;
  padding: 12px 0 16px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 16px;
}
.tab { font-size: 14px; color: var(--text-2); cursor: pointer; }
.tab.active { color: var(--brand-deep); font-weight: 600; position: relative; }
.tab.active::after {
  content: '';
  position: absolute;
  bottom: -13px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: var(--grad-brand);
  border-radius: 1px;
}
.tip { text-align: center; font-size: 13px; color: var(--text-2); }
</style>
