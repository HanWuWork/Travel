<script setup>
/**
 * 景点评价区：评分汇总 + 星级分布 + 热门标签 + 评价列表 + 写评价弹层
 */
import { computed, onMounted, ref } from 'vue'
import { Icon, Popup, Rate, showConfirmDialog, showToast } from 'vant'
import { listReviews, reviewSummary, submitReview, deleteReview } from '../api/review'
import { isLoggedIn } from '../utils/auth'

const TAGS = ['适合亲子', '拍照好看', '值得再来', '排队较久', '性价比高', '交通便利', '人少清净', '美食很多']

const props = defineProps({
  attractionId: { type: [Number, String], required: true }
})

const summary = ref(null)
const reviews = ref([])
const showForm = ref(false)
const submitting = ref(false)
const form = ref({ rating: 5, content: '', tags: [] })

const maxCount = computed(() => {
  const d = summary.value?.distribution || []
  return Math.max(1, ...d)
})

onMounted(load)

async function load() {
  try {
    const [s, list] = await Promise.all([
      reviewSummary(props.attractionId),
      listReviews(props.attractionId)
    ])
    summary.value = s
    reviews.value = list
  } catch (e) {
    // 评价加载失败不影响详情页
  }
}

function openForm() {
  if (!isLoggedIn()) {
    showToast('请先登录后再评价')
    return
  }
  const mine = reviews.value.find((r) => r.mine)
  form.value = mine
    ? { rating: mine.rating, content: mine.content || '', tags: [...(mine.tagList || [])] }
    : { rating: 5, content: '', tags: [] }
  showForm.value = true
}

function toggleTag(tag) {
  const idx = form.value.tags.indexOf(tag)
  if (idx >= 0) form.value.tags.splice(idx, 1)
  else form.value.tags.push(tag)
}

