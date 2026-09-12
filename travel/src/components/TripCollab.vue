<script setup>
/**
 * 行程协作面板：生成分享码、复制链接、成员管理与移除
 */
import { computed, ref, watch } from 'vue'
import { Icon, Popup, showConfirmDialog, showToast } from 'vant'
import {
  collabMembers,
  createShare,
  removeCollabMember
} from '../api/collab'
import { getUser } from '../utils/auth'

const props = defineProps({
  show: { type: Boolean, default: false },
  tripId: { type: [Number, String], default: null }
})
const emit = defineEmits(['update:show'])

const share = ref(null)
const members = ref([])
const loading = ref(false)
const me = getUser()

const shareLink = computed(() => {
  if (!share.value?.shareCode) return ''
  return `${location.origin}/collab/${share.value.shareCode}`
})

watch(() => props.show, async (v) => {
  if (v && props.tripId) {
    await load()
  }
})

async function load() {
  loading.value = true
  try {
    share.value = await createShare(props.tripId)
    members.value = await collabMembers(props.tripId)
  } catch (e) {
    showToast(e.message || '获取协作信息失败')
  } finally {
    loading.value = false
  }
}

async function copyLink() {
  const text = `一起编辑行程「${share.value?.destination}」：${shareLink.value}（分享码 ${share.value?.shareCode}）`
  try {
    await navigator.clipboard.writeText(text)
    showToast('链接已复制')
  } catch (e) {
    showToast('复制失败，请手动长按复制')
  }
}

async function remove(m) {
  try {
    await showConfirmDialog({ title: '提示', message: `移除成员「${m.name}」？` })
    await removeCollabMember(props.tripId, m.id)
    showToast('已移除')
    await load()
  } catch (e) {
    // 取消
  }
}

function isMe(m) {
  return me && Number(me.id) === Number(m.userId)
}

function isOwnerRole(m) {
  return m.role === 'owner'
}
</script>

<template>
  <Popup :show="show" position="bottom" round @update:show="(v) => emit('update:show', v)" :style="{ padding: '18px 16px 24px' }">
    <div class="collab-title">邀请好友一起编辑</div>

    <van-loading v-if="loading" class="collab-loading" size="24" vertical>加载中...</van-loading>

    <template v-else-if="share">
      <div class="code-box">
        <div class="code-label">分享码</div>
        <div class="code-value">{{ share.shareCode }}</div>
      </div>
      <div class="link-box">{{ shareLink }}</div>
      <van-button type="primary" block round @click="copyLink">
        <Icon name="link-o" /> 复制邀请链接
      </van-button>

      <div class="member-head">
        <span>成员（{{ members.length }}）</span>
        <span class="member-hint">好友打开链接后即可加入</span>
      </div>
      <div class="member-list">
        <div v-for="m in members" :key="m.id" class="member-item">
          <div class="member-avatar">
            <van-image round width="30" height="30" :src="m.avatar">
              <template #error><Icon name="user-o" /></template>
            </van-image>
          </div>
          <span class="member-name">{{ m.name }}<span v-if="isMe(m)" class="me-tag">（我）</span></span>
          <span v-if="isOwnerRole(m)" class="role-tag role-tag--owner">创建者</span>
          <span v-else class="role-tag">协作者</span>
          <Icon
            v-if="!isOwnerRole(m) && (share.ownerId === me?.id || isMe(m))"
            name="cross"
            class="member-del"
            @click="remove(m)"
          />
        </div>
      </div>
      <div class="collab-note">
        <Icon name="info-o" /> 协作者修改行程后，其他成员打开行程页会自动同步最新版本
      </div>
    </template>
  </Popup>
</template>

<style scoped>
.collab-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.collab-loading {
  padding: 30px 0;
  text-align: center;
}

.code-box {
  text-align: center;
  padding: 14px;
  border-radius: 16px;
  background: var(--tint);
  border: 1px dashed #b3d4ff;
}

.code-label {
  font-size: 12px;
  color: var(--text-2);
}

.code-value {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 4px;
  color: var(--brand-deep);
  margin-top: 4px;
}

.link-box {
  font-size: 11px;
  color: var(--text-2);
  text-align: center;
  margin: 10px 0 12px;
  word-break: break-all;
}

.member-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 14px;
  font-weight: 600;
  margin: 18px 0 6px;
}

.member-hint {
  font-size: 11px;
  color: var(--text-2);
  font-weight: 400;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 0;
  border-bottom: 1px solid var(--line);
}

.member-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--line);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-2);
}

.member-name {
  flex: 1;
  font-size: 14px;
}

.me-tag {
  font-size: 11px;
  color: var(--text-2);
}

.role-tag {
  font-size: 11px;
  color: var(--brand-deep);
  background: var(--tint);
  border-radius: 8px;
  padding: 2px 8px;
}

.role-tag--owner {
  color: #4cccf4;
  background: #fff4ec;
}

.member-del {
  color: var(--text-2);
  font-size: 14px;
}

.collab-note {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--text-2);
  margin-top: 14px;
}
</style>
