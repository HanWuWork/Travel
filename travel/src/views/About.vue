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
        <van-cell title="应用介绍" icon="description-o" is-link @click="open('intro')" />
        <van-cell title="用户协议" icon="balance-o" is-link @click="open('terms')" />
        <van-cell title="隐私政策" icon="shield-o" is-link @click="open('privacy')" />
      </van-cell-group>

      <div class="footer-text">
        <p>AI旅游助手 — 智能旅游，尽在爱游</p>
        <p>© 2026 Travel Assistant. All rights reserved.</p>
      </div>
    </div>

    <ContentPopup v-model:show="popup.show" :title="popup.title" :items="popup.items" />
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import ContentPopup from '../components/ContentPopup.vue'

const router = useRouter()
const onBack = () => router.back()

const CONTENT = {
  intro: {
    title: '应用介绍',
    items: [
      { t: 'AI旅游助手是一款面向移动端的智能旅游规划应用：输入目的地、预算与天数，即可生成含每日安排、预算分配与地图路线的完整行程。' },
      { h: true, t: '主要能力' },
      { t: '· AI 行程规划与对话助手（支持流式输出、行程微调）' },
      { t: '· 目的地地图、景点库与周边探索' },
      { t: '· 足迹打卡、旅行记账、打包清单、天气与汇率工具' },
      { t: '· 游记社区、景点评价与多人协作行程' }
    ]
  },
  terms: {
    title: '用户协议',
    items: [
      { t: '欢迎使用 AI旅游助手。使用本应用即表示你已阅读并同意以下条款。' },
      { h: true, t: '一、服务内容' },
      { t: '本应用提供 AI 行程规划、目的地信息查询、旅行工具与社区内容等功能。AI 生成内容仅供参考，不构成任何形式的出行或消费承诺。' },
      { h: true, t: '二、用户行为规范' },
      { t: '请勿发布违法违规、侵权、虚假或骚扰性内容；不得利用本服务从事任何危害网络安全的行为。' },
      { h: true, t: '三、内容与知识产权' },
      { t: '用户发布的游记、评论等内容由用户自行负责，并授予本应用在本平台内展示的权利。' },
      { h: true, t: '四、免责声明' },
      { t: '第三方数据（天气、汇率等）仅供参考，实际以官方渠道为准；因使用本应用产生的出行风险由用户自行承担。' }
    ]
  },
  privacy: {
    title: '隐私政策',
    items: [
      { t: '我们重视你的隐私，本政策说明本应用会收集与使用哪些信息。' },
      { h: true, t: '一、收集的信息' },
      { t: '· 账号信息：注册时提供的用户名、昵称；密码经 BCrypt 加密存储。' },
      { t: '· 使用数据：你的收藏、足迹、行程、记账、打包清单等，用于提供对应功能。' },
      { t: '· 定位信息：仅在「周边探索」中经你授权后使用，用于查询附近地点。' },
      { h: true, t: '二、信息的使用与存储' },
      { t: '上述信息仅用于实现应用功能，不会出售或提供给第三方用于营销。' },
      { h: true, t: '三、你的权利' },
      { t: '你可以随时在「我的」中查看、修改个人资料，或删除收藏、行程、记账等记录。' },
      { h: true, t: '四、联系方式' },
      { t: '如对隐私政策有疑问，请联系 support@travel.com。' }
    ]
  }
}

const popup = reactive({ show: false, title: '', items: [] })

function open(key) {
  const c = CONTENT[key]
  popup.title = c.title
  popup.items = c.items
  popup.show = true
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--bg); }
.content { padding: 0 0 40px; }
.about-logo {
  text-align: center;
  padding: 40px 0 32px;
}
.logo-circle {
  width: 72px; height: 72px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: var(--grad-brand);
  display: flex; align-items: center; justify-content: center;
  color: var(--brand-ink);
}
.about-logo h3 { margin: 0; font-size: 20px; }
.version { margin: 6px 0 0; font-size: 13px; color: var(--text-2); }
.footer-text {
  text-align: center;
  margin-top: 40px;
  padding: 0 24px;
}
.footer-text p {
  margin: 4px 0;
  font-size: 12px;
  color: var(--text-2);
  line-height: 1.8;
}
</style>
