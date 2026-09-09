<template>
  <div class="page">
    <van-nav-bar title="优惠券" left-arrow @click-left="onBack" />
    <div class="content">
      <div class="coupon-tabs">
        <span class="tab active">未使用</span>
        <span class="tab">已使用</span>
        <span class="tab">已过期</span>
      </div>
      <van-empty image="coupon" description="暂无优惠券" />
      <div class="tip">参与活动可获得优惠券</div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const onBack = () => router.back()
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; }
.content { padding: 12px 16px 40px; }
.coupon-tabs {
  display: flex;
  gap: 24px;
  padding: 12px 0 16px;
  border-bottom: 1px solid #f2f3f5;
  margin-bottom: 16px;
}
.tab { font-size: 14px; color: #969799; }
.tab.active { color: #1989fa; font-weight: 600; position: relative; }
.tab.active::after {
  content: '';
  position: absolute;
  bottom: -13px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: #1989fa;
  border-radius: 1px;
}
.tip { text-align: center; font-size: 13px; color: #969799; }
</style>
