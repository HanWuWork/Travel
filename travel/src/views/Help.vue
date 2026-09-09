<template>
  <div class="page">
    <van-nav-bar title="帮助与反馈" left-arrow @click-left="onBack" />
    <div class="content">
      <van-cell-group inset>
        <van-cell title="常见问题" icon="question-o" is-link />
        <van-cell title="使用指南" icon="book-o" is-link />
        <van-cell title="意见反馈" icon="edit" is-link />
        <van-cell title="举报投诉" icon="warning-o" is-link />
      </van-cell-group>
      <div class="contact-card">
        <div class="contact-title">联系我们</div>
        <div class="contact-item">
          <van-icon name="phone-o" size="20" color="#1989fa" />
          <span>客服热线：400-xxx-xxxx</span>
        </div>
        <div class="contact-item">
          <van-icon name="envelop-o" size="20" color="#1989fa" />
          <span>邮箱：support@travel.com</span>
        </div>
        <div class="contact-item">
          <van-icon name="clock-o" size="20" color="#1989fa" />
          <span>服务时间：09:00 - 18:00</span>
        </div>
      </div>
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
.content { padding: 12px 0 40px; }
.contact-card {
  margin: 16px 16px 0;
  background: #fff;
  border-radius: 10px;
  padding: 20px 16px;
}
.contact-title { font-size: 15px; font-weight: 600; margin-bottom: 16px; }
.contact-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 0; font-size: 14px; color: #646566;
}
</style>
