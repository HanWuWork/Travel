<template>
  <div class="page">
    <van-nav-bar title="关于我们" left-arrow @click-left="onBack" />
    <div class="content">
      <div class="about-logo">
        <div class="logo-circle">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="#fff">
            <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z"/>
          </svg>
        </div>
        <h3>AI旅游助手</h3>
        <p class="version">v1.0.0</p>
      </div>

      <van-cell-group inset>
        <van-cell title="应用介绍" icon="description-o" is-link />
        <van-cell title="用户协议" icon="balance-o" is-link />
        <van-cell title="隐私政策" icon="shield-o" is-link />
      </van-cell-group>

      <div class="footer-text">
        <p>AI旅游助手 — 让每次旅行更简单</p>
        <p>© 2026 Travel Assistant. All rights reserved.</p>
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
.content { padding: 0 0 40px; }
.about-logo {
  text-align: center;
  padding: 40px 0 32px;
}
.logo-circle {
  width: 72px; height: 72px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex; align-items: center; justify-content: center;
}
.about-logo h3 { margin: 0; font-size: 20px; }
.version { margin: 6px 0 0; font-size: 13px; color: #969799; }
.footer-text {
  text-align: center;
  margin-top: 40px;
  padding: 0 24px;
}
.footer-text p {
  margin: 4px 0;
  font-size: 12px;
  color: #c8c9cc;
  line-height: 1.8;
}
</style>
