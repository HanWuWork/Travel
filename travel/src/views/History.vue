<template>
  <div class="page">
    <van-nav-bar title="浏览历史" left-arrow @click-left="onBack" />
    <div class="content">
      <van-empty image="search" description="暂无浏览记录" />
      <div class="tip">您的浏览记录将显示在这里</div>
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
.content { padding: 40px 16px; text-align: center; }
.tip { margin-top: 8px; font-size: 13px; color: #969799; }
</style>
