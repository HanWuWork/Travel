<script setup>
/**
 * 游记详情：正文 + 点赞 + 评论（支持回复与删除）
 */
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon, showConfirmDialog, showToast } from 'vant'
import {
  addComment,
  deleteComment,
  deletePost,
  getPost,
  listComments,
  toggleLike
} from '../api/social'
import { getUser, isLoggedIn } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const post = ref(null)
const comments = ref([])
const error = ref('')
const commentText = ref('')
const replyTo = ref(null)
const submitting = ref(false)

const me = getUser()

onMounted(async () => {
  try {
    post.value = await getPost(route.params.id)
    comments.value = await listComments(route.params.id)
  } catch (e) {
    error.value = e.message || '加载失败'
  }
})

async function like() {
  if (!isLoggedIn()) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  try {
    const updated = await toggleLike(post.value.id)
    post.value.liked = updated.liked
    post.value.likeCount = updated.likeCount
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

function startReply(c) {
  replyTo.value = c
  commentText.value = ''
}

function cancelReply() {
  replyTo.value = null
}

async function submitComment() {
  if (!isLoggedIn()) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  if (!commentText.value.trim()) return showToast('请输入评论内容')
  submitting.value = true
  try {
    await addComment({
      postId: post.value.id,
      content: commentText.value.trim(),
      replyTo: replyTo.value ? replyTo.value.id : null
    })
    commentText.value = ''
    replyTo.value = null
    comments.value = await listComments(post.value.id)
    post.value.commentCount = comments.value.length
    showToast('评论成功')
  } catch (e) {
    showToast(e.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

async function removeComment(c) {
  try {
    await showConfirmDialog({ title: '提示', message: '确定删除这条评论吗？' })
    await deleteComment(c.id)
    comments.value = comments.value.filter((x) => x.id !== c.id)
    post.value.commentCount = comments.value.length
    showToast('已删除')
  } catch (e) {
    // 取消
  }
}

async function removePost() {
  try {
    await showConfirmDialog({ title: '提示', message: '确定删除这篇游记吗？' })
    await deletePost(post.value.id)
    showToast('已删除')
    router.back()
  } catch (e) {
    // 取消
  }
}

function isMine(uid) {
  return me && Number(me.id) === Number(uid)
}

const onBack = () => router.back()
</script>

<template>
  <div class="post-detail">
    <van-nav-bar title="游记详情" left-arrow @click-left="onBack">
      <template #right>
        <template v-if="post && isMine(post.userId)">
          <Icon name="edit" size="17" style="margin-right: 14px" @click="router.push({ path: '/posts/publish', query: { id: post.id } })" />
          <Icon name="delete-o" size="17" @click="removePost" />
        </template>
      </template>
    </van-nav-bar>

    <div v-if="error" class="state-wrap"><van-empty image="error" :description="error" /></div>

    <template v-else-if="post">
      <div class="article">
        <h1 class="article__title">{{ post.title }}</h1>
        <div class="article__meta">
          <span class="author">{{ post.authorName || '旅行者' }}</span>
          <span v-if="post.city" class="city"><Icon name="location-o" /> {{ post.city }}</span>
          <span class="time">{{ post.createTime }}</span>
        </div>
        <div v-if="post.tagList && post.tagList.length" class="tags">
          <span v-for="t in post.tagList" :key="t" class="tag">#{{ t }}</span>
        </div>
        <div class="article__content">{{ post.content }}</div>
        <div class="article__stats">
          <span><Icon name="eye-o" /> {{ post.viewCount }} 浏览</span>
          <span><Icon name="chat-o" /> {{ post.commentCount }} 评论</span>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <div class="comment-title">评论 {{ comments.length }}</div>
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <div class="comment-head">
            <span class="comment-author">{{ c.authorName }}</span>
            <span class="comment-time">{{ c.createTime }}</span>
          </div>
          <div class="comment-content">
            <span v-if="c.replyToName" class="reply-tag">回复 {{ c.replyToName }}：</span>{{ c.content }}
          </div>
          <div class="comment-actions">
            <span @click="startReply(c)">回复</span>
            <span v-if="isMine(c.userId)" @click="removeComment(c)">删除</span>
          </div>
        </div>
        <van-empty v-if="!comments.length" description="还没有评论，来说两句" image-size="60" />
      </div>
    </template>

    <div v-else class="state-wrap"><van-loading size="28" vertical>加载中...</van-loading></div>

    <!-- 底部操作栏 -->
    <div v-if="post" class="bottom-bar">
      <div class="reply-hint" v-if="replyTo">回复 @{{ replyTo.authorName }}</div>
      <div class="bottom-row">
        <van-field
          v-model="commentText"
          class="comment-input"
          placeholder="说点什么..."
          @keyup.enter="submitComment"
        />
        <van-button size="small" type="primary" round :loading="submitting" @click="submitComment">发送</van-button>
        <div class="like-btn" :class="{ liked: post.liked }" @click="like">
          <Icon :name="post.liked ? 'like' : 'like-o'" size="20" />
          <span>{{ post.likeCount }}</span>
        </div>
      </div>
      <div v-if="replyTo" class="cancel-reply" @click="cancelReply">取消回复</div>
    </div>
  </div>
</template>

<style scoped>
.post-detail {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 90px;
}

.state-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.article {
  background: var(--surface);
  margin: 12px;
  border-radius: 16px;
  padding: 16px;
}

.article__title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 10px;
  line-height: 1.4;
}

.article__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--text-2);
}

.city {
  color: var(--brand-deep);
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.tag {
  font-size: 12px;
  color: var(--brand-deep);
}

.article__content {
  margin-top: 14px;
  font-size: 15px;
  line-height: 1.8;
  color: var(--text);
  white-space: pre-wrap;
  word-break: break-word;
}

.article__stats {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  padding-top: 10px;
  border-top: 1px solid var(--line);
  font-size: 12px;
  color: var(--text-2);
}

.comment-section {
  background: var(--surface);
  margin: 0 12px;
  border-radius: 16px;
  padding: 14px;
}

.comment-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--line);
}

.comment-item:last-of-type {
  border-bottom: none;
}

.comment-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-author {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
}

.comment-time {
  font-size: 11px;
  color: var(--text-2);
}

.comment-content {
  font-size: 14px;
  color: var(--text);
  line-height: 1.6;
  margin-top: 4px;
}

.reply-tag {
  color: var(--brand-deep);
}

.comment-actions {
  display: flex;
  gap: 14px;
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-2);
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 750px;
  background: var(--surface);
  border-top: 1px solid var(--line);
  padding: 8px 12px calc(8px + env(safe-area-inset-bottom));
  z-index: 900;
}

.reply-hint {
  font-size: 11px;
  color: var(--brand-deep);
  margin-bottom: 4px;
}

.bottom-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-input {
  flex: 1;
  background: var(--bg);
  border-radius: 18px;
  padding: 4px 12px;
}

.comment-input :deep(.van-field__body) {
  padding: 0;
}

.like-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 10px;
  color: var(--text-2);
  flex-shrink: 0;
}

.like-btn.liked {
  color: var(--danger);
}

.cancel-reply {
  text-align: right;
  font-size: 11px;
  color: var(--text-2);
  margin-top: 4px;
}
</style>
