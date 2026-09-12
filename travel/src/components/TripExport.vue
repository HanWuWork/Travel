<script setup>
/**
 * 行程导出：生成分享海报图 / 导出 .ics 日历
 * 用法：<TripExport v-model:show="show" :plan="plan" />
 */
import { ref } from 'vue'
import { showToast } from 'vant'
import { exportIcs, exportPoster } from '../utils/export'

const props = defineProps({
  show: { type: Boolean, default: false },
  plan: { type: Object, default: null }
})
const emit = defineEmits(['update:show'])

const posterRef = ref(null)
const generating = ref(false)

const close = () => emit('update:show', false)

/** 行程 POI 按天分组，用于海报上的行程点展示 */
function poisOfDay(day) {
  return (props.plan?.pois || []).filter((p) => Number(p.day) === Number(day))
}

async function genPoster() {
  generating.value = true
  try {
    await exportPoster(posterRef.value, `${props.plan?.destination || '行程'}海报.png`)
    showToast('海报已生成')
  } catch (e) {
    showToast('海报生成失败，请重试')
  } finally {
    generating.value = false
  }
}

function downloadIcs() {
  try {
    exportIcs(props.plan)
    showToast('日历文件已导出')
  } catch (e) {
    showToast('导出失败')
  }
}
</script>

<template>
  <van-popup :show="show" position="bottom" round @update:show="close" :style="{ padding: '18px 0 0' }">
    <div class="export-title">行程导出</div>

    <!-- 操作 -->
    <div class="export-actions">
      <div class="action-item" @click="genPoster">
        <div class="action-icon" style="background: var(--tint); color: var(--brand-deep)">
          <van-icon name="photo-o" size="22" />
        </div>
        <div class="action-name">{{ generating ? '生成中...' : '分享海报' }}</div>
        <div class="action-desc">生成长图保存/分享</div>
      </div>
      <div class="action-item" @click="downloadIcs">
        <div class="action-icon" style="background: #e8fff3; color: var(--success)">
          <van-icon name="calendar-o" size="22" />
        </div>
        <div class="action-name">导出日历</div>
        <div class="action-desc">.ics 导入手机日历</div>
      </div>
    </div>

    <div class="export-tip" v-if="!plan?.startDate">
      未设置出发日期，日历将按今天开始排期
    </div>

    <van-button block round plain style="margin: 14px 16px 24px; width: auto" @click="close">关闭</van-button>

    <!-- 海报内容（离屏渲染，不显示在页面上） -->
    <div class="poster-holder">
      <div ref="posterRef" class="poster">
        <div class="poster__hero">
          <div class="poster__app">AI 旅游助手</div>
          <div class="poster__dest">{{ plan?.destination || '我的行程' }}</div>
          <div class="poster__meta">
            {{ plan?.days }} 天 · 预算 ¥{{ plan?.budget }}
            <template v-if="plan?.startDate"> · {{ plan.startDate }} 出发</template>
          </div>
        </div>

        <div class="poster__body">
          <div v-for="item in plan?.itinerary || []" :key="item.day" class="poster__day">
            <div class="poster__day-badge">D{{ item.day }}</div>
            <div class="poster__day-main">
              <div class="poster__day-title">{{ item.title }}</div>
              <div class="poster__day-desc">{{ item.description }}</div>
              <div v-if="poisOfDay(item.day).length" class="poster__day-pois">
                📍 {{ poisOfDay(item.day).map((p) => p.name).join(' · ') }}
              </div>
            </div>
          </div>

          <div v-if="plan?.budgetBreakdown?.length" class="poster__budget">
            <span v-for="b in plan.budgetBreakdown" :key="b.label" class="poster__budget-item">
              {{ b.label }} ¥{{ b.amount }}
            </span>
          </div>
        </div>

        <div class="poster__footer">由 AI 旅游助手生成 · 行程仅供参考</div>
      </div>
    </div>
  </van-popup>
</template>

<style scoped>
.export-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
}

.export-actions {
  display: flex;
  gap: 12px;
  padding: 16px;
}

.action-item {
  flex: 1;
  background: var(--bg);
  border-radius: 16px;
  padding: 16px 8px;
  text-align: center;
}

.action-icon {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
}

.action-name {
  font-size: 14px;
  font-weight: 600;
}

.action-desc {
  font-size: 11px;
  color: var(--text-2);
  margin-top: 3px;
}

.export-tip {
  text-align: center;
  font-size: 12px;
  color: #4cccf4;
  padding: 0 16px 4px;
}

/* 海报离屏容器：保留在 DOM 中以便截图，但移出可视区域 */
.poster-holder {
  position: fixed;
  left: -9999px;
  top: 0;
}

.poster {
  width: 375px;
  background: var(--surface);
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Arial, sans-serif;
}

.poster__hero {
  background: var(--grad-dark);
  color: #fff;
  padding: 26px 22px;
}

.poster__app {
  font-size: 12px;
  opacity: 0.85;
  letter-spacing: 1px;
}

.poster__dest {
  font-size: 30px;
  font-weight: 700;
  margin: 8px 0 6px;
}

.poster__meta {
  font-size: 13px;
  opacity: 0.92;
}

.poster__body {
  padding: 18px 22px;
}

.poster__day {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--line);
}

.poster__day:last-of-type {
  border-bottom: none;
}

.poster__day-badge {
  flex-shrink: 0;
  width: 34px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  background: #0e7490;
  color: #fff;
  border-radius: 11px;
  font-size: 12px;
  font-weight: 600;
}

.poster__day-main {
  flex: 1;
}

.poster__day-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}

.poster__day-desc {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 4px;
  line-height: 1.6;
}

.poster__day-pois {
  font-size: 11px;
  color: var(--brand-deep);
  margin-top: 4px;
}

.poster__budget {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.poster__budget-item {
  font-size: 11px;
  color: var(--text-2);
  background: var(--bg);
  border-radius: 10px;
  padding: 3px 9px;
}

.poster__footer {
  text-align: center;
  font-size: 11px;
  color: var(--text-2);
  padding: 8px 0 18px;
}
</style>