async function submit() {
  submitting.value = true
  try {
    await submitReview({
      attractionId: Number(props.attractionId),
      rating: form.value.rating,
      content: form.value.content.trim(),
      tags: form.value.tags.join(',')
    })
    showForm.value = false
    showToast('评价已发布')
    await load()
  } catch (e) {
    showToast(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

async function removeReview(r) {
  try {
    await showConfirmDialog({ title: '提示', message: '确定删除这条评价吗？' })
    await deleteReview(r.id)
    showToast('已删除')
    await load()
  } catch (e) {
    // 取消
  }
}
</script>

<template>
  <div class="review-section">
    <div class="review-head">
      <div class="card-title">用户评价
        <span class="count" v-if="summary">（{{ summary.reviewCount }}）</span>
      </div>
      <van-button size="mini" round type="primary" plain @click="openForm">写评价</van-button>
    </div>

    <!-- 评分汇总 -->
    <div v-if="summary && summary.reviewCount" class="summary">
      <div class="summary__score">
        <div class="summary__avg">{{ summary.avgRating }}</div>
        <Rate :model-value="Math.round(summary.avgRating)" readonly allow-half size="12" />
        <div class="summary__label">{{ summary.reviewCount }} 条评价</div>
      </div>
      <div class="summary__dist">
        <div v-for="(c, i) in (summary.distribution || [])" :key="i" class="dist-row">
          <span class="dist-star">{{ 5 - i }}星</span>
          <div class="dist-bar-wrap">
            <div class="dist-bar" :style="{ width: (c / maxCount) * 100 + '%' }"></div>
          </div>
          <span class="dist-count">{{ c }}</span>
        </div>
      </div>
    </div>

    <!-- 热门标签 -->
    <div v-if="summary && summary.topTags && summary.topTags.length" class="tag-cloud">
      <span v-for="t in summary.topTags" :key="t.tag" class="cloud-tag">
        {{ t.tag }} {{ t.count }}
      </span>
    </div>

    <!-- 评价列表 -->
    <div v-if="reviews.length" class="review-list">
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <div class="review-item__head">
          <span class="review-author">{{ r.authorName || '旅行者' }}</span>
          <Rate :model-value="r.rating" readonly size="11" />
          <span class="review-time">{{ r.createTime }}</span>
        </div>
        <div v-if="r.content" class="review-content">{{ r.content }}</div>
        <div v-if="r.tagList && r.tagList.length" class="review-tags">
          <span v-for="t in r.tagList" :key="t" class="review-tag">{{ t }}</span>
        </div>
        <div v-if="r.mine" class="review-actions">
          <span @click="openForm">修改</span>
          <span @click="removeReview(r)">删除</span>
        </div>
      </div>
    </div>
    <div v-else class="review-empty">还没有评价，来写第一条吧</div>

    <!-- 写评价弹层 -->
    <Popup v-model:show="showForm" position="bottom" round :style="{ padding: '20px 16px 28px' }">
      <div class="form-title">发表评价</div>
      <div class="form-row">
        <span class="form-label">评分</span>
        <Rate v-model="form.rating" size="22" color="#c98a00" />
      </div>
      <div class="form-row form-row--wrap">
        <span class="form-label">标签</span>
        <div class="form-tags">
          <span
            v-for="t in TAGS"
            :key="t"
            class="form-tag"
            :class="{ 'form-tag--active': form.tags.includes(t) }"
            @click="toggleTag(t)"
          >{{ t }}</span>
        </div>
      </div>
      <van-field
        v-model="form.content"
        type="textarea"
        rows="4"
        maxlength="500"
        show-word-limit
        placeholder="说说你的真实体验..."
      />
      <van-button type="primary" block round style="margin-top: 14px" :loading="submitting" @click="submit">
        发布评价
      </van-button>
    </Popup>
  </div>
</template>

<style scoped>
.review-section {
  background: var(--surface);
  border-radius: 16px;
  margin: 12px;
  padding: 14px 16px;
}

.review-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
}

.count {
  font-size: 12px;
  color: var(--text-2);
  font-weight: 400;
}

.summary {
  display: flex;
  gap: 18px;
  margin-top: 12px;
}

.summary__score {
  width: 86px;
  text-align: center;
  flex-shrink: 0;
}

.summary__avg {
  font-size: 30px;
  font-weight: 700;
  color: var(--brand-deep);
  line-height: 1.1;
}

.summary__label {
  font-size: 11px;
  color: var(--text-2);
  margin-top: 4px;
}

.summary__dist {
  flex: 1;
}

.dist-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.dist-star {
  font-size: 11px;
  color: var(--text-2);
  width: 26px;
  flex-shrink: 0;
}

.dist-bar-wrap {
  flex: 1;
  height: 7px;
  background: var(--line);
  border-radius: 4px;
  overflow: hidden;
}

.dist-bar {
  height: 100%;
  background: #04dc9c;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.dist-count {
  font-size: 11px;
  color: var(--text-2);
  width: 20px;
  text-align: right;
  flex-shrink: 0;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.cloud-tag {
  font-size: 12px;
  color: var(--brand-deep);
  background: var(--tint);
  border-radius: 10px;
  padding: 3px 10px;
}

.review-list {
  margin-top: 6px;
}

.review-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--line);
}

.review-item:last-child {
  border-bottom: none;
}

.review-item__head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-author {
  font-size: 13px;
  font-weight: 600;
}

.review-time {
  margin-left: auto;
  font-size: 11px;
  color: var(--text-2);
}

.review-content {
  font-size: 14px;
  color: var(--text);
  line-height: 1.6;
  margin-top: 6px;
}

.review-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.review-tag {
  font-size: 11px;
  color: var(--text-2);
  background: var(--bg);
  border-radius: 8px;
  padding: 2px 8px;
}

.review-actions {
  display: flex;
  gap: 14px;
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-2);
}

.review-empty {
  text-align: center;
  font-size: 13px;
  color: var(--text-2);
  padding: 20px 0 8px;
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 16px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.form-row--wrap {
  align-items: flex-start;
}

.form-label {
  font-size: 14px;
  color: var(--text-2);
  flex-shrink: 0;
}

.form-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.form-tag {
  font-size: 12px;
  color: var(--text-2);
  background: var(--bg);
  border-radius: 10px;
  padding: 3px 10px;
  border: 1px solid transparent;
}

.form-tag--active {
  color: var(--brand-deep);
  background: var(--tint);
  border-color: var(--brand-deep);
}
</style>
