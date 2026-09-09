<template>
  <div class="auth-page">
    <van-nav-bar title="注册" left-arrow @click-left="onBack" />
    <div class="auth-form">
      <div class="auth-logo">
        <div class="logo-circle">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="#fff">
            <path d="M15 12a3 3 0 1 0-3-3 3 3 0 0 0 3 3zm-9 8c0-3 3-5 6-5s6 2 6 5v1H6zM12 2a5 5 0 1 0 0 10 5 5 0 0 0 0-10z"/>
          </svg>
        </div>
        <h2>加入旅游助手</h2>
        <p>注册账号，规划你的完美旅程</p>
      </div>

      <van-cell-group inset>
        <van-field
          v-model="form.username"
          label="用户名"
          placeholder="至少 3 个字符"
          clearable
        />
        <van-field
          v-model="form.password"
          type="password"
          label="密码"
          placeholder="至少 6 个字符"
          clearable
        />
        <van-field
          v-model="form.nickname"
          label="昵称"
          placeholder="选填，默认为用户名"
          clearable
        />
      </van-cell-group>

      <div class="auth-actions">
        <van-button type="primary" block round :loading="loading" @click="onRegister">
          注册
        </van-button>
        <div class="auth-link">
          已有账号？<span @click="goLogin">直接登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { register, login } from '../api/auth'

const router = useRouter()
const form = ref({ username: '', password: '', nickname: '' })
const loading = ref(false)

const onBack = () => router.back()

const onRegister = async () => {
  if (!form.value.username) return showToast('请输入用户名')
  if (form.value.username.length < 3) return showToast('用户名至少 3 个字符')
  if (!form.value.password) return showToast('请输入密码')
  if (form.value.password.length < 6) return showToast('密码至少 6 个字符')

  loading.value = true
  try {
    await register({
      username: form.value.username,
      password: form.value.password,
      nickname: form.value.nickname
    })
    showToast('注册成功，正在登录...')
    // 注册成功后自动登录
    await login({ username: form.value.username, password: form.value.password })
    router.replace('/profile')
  } catch (e) {
    showFailToast(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const goLogin = () => router.replace('/login')
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
  background: linear-gradient(135deg, #07c160, #13c2c2);
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
