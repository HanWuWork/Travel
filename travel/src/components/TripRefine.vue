<script setup>
/**
 * AI 行程微调：输入一句要求（如"第二天太赶了"），AI 返回调整后的行程，可应用并保存
 */
import { ref } from 'vue'
import { Icon, Popup, showToast } from 'vant'
import { refineTrip, updateTrip } from '../api/trip'
import { updateCollabTrip } from '../api/collab'

const props = defineProps({
  show: { type: Boolean, default: false },
  plan: { type: Object, default: null },
  /** owner-我的行程 / shared-协作行程 */
  mode: { type: String, default: 'owner' }
})
const emit = defineEmits(['update:show', 'applied'])

const instruction = ref('')
const refining = ref(false)
const result = ref(null)
const applying = ref(false)

const QUICK = ['行程太赶了，改轻松一点', '多安排一些当地美食', '减少购物，增加自然风光', '控制在预算内，再省一点']

function close() {
  emit('update:show', false)
}

async function refine() {
  if (!instruction.value.trim()) return showToast('请输入修改要求')
  refining.value = true
  result.value = null
  try {
    result.value = await refineTrip({ plan: props.plan, instruction: instruction.value.trim() })
  } catch (e) {
    showToast(e.message || '调整失败')
  } finally {
    refining.value = false
  }
}

async function apply() {
  if (!result.value) return
  if (!props.plan?.id) {
    showToast('该行程未保存，请先保存后再应用')
    return
  }
  applying.value = true
  try {
    if (props.mode === 'shared') {
      await updateCollabTrip(props.plan.id, result.value)
    } else {
      await updateTrip(props.plan.id, { startDate: props.plan.startDate || '', plan: result.value })
    }
    showToast('已保存到行程')
    emit('applied', result.value)
    close()
  } catch (e) {
    showToast(e.message || '保存失败')
  } finally {
    applying.value = false
  }
}
</script>

<template>
  <Popup :show="show" position="bottom" round @update:show="close" :style="{ padding: '18px 16px 24px', maxHeight: '86vh' }">
    <div class="refine-title">AI 行程微调</div>
    <div class="refine-sub">用一句话告诉 AI 你想怎么改</div>

    <div class="quick-chips">
      <span v-for="q in QUICK" :key="q" class="quick-chip" @click="instruction = q">{{ q }}</span>
    </div>

    <div class="input-row">
      <van-field
        v-model="instruction"
        class="refine-input"
        placeholder="如：第二天安排太满了，改成轻松点"
        maxlength="100"
        @keyup.enter="refine"
      />
      <van-button type="primary" size="small" round :loading="refining" @click="refine">调整</van-button>
    </div>

    <!-- 结果预览 -->
    <div v-if="result" class="result-box">
      <div class="result-head">
        <span>调整后的行程</span>
        <span class="result-days">{{ result.itinerary?.length }} 天</span>
      </div>
      <div class="result-list">
        <div v-for="d in result.itinerary" :key="d.day" class="result-day">
          <span class="day-badge">D{{ d.day }}</span>
          <div class="day-main">
            <div class="day-title">{{ d.title }}</div>
            <div class="day-desc">{{ d.description }}</div>
          </div>
        </div>
      </div>
      <div class="result-actions">
        <van-button round plain size="small" @click="result = null">放弃</van-button>
        <van-button round type="primary" size="small" :loading="applying" @click="apply">应用并保存</van-button>
      </div>
    </div>

    <div v-else-if="refining" class="refining">
      <van-loading size="24" vertical>AI 正在调整行程...</van-loading>
    </div>

    <div v-else class="refine-hint">
      <Icon name="info-o" /> 调整结果仅在你点击"应用并保存"后才会覆盖当前行程
    </div>
  </Popup>
</template>

<style scoped>
.refine-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
}

.refine-sub {
  font-size: 12px;
  color: var(--text-2);
  text-align: center;
  margin: 6px 0 12px;
}

.quick-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.quick-chip {
  font-size: 12px;
  color: var(--brand-deep);
  background: var(--tint);
  border-radius: 16px;
  padding: 4px 10px;
}

.input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.refine-input {
  flex: 1;
  background: var(--bg);
  border-radius: 18px;
  padding: 4px 12px;
}

.refine-input :deep(.van-field__body) {
  padding: 0;
}

.result-box {
  margin-top: 16px;
  border-top: 1px solid var(--line);
  padding-top: 12px;
}

.result-head {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.result-days {
  font-size: 12px;
  color: var(--text-2);
  font-weight: 400;
}

.result-list {
  max-height: 34vh;
  overflow-y: auto;
}

.result-day {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--line);
}

.day-badge {
  flex-shrink: 0;
  width: 30px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  background: var(--tint);
  color: var(--brand-deep);
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.day-title {
  font-size: 13px;
  font-weight: 600;
}

.day-desc {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 2px;
  line-height: 1.5;
}

.result-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}

.refining {
  padding: 30px 0;
  text-align: center;
}

.refine-hint {
  margin-top: 18px;
  font-size: 12px;
  color: var(--text-2);
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
