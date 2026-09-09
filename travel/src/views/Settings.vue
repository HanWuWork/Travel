<template>
  <div class="page">
    <van-nav-bar title="设置" left-arrow @click-left="onBack" />
    <div class="content">
      <van-cell-group inset>
        <van-cell title="个人资料" icon="contact" is-link />
        <van-cell title="修改密码" icon="lock" is-link />
        <van-cell title="绑定手机号" icon="phone-o" is-link value="未绑定" />
      </van-cell-group>
      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="消息通知" icon="bell" is-link />
        <van-cell title="隐私设置" icon="shield-o" is-link />
        <van-cell title="清除缓存" icon="delete-o" is-link value="0.0 MB" />
      </van-cell-group>
      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="检查更新" icon="refresh" is-link value="v1.0.0" />
      </van-cell-group>
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
</style>
