<script setup>
/**
 * 协作邀请落地页：展示行程概要，登录后一键加入
 */
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon, showToast } from 'vant'
import { collabInfo, joinCollab } from '../api/collab'
import { isLoggedIn } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const info = ref(null)
const error = ref('')
const joining = ref(false)

onMounted(async () => {
  try {
    info.value = await collabInfo(route.params.code)
  } catch (e) {
    error.value = e.message || '邀请无效'
  }
})

async function join() {
  if (!isLoggedIn()) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  joining.value = true
  try {
    const res = await joinCollab(route.params.code)
    showToast('已加入协作')
    router.replace({ path: '/trip-detail', query: { id: res.tripId, shared: '1' } })
  } catch (e) {
    showToast(e.message || '加入失败')
  } finally {
    joining.value = false
  }
}

function openTrip() {
  router.replace({ path: '/trip-detail', query: { id: info.value.tripId, shared: '1' } })
}
</script>

<template>
  <div class="join-page">
    <van-nav-bar title="协作邀请" left-arrow @click-left="$router.back()" />

    <div v-if="error" class="state-wrap">
      <van-empty image="error" :description="error" />
    </div>

    <template v-else-if="info">
      <div class="invite-card">
        <div class="invite-from">
          <Icon name="friends-o" /> {{ info.ownerName }} 邀请你一起编辑行程
        </div>
        <div class="invite-dest">{{ info.destination }}</div>
        <div class="invite-meta">
          {{ info.days }} 天 · 预算 ¥{{ info.budget }}
          <template v-if="info.startDate"> · {{ info.startDate }} 出发</template>
        </div>
        <div class="invite-members">
          <Icon name="friends" /> 已有 {{ info.memberCount }} 人参与
        </div>
      </div>

      <div class="actions">
        <van-button v-if="info.role === 'none'" type="primary" block round :loading="joining" @click="join">
          加入协作
        </van-button>
        <van-button v-else type="primary" block round @click="openTrip">
          查看行程（你已在协作中）
        </van-button>
      </div>

      <div class="tip">加入后你可以查看并编辑这份行程，改动会同步给所有成员</div>
    </template>

    <div v-else class="state-wrap"><van-loading size="28" vertical>加载中...</van-loading></div>
  </div>
</template>

<style scoped>
.join-page {
  min-height: 100vh;
  background: var(--bg);
}

.state-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.invite-card {
  margin: 16px 12px;
  border-radius: 14px;
  padding: 22px 18px;
  background: var(--grad-dark);
  color: #fff;
}

.invite-from {
  font-size: 13px;
  opacity: 0.92;
  display: flex;
  align-items: center;
  gap: 4px;
}

.invite-dest {
  font-size: 26px;
  font-weight: 700;
  margin: 10px 0 6px;
}

.invite-meta {
  font-size: 13px;
  opacity: 0.9;
}

.invite-members {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 12px;
}

.actions {
  padding: 0 12px;
}

.tip {
  text-align: center;
  font-size: 12px;
  color: var(--text-2);
  margin-top: 14px;
  padding: 0 24px;
  line-height: 1.6;
}
</style>
