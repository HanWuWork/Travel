<template>
  <div class="auth-page">
    <van-nav-bar title="登录" left-arrow @click-left="onBack" />
    <div class="auth-form">
      <div class="auth-logo">
        <div class="logo-circle">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="#fff">
            <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z"/>
          </svg>
        </div>
        <h2>旅游助手</h2>
        <p>登录开启你的旅程</p>
      </div>

      <van-cell-group inset>
        <van-field
          v-model="form.username"
          label="用户名"
          placeholder="请输入用户名"
          clearable
        />
        <van-field
          v-model="form.password"
          type="password"
          label="密码"
          placeholder="请输入密码"
          clearable
        />
      </van-cell-group>

      <div class="auth-actions">
        <van-button type="primary" block round :loading="loading" @click="onLogin">
          登录
        </van-button>
        <div class="auth-link">
          还没有账号？<span @click="goRegister">立即注册</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { login } from '../api/auth'
import { isLoggedIn } from '../utils/auth'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const onBack = () => {
  if (isLoggedIn()) router.push('/profile')
  else router.push('/')
}

const onLogin = async () => {
  if (!form.value.username) return showToast('请输入用户名')
  if (!form.value.password) return showToast('请输入密码')

  loading.value = true
  try {
    await login({ username: form.value.username, password: form.value.password })
    showToast('登录成功')
    router.replace('/profile')
  } catch (e) {
    showFailToast(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  router.replace('/register')
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: #fff;
}

.auth-form {
  padding: 24px 16px;
}

.auth-logo {
  text-align: center;
  padding: 24px 0 32px;
}

.logo-circle {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-logo h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #323233;
}

.auth-logo p {
  margin: 0;
  font-size: 13px;
  color: #969799;
}

.auth-actions {
  margin-top: 24px;
  padding: 0 16px;
}

.auth-link {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: #646566;
}

.auth-link span {
  color: #1989fa;
  cursor: pointer;
}
</style>
