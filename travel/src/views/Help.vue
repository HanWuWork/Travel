<template>
  <div class="page">
    <van-nav-bar title="帮助与反馈" left-arrow @click-left="onBack" />
    <div class="content">
      <van-cell-group inset>
        <van-cell title="常见问题" icon="question-o" is-link @click="open('faq')" />
        <van-cell title="使用指南" icon="book-o" is-link @click="open('guide')" />
        <van-cell title="意见反馈" icon="edit" is-link @click="open('feedback')" />
        <van-cell title="举报投诉" icon="warning-o" is-link @click="open('report')" />
      </van-cell-group>
      <div class="contact-card">
        <div class="contact-title">联系我们</div>
        <div class="contact-item">
          <Icon name="phone-o" size="20" color="var(--text-2)" />
          <span>客服热线：400-xxx-xxxx</span>
        </div>
        <div class="contact-item" @click="copyEmail">
          <Icon name="envelop-o" size="20" color="var(--text-2)" />
          <span>邮箱：support@travel.com（点击复制）</span>
        </div>
        <div class="contact-item">
          <Icon name="clock-o" size="20" color="var(--text-2)" />
          <span>服务时间：09:00 - 18:00</span>
        </div>
      </div>
    </div>

    <ContentPopup v-model:show="popup.show" :title="popup.title" :items="popup.items" />
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, showToast } from 'vant'
import ContentPopup from '../components/ContentPopup.vue'

const router = useRouter()
const onBack = () => router.back()

const EMAIL = 'support@travel.com'

const CONTENT = {
  faq: {
    title: '常见问题',
    items: [
      { h: true, t: '如何生成行程？' },
      { t: '在主页填写「目的地 / 预算 / 天数」，点击「开始规划」，AI 会生成逐日行程与预算分配，可在地图模式下查看路线。' },
      { h: true, t: '生成的行程能保存吗？' },
      { t: '可以。登录后打开行程页，点右上角「保存行程」，之后在「我的 - 我的行程」中随时查看。' },
      { h: true, t: '怎样和朋友一起编辑行程？' },
      { t: '打开已保存的行程 → 右上角协作图标 → 复制邀请链接发给好友，对方加入后即可共同编辑。' },
      { h: true, t: 'AI 对话可以问什么？' },
      { t: '景点推荐、行程规划、美食住宿、出行季节、预算控制等旅行相关问题都可以问。' }
    ]
  },
  guide: {
    title: '使用指南',
    items: [
      { h: true, t: '1. 规划行程' },
      { t: '主页填写目的地、预算、天数即可生成 AI 行程；生成后支持「AI 微调」，用一句话调整（例如「第二天太赶了，改轻松点」）。' },
      { h: true, t: '2. 目的地与地图' },
      { t: '「地图」页可按城市查看景点/美食/住宿分布，点击标记查看详情；「目的地库」提供城市与景点资料。' },
      { h: true, t: '3. 旅行工具' },
      { t: '提供旅行记账（含预算进度）、打包清单、目的地天气、汇率换算、足迹打卡等工具。' },
      { h: true, t: '4. 社区与记录' },
      { t: '可以发布游记、点赞评论；在景点详情页写评价，或用足迹地图记录去过/想去的城市。' }
    ]
  },
  feedback: {
    title: '意见反馈',
    items: [
      { t: `感谢你的建议！请发送邮件至 ${EMAIL}，或拨打客服热线 400-xxx-xxxx。` },
      { t: '为便于定位问题，建议附上：使用的页面、操作步骤、期望结果与实际结果（可截图）。' },
      { t: '我们会认真阅读每一条反馈，并在后续版本中持续改进。' }
    ]
  },
  report: {
    title: '举报投诉',
    items: [
      { t: `如发现违规内容（虚假攻略、侵权信息、骚扰评论等），请邮件举报至 ${EMAIL}，标题注明「举报投诉」。` },
      { t: '请提供被举报内容的页面链接或截图、以及你的联系方式，我们会在 1-2 个工作日内核查处理。' }
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

async function copyEmail() {
  try {
    await navigator.clipboard.writeText(EMAIL)
    showToast('邮箱已复制')
  } catch (e) {
    showToast(`邮箱：${EMAIL}`)
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--bg); }
.content { padding: 12px 0 40px; }
.contact-card {
  margin: 16px 16px 0;
  background: var(--surface);
  border-radius: 16px;
  padding: 20px 16px;
}
.contact-title { font-size: 15px; font-weight: 600; margin-bottom: 16px; }
.contact-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 0; font-size: 14px; color: var(--text-2);
}
</style>
