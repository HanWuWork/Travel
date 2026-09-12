<script setup>
/**
 * 发布/编辑游记
 */
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getPost, publishPost, updatePost } from '../api/social'
import { isLoggedIn } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const editId = ref(null)
const submitting = ref(false)
const form = reactive({ title: '', content: '', city: '', tags: '' })

onMounted(async () => {
  if (!isLoggedIn()) {
    showToast('请先登录')
    router.replace('/login')
    return
  }
  const id = route.query.id
  if (id) {
    editId.value = id
    try {
      const p = await getPost(id)
      form.title = p.title
      form.content = p.content || ''
      form.city = p.city || ''
      form.tags = (p.tagList || []).join(' ')
    } catch (e) {
      showToast(e.message || '加载失败')
    }
  }
})

async function submit() {
  if (!form.title.trim()) return showToast('请填写标题')
  if (!form.content.trim()) return showToast('请填写正文')
  submitting.value = true
  try {
    const payload = {
      title: form.title.trim(),
      content: form.content,
      city: form.city.trim(),
      tags: form.tags.trim()
    }
    if (editId.value) {
      await updatePost(editId.value, payload)
      showToast('已更新')
    } else {
      await publishPost(payload)
      showToast('发布成功')
    }
    router.back()
  } catch (e) {
    showToast(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="publish-page">
    <van-nav-bar :title="editId ? '编辑游记' : '发布游记'" left-arrow @click-left="$router.back()" />

    <div class="form-card">
      <van-field v-model="form.title" label="标题" placeholder="给游记起个标题" maxlength="100" show-word-limit />
      <van-field v-model="form.city" label="目的地" placeholder="如：杭州（可空）" maxlength="20" />
      <van-field v-model="form.tags" label="话题" placeholder="空格分隔，如：美食 亲子 自驾（可空）" maxlength="60" />
      <van-field
        v-model="form.content"
        type="textarea"
        label="正文"
        placeholder="分享你的行程、路线、花费与避坑经验..."
        rows="10"
        maxlength="5000"
        show-word-limit
      />
    </div>

    <div class="tip">小贴士：写清楚「几天几晚、人均花费、路线顺序」，对其他旅行者最有帮助。</div>

    <div class="actions">
      <van-button type="primary" block round :loading="submitting" @click="submit">
        {{ editId ? '保存修改' : '发布' }}
      </van-button>
    </div>
  </div>
</template>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 30px;
}

.form-card {
  margin: 12px;
  border-radius: 16px;
  overflow: hidden;
}

.tip {
  margin: 0 16px 16px;
  font-size: 12px;
  color: var(--text-2);
  line-height: 1.6;
}

.actions {
  padding: 0 12px;
}
</style>
