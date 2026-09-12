<script setup>
/**
 * 编辑个人资料：头像上传 + 昵称/签名/常居地
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, Uploader } from 'vant'
import { getProfile, updateProfile, uploadAvatar } from '../api/profile'
import { getUser, setUser } from '../utils/auth'

const router = useRouter()

const form = reactive({ nickname: '', bio: '', city: '', avatar: '' })
const fileList = ref([])
const saving = ref(false)
const loading = ref(true)

onMounted(async () => {
  try {
    const p = await getProfile()
    form.nickname = p.nickname || ''
    form.bio = p.bio || ''
    form.city = p.city || ''
    form.avatar = p.avatar || ''
    if (form.avatar) {
      fileList.value = [{ url: form.avatar, isImage: true }]
    }
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
})

/** 选择图片后立即上传，成功后保存返回的 URL */
async function afterRead(item) {
  item.status = 'uploading'
  item.message = '上传中...'
  try {
    const res = await uploadAvatar(item.file)
    form.avatar = res.url
    item.status = 'done'
    item.url = res.url
    item.message = ''
    showToast('头像已上传，记得保存')
  } catch (e) {
    item.status = 'failed'
    item.message = '上传失败'
    showToast(e.message || '上传失败')
  }
}

async function save() {
  if (!form.nickname.trim()) return showToast('昵称不能为空')
  saving.value = true
  try {
    const updated = await updateProfile({
      nickname: form.nickname.trim(),
      bio: form.bio.trim(),
      city: form.city.trim(),
      avatar: form.avatar
    })
    // 同步本地缓存，界面立刻生效
    const cached = getUser() || {}
    setUser({ ...cached, nickname: updated.nickname, avatar: updated.avatar, bio: updated.bio, city: updated.city })
    showToast('已保存')
    router.back()
  } catch (e) {
    showToast(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-edit">
    <van-nav-bar title="编辑资料" left-arrow @click-left="$router.back()" />

    <div v-if="!loading" class="avatar-block">
      <Uploader
        v-model="fileList"
        :max-count="1"
        :after-read="afterRead"
        accept="image/*"
      />
      <div class="avatar-tip">点击上传头像（jpg/png，最大 5MB）</div>
    </div>

    <div class="form-card" v-if="!loading">
      <van-field v-model="form.nickname" label="昵称" placeholder="请输入昵称" maxlength="20" />
      <van-field v-model="form.city" label="常居地" placeholder="如：杭州" maxlength="20" />
      <van-field
        v-model="form.bio"
        type="textarea"
        label="签名"
        placeholder="介绍一下自己，比如：爱旅行，爱美食"
        rows="3"
        maxlength="100"
        show-word-limit
      />
    </div>

    <div class="actions" v-if="!loading">
      <van-button type="primary" block round :loading="saving" @click="save">保存</van-button>
    </div>
  </div>
</template>

<style scoped>
.profile-edit {
  min-height: 100vh;
  background: var(--bg);
}

.avatar-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0 12px;
}

.avatar-block :deep(.van-uploader__upload) {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: #eef2f7;
}

.avatar-block :deep(.van-uploader__preview-image) {
  width: 84px;
  height: 84px;
  border-radius: 50%;
}

.avatar-tip {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 10px;
}

.form-card {
  margin: 0 12px;
  border-radius: 16px;
  overflow: hidden;
}

.actions {
  padding: 18px 12px;
}
</style>
