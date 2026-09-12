<template>
  <div class="journey-card">
    <Card>
      <template #title>
        <div class="card-title">
          <Icon name="location-o" />
          <span>旅程规划</span>
        </div>
      </template>
      <template #desc>
        <div class="journey-form">
          <Field
            v-model="form.destination"
            label="旅游目的地"
            placeholder="请选择目的地"
            is-link
            readonly
            @click="showDestinationPicker = true"
          />
          <Field
            v-model="form.budget"
            label="预算"
            placeholder="请输入预算金额"
            type="number"
          >
            <template #button>
              <span class="field-suffix">元</span>
            </template>
          </Field>
          <Field
            v-model="form.days"
            label="天数"
            placeholder="请输入天数"
            type="number"
          >
            <template #button>
              <span class="field-suffix">天</span>
            </template>
          </Field>
        </div>
      </template>
      <template #footer>
        <div class="journey-actions">
          <Button class="action-btn action-btn--main" round @click="onStartPlan">
            开始规划
          </Button>
          <Button class="action-btn action-btn--ghost" round @click="onReset">
            重置
          </Button>
        </div>
      </template>
    </Card>

    <Popup v-model:show="showDestinationPicker" position="bottom" class="destination-popup">
      <Picker
        title="选择目的地"
        :columns="destinationColumns"
        @confirm="onDestinationConfirm"
        @cancel="showDestinationPicker = false"
      />
    </Popup>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Card, Icon, Button, Field, Popup, Picker, showToast } from 'vant'

const props = defineProps({
  journey: {
    type: Object,
    default: () => ({
      destination: '',
      budget: '',
      days: ''
    })
  }
})

const emit = defineEmits(['view-plan', 'edit-plan', 'update:journey'])
const router = useRouter()

const showDestinationPicker = ref(false)

const form = ref({
  destination: props.journey.destination || '',
  budget: props.journey.budget || '',
  days: props.journey.days || ''
})

watch(
  () => props.journey,
  (newJourney) => {
    form.value = {
      destination: newJourney.destination || '',
      budget: newJourney.budget || '',
      days: newJourney.days || ''
    }
  },
  { deep: true }
)

const destinationColumns = [
  { text: '北京', value: '北京' },
  { text: '上海', value: '上海' },
  { text: '杭州', value: '杭州' },
  { text: '成都', value: '成都' },
  { text: '西安', value: '西安' },
  { text: '三亚', value: '三亚' },
  { text: '厦门', value: '厦门' },
  { text: '云南', value: '云南' },
  { text: '西藏', value: '西藏' },
  { text: '新疆', value: '新疆' },
  { text: '广州', value: '广州' },
  { text: '深圳', value: '深圳' },
  { text: '南京', value: '南京' },
  { text: '重庆', value: '重庆' },
  { text: '青岛', value: '青岛' }
]

const onDestinationConfirm = ({ selectedOptions }) => {
  form.value.destination = selectedOptions[0].text
  showDestinationPicker.value = false
  emit('update:journey', { ...form.value })
}

const onReset = () => {
  form.value = { destination: '', budget: '', days: '' }
  emit('update:journey', { ...form.value })
  emit('edit-plan')
}

const onStartPlan = () => {
  if (!form.value.destination) {
    showToast('请先选择旅游目的地')
    return
  }
  if (!form.value.budget) {
    showToast('请输入预算金额')
    return
  }
  if (!form.value.days) {
    showToast('请输入行程天数')
    return
  }
  emit('update:journey', { ...form.value })
  router.push({
    path: '/plan',
    query: {
      destination: form.value.destination,
      budget: form.value.budget,
      days: form.value.days
    }
  })
}
</script>

<style scoped>
/* 首页主视觉：亮黄大色块 + 近黑字，呼应参考图 */
.journey-card {
  margin: 12px;
  border-radius: var(--radius-lg);
  background: var(--grad-brand);
  padding: 10px 12px 8px;
  box-shadow: 0 8px 24px rgba(180, 140, 0, 0.28);
  position: relative;
  overflow: hidden;
}

/* 右上角浅色圆形装饰 */
.journey-card::after {
  content: '';
  position: absolute;
  right: -28px;
  top: -28px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.28);
  pointer-events: none;
}

.journey-card :deep(.van-card) {
  background: transparent;
  padding: 0;
  box-shadow: none;
  border-radius: 0;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 15px;
  font-weight: 800;
  color: var(--brand-ink);
  position: relative;
  z-index: 1;
}

.journey-form {
  background: var(--surface);
  border-radius: 14px;
  padding: 6px 14px;
  margin: 10px 0 4px;
  position: relative;
  z-index: 1;
  /* 中间表单区占卡片主体：Field 基于 Cell，行高由 cell 内边距变量控制 */
  --van-field-vertical-padding: 15px;
  --van-cell-vertical-padding: 15px;
  --van-field-label-color: var(--text-2);
}

/* 显式设置每行输入高度，不依赖变量名，避免 Vant 版本差异 */
.journey-form :deep(.van-field.van-cell) {
  padding-top: 15px;
  padding-bottom: 15px;
}

/* 输入文字/标签：略小于上一步，配合整体缩小 */
.journey-form :deep(.van-field__label) {
  font-size: 14.5px;
  line-height: 22px;
}

.journey-form :deep(.van-field__control) {
  font-size: 15px;
  line-height: 22px;
  min-height: 22px;
}

.journey-form :deep(.van-field__body) {
  min-height: 22px;
}

/* 输入内容用主题文字色并加粗，未填写时保持占位色 */
.journey-form :deep(input.van-field__control) {
  font-weight: 600;
  color: var(--text);
}

.field-suffix {
  color: var(--text-2);
  font-size: 13px;
}

.journey-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  padding: 6px 0 2px;
  position: relative;
  z-index: 1;
}

.action-btn {
  flex: 1;
  height: 34px;
  font-size: 13.5px;
  border: none;
}

/* 深色主按钮 + 黄色文字，在黄底上对比最强 */
.journey-card :deep(.action-btn--main) {
  background: var(--ink);
  color: var(--brand);
  font-weight: 700;
}

.journey-card :deep(.action-btn--ghost) {
  background: rgba(20, 20, 0, 0.06);
  color: var(--brand-ink);
  border: 1.5px solid rgba(20, 20, 0, 0.35);
  font-weight: 700;
}
</style>
